package com.zhy.dazi.model.request;

import lombok.Data;

import java.util.List;

/**
 * 用户标签更新请求
 */
@Data
public class UserTagUpdateRequest {
    private List<String> tags;
}