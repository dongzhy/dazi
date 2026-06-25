package com.zhy.dazi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhy.dazi.common.BaseResponse;
import com.zhy.dazi.common.ErrorCode;
import com.zhy.dazi.common.ResultUtils;
import com.zhy.dazi.exception.BusinessException;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.domain.request.UserLoginRequest;
import com.zhy.dazi.model.request.UserRegisterRequest;
import com.zhy.dazi.model.request.UserTagUpdateRequest;
import com.zhy.dazi.model.request.UserUpdateRequest;
import com.zhy.dazi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhy.dazi.contant.UserConstant.ADMIN_ROLE;
import static com.zhy.dazi.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     */
    @ApiOperation("注册用户")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销（新增补齐接口）
     */
    @ApiOperation("用户注销")
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     */
    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 搜索用户
     */
    @ApiOperation("搜索用户")
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam(required = false) String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 首页推荐
     */
    @ApiOperation("首页推荐")
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(@RequestParam long pageSize, @RequestParam long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
           throw new BusinessException(ErrorCode.NULL_ERROR,"请先完成登录");
        }
        String redisKey = String.format("daZi:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperation = redisTemplate.opsForValue();
        Page<User> userPage = null;

        try {
            Object cacheObj = valueOperation.get(redisKey);
            if (cacheObj != null && cacheObj instanceof Page) {
                userPage = (Page<User>) cacheObj;
            }
        } catch (ClassCastException e) {
            log.error("Redis缓存数据类型转换失败，key:{}", redisKey, e);
            redisTemplate.delete(redisKey);
        }

        if (userPage != null) {
            return ResultUtils.success(userPage);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("id", loginUser.getId());
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

        try {
            valueOperation.set(redisKey, userPage, 300, TimeUnit.SECONDS);
            log.info("用户推荐缓存写入成功，key:{}，有效期5分钟", redisKey);
        } catch (Exception e) {
            log.error("Redis缓存写入失败，key:{}，pageNum:{}，pageSize:{}", redisKey, pageNum, pageSize, e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 根据标签搜索用户
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchByTags(@RequestParam(required = false, name = "tagNameList") List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.SearchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.getLoginUser(request);
        if (userLogin == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int result = userService.updateUser(userUpdateRequest, userLogin);
        return ResultUtils.success(result);
    }
    /**
     * 修改标签（新增补齐接口）
     */
    @ApiOperation("修改标签")
    @PostMapping("/update/tags")
    public BaseResponse<Integer> updateUserTags(@RequestBody UserTagUpdateRequest userTagUpdateRequest, HttpServletRequest request) {
        if (userTagUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int result = userService.updateUserTags(loginUser.getId(), userTagUpdateRequest.getTags());
        return ResultUtils.success(result);
    }


    /**
     * 删除用户
     */
    @ApiOperation("删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 标签匹配用户
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(@RequestParam long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}