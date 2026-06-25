package com.zhy.dazi.service;

import com.zhy.dazi.mapper.UserMapper;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import javax.annotation.Resource;

@SpringBootTest
public class InsertUsersTest {
    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(1000));

    @Resource
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    //@Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)

    @Test
    void testTags(){
        List<String> list = Arrays.asList("java", "大一", "男");
        List<String> list1 = Arrays.asList("java", "大一", "女");
        List<String> list2 = Arrays.asList("python", "大二", "女");

        //1
        int score1 = AlgorithmUtils.editDistance(list,list1);
        //3
        int score2 = AlgorithmUtils.editDistance(list,list2);
        System.out.println(score1);
        System.out.println(score2);


    }
    @Test
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


    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        //分十组
        int j=0;
        for (int i=0;i < 10;i++){
            List<User> userList = new ArrayList<>();
            while (true) {
                for (; j < 1000; j++) {
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
                    if (j % 10000 == 0) {
                        break;

                    }
                }
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    System.out.println("threadName" + Thread.currentThread().getName());
                    userService.saveBatch(userList, 10000);
                });
//                futureList.add(future);
            }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{}).join());
//        //20秒1万条
//        stopWatch.stop();
//        stopWatch.getTotalTimeMillis();
    }
}
}
