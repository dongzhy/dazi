package com.zhy.dazi.model.request;

import lombok.Data;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest {
    private Long id;
    private String username;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private String email;
    private String tags;
}

