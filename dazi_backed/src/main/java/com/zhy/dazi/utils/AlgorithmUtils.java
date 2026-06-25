package com.zhy.dazi.utils;

import java.util.List;
import java.util.Objects;

public class AlgorithmUtils {
    // 动态规划求解标签列表的编辑距离（Levenshtein距离）
    public static int editDistance(List<String> tagList1, List<String> tagList2) {
        // 处理空列表的边界情况（避免空指针）
        if (tagList1 == null || tagList2 == null) {
            return tagList1 == null ? (tagList2 == null ? 0 : tagList2.size()) : tagList1.size();
        }

        int m = tagList1.size();
        int n = tagList2.size();

        // dp[i][j]：将tagList1的前i个标签转为tagList2的前j个标签的最小编辑次数
        int[][] dp = new int[m + 1][n + 1];

        // 初始化边界：空列表转成前j个标签，需要插入j次
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i; // tagList2为空，删除tagList1的前i个标签
        }
        // 初始化边界：前i个标签转成空列表，需要删除i次
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j; // tagList1为空，插入tagList2的前j个标签
        }

        // 填充dp数组（核心逻辑修复）
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // 修复点：去掉!，判断两个标签是否相等
                if (Objects.equals(tagList1.get(i - 1), tagList2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1]; // 相等，无需操作
                } else {
                    // 不相等，取删除、插入、替换的最小成本 + 1（替换成本）
                    dp[i][j] = Math.min(dp[i - 1][j], // 删除tagList1的第i个标签
                            Math.min(dp[i][j - 1], // 插入tagList2的第j个标签到tagList1
                                    dp[i - 1][j - 1])) + 1; // 替换tagList1的第i个标签为tagList2的第j个
                }
            }
        }

        // 返回最终编辑距离
        return dp[m][n];
    }
}