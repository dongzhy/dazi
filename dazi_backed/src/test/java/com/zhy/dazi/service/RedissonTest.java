package com.zhy.dazi.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class RedissonTest {
    // 注入配置类中创建的RedissonClient Bean
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // 1. 本地JVM内存中的List
        List<String> localList = new ArrayList<String>(); // 补充泛型类型
        localList.add("zhy");
        System.out.println("本地list：" + localList.get(0));

        // 2. Redis内存中的分布式List（Redisson的RList）
        RList<String> redisList = redissonClient.getList("test-list");
        redisList.add("zhy");
        System.out.println("Redis分布式list：" + redisList.get(0));

        //map
        Map<String,Integer> map = new HashMap<>();
        map.put("zhy",10);
        map.get("zhy");

        RMap<Object, Object> map1 = redissonClient.getMap("test-map");

    }
}
