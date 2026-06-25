package com.zhy.dazi.once;

import com.zhy.dazi.mapper.UserMapper;
import com.zhy.dazi.model.domain.User;
import org.springframework.util.StopWatch;


import javax.annotation.Resource;

/**
 * 批量插入用户
 */


public class InsertUsers {
    @Resource
    private UserMapper userMapper;
    //@Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)

    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i=0;i<INSERT_NUM;i++){
            User user = new User();
            user.setUsername("海勇");
            user.setUserAccount("haiyong");
            user.setGender(0);
            user.setUserPassword("123456");
            user.setEmail("123@qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            userMapper.insert(user);
        }
        stopWatch.stop();
        stopWatch.getTotalTimeMillis();
    }

    public static void main(String[] args) {
        new InsertUsers().doInsertUsers();
    }

}
