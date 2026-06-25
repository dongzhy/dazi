package com.zhy.dazi.model.request;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍
 * @TableName team
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long teamId;
}
