package com.zhy.dazi.service;

import com.zhy.dazi.model.domain.User;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("key1","value");
        valueOperations.set("key2","value2");
        valueOperations.set("key3","value3");
        User user = new User();
        user.setId(1L);
        user.setUsername("zhy");
        valueOperations.set("key4",user);
        //查
        Object key1 = valueOperations.get("key1");
        Assertions.assertTrue("value".equals((String)key1));


    }
}
