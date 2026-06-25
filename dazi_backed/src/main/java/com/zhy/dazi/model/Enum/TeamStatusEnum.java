package com.zhy.dazi.model.Enum;

import lombok.Getter;

/**
 * 队伍状态枚举
 */
@Getter
public enum TeamStatusEnum {
    /**
     * 公开
     */
    PUBLIC(0, "公开"),
    /**
     * 私有
     */
    PRIVATE(1, "私有"),
    /**
     * 加密（注意命名：和你代码里的SECRET保持一致）
     */
    SECRET(2, "加密");

    /**
     * 状态值（前端传递的数字）
     */
    private final Integer value;
    /**
     * 状态描述
     */
    private final String desc;

    // 构造方法
    TeamStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据value值获取枚举（核心方法：前端传数字时靠这个匹配）
     */
    public static TeamStatusEnum getEnumByValue(Integer value) {
        // 空值防护
        if (value == null) {
            return null;
        }
        // 遍历枚举匹配value
        for (TeamStatusEnum enumObj : TeamStatusEnum.values()) {
            if (enumObj.getValue().equals(value)) {
                return enumObj;
            }
        }
        // 匹配不到返回null
        return null;
    }
}