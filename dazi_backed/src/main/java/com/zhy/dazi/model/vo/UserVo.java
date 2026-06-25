package com.zhy.dazi.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类
 */
@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 189906686785785785L;
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;
    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 标签列表json
     */
    private String tags;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 星球编号
     */
    private String planetCode;


}
