package com.zhy.dazi.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.dazi.common.ErrorCode;
import com.zhy.dazi.exception.BusinessException;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.request.UserUpdateRequest;
import com.zhy.dazi.service.UserService;
import com.zhy.dazi.mapper.UserMapper;
import com.zhy.dazi.contant.UserConstant;
import com.zhy.dazi.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.zhy.dazi.contant.UserConstant.ADMIN_ROLE;
import static com.zhy.dazi.contant.UserConstant.USER_LOGIN_STATE;
/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SALT = "zhy";
    private static final Gson GSON = new Gson();

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }

        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含非法特殊字符");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不足");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不足");
        }

        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含非法字符");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        User safetyUser = getSafetyUser(user);
        HttpSession session = request.getSession();
        session.setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> SearchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            Set<String> temptagNameSet = GSON.fromJson(tagStr, new TypeToken<Set<String>>() {}.getType());
            temptagNameSet = Optional.ofNullable(temptagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!temptagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(UserUpdateRequest userUpdateRequest, User userLogin) {
        Long userId = userUpdateRequest.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新目标用户ID不能为空");
        }
        boolean isAdmin = isAdmin(userLogin);
        // 非管理员只能更新自己
        if (!isAdmin && !userId.equals(userLogin.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限修改他人信息");
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "目标用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(userId);

        if (StringUtils.isNotBlank(userUpdateRequest.getUsername())) {
            if (userUpdateRequest.getUsername().length()>20){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名长度不能超过20");
            }
            updateUser.setUsername(userUpdateRequest.getUsername());
        }
        if (StringUtils.isNotBlank(userUpdateRequest.getAvatarUrl())) {
            updateUser.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        }
        if (userUpdateRequest.getGender() != null) {
            updateUser.setGender(userUpdateRequest.getGender());
        }

        if (StringUtils.isNotBlank(userUpdateRequest.getPhone())) {
            if (!userUpdateRequest.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
            updateUser.setPhone(userUpdateRequest.getPhone());
        }
        if (StringUtils.isNotBlank(userUpdateRequest.getEmail())) {
            if (!userUpdateRequest.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
            }
            updateUser.setEmail(userUpdateRequest.getEmail());
        }
        if (userUpdateRequest.getTags() != null) {
            try {
                GSON.fromJson(userUpdateRequest.getTags(), new TypeToken<List<String>>() {}.getType());
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签格式不正确，必须为JSON数组");
            }
            updateUser.setTags(userUpdateRequest.getTags());
        }

        // 优化：判断是否有需要更新的字段
        if (updateUser.getUsername() == null && updateUser.getAvatarUrl() == null
                && updateUser.getGender() == null && updateUser.getPhone() == null
                && updateUser.getEmail() == null && updateUser.getTags() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未填写任何更新内容");
        }

        return userMapper.updateById(updateUser);
    }

    /**
     * 修改标签独立方法实现
     */
    @Override
    public int updateUserTags(Long userId, List<String> tags) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID非法");
        }
        if (tags == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不能为空");
        }
        if (tags.size() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签数量不能超过10个");
        }
        for (String tag : tags) {
            if (tag.length() > 20) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "单个标签长度不能超过20");
            }
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setTags(GSON.toJson(tags));

        // 清除该用户的匹配缓存和推荐缓存
        String matchCacheKey = String.format("user:match:%s", userId);
        String recommendCacheKey = String.format("daZi:user:recommend:%s", userId);
        redisTemplate.delete(matchCacheKey);
        redisTemplate.delete(recommendCacheKey);

        return userMapper.updateById(updateUser);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            log.warn("获取登录用户失败，request对象为null");
            throw new BusinessException(ErrorCode.NO_AUTH, "请求对象无效，无法获取登录态");
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("获取登录用户失败，无有效Session，用户未登录");
            throw new BusinessException(ErrorCode.NO_AUTH, "请先完成登录");
        }
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            log.warn("获取登录用户失败，Session中无有效登录态");
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限，登录态已失效，请重新登录");
        }
        try {
            return (User) userObj;
        } catch (ClassCastException e) {
            log.error("登录态类型转换失败，用户对象格式异常", e);
            throw new BusinessException(ErrorCode.NO_AUTH, "登录态异常，请重新登录");
        }
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        if (num <= 0) {
            return Collections.emptyList();
        }
        String loginUserTags = loginUser.getTags();
        if (StringUtils.isBlank(loginUserTags)) {
            return Collections.emptyList();
        }

        Long loginUserId = loginUser.getId();

        // 1. 缓存检查
        String cacheKey = String.format("user:match:%s", loginUserId);
        ValueOperations<String, Object> valueOperation = redisTemplate.opsForValue();
        try {
            Object cacheObj = valueOperation.get(cacheKey);
            if (cacheObj instanceof List) {
                return (List<User>) cacheObj;
            }
        } catch (Exception e) {
            log.error("Redis缓存读取失败，key:{}", cacheKey, e);
        }

        // 2. 数据库查询与计算
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags")
                .isNotNull("tags")
                .ne("id", loginUserId);
        List<User> userList = this.list(queryWrapper);

        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> loginTagList = parseTagsToList(loginUserTags);
        if (loginTagList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map.Entry<User, Long>> userDistanceList = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            List<String> userTagList = parseTagsToList(userTags);
            long distance = AlgorithmUtils.editDistance(loginTagList, userTagList);
            userDistanceList.add(new AbstractMap.SimpleEntry<>(user, distance));
        }

        List<User> finalResult = userDistanceList.stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .limit(num)
                .map(Map.Entry::getKey)
                .map(this::getSafetyUser)
                .collect(Collectors.toList());

        // 3. 写入缓存
        try {
            valueOperation.set(cacheKey, finalResult, 10, TimeUnit.MINUTES);
            log.info("用户匹配缓存写入成功，key:{}，有效期10分钟", cacheKey);
        } catch (Exception e) {
            log.error("Redis缓存写入失败，key:{}", cacheKey, e);
        }

        return finalResult;
    }

    private List<String> parseTagsToList(String tagsJson) {
        try {
            return GSON.fromJson(tagsJson, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}