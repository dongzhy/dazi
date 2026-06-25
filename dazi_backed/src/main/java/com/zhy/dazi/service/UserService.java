package com.zhy.dazi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.model.request.UserUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/donghaiyong">程序员zhy</a>

 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);
    /**
     * 根据标签搜索用户
     */
    List<User> SearchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param userUpdateRequest
     * @return
     */
    int updateUser(UserUpdateRequest userUpdateRequest, User loginUser);


    /**
     * 修改标签
     * @param userId
     * @param tags
     * @return
     */
    int updateUserTags(Long userId, List<String> tags);

    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
   boolean isAdmin(HttpServletRequest request);
    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */


    List<User> matchUsers(long num, User loginUser);
}
