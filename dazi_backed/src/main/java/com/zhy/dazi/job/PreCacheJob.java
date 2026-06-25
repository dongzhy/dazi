package com.zhy.dazi.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhy.dazi.mapper.UserMapper;
import com.zhy.dazi.model.domain.User;
import com.zhy.dazi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserMapper userMapper;

    //重点用户
    private List<Long> mainUserList= Arrays.asList(1L);

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    //每天执行，加载预热推荐用户
    @Scheduled(cron = "0 0 12 * * ?")
    public  void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("dazi:precachjob:docache:lock");
        try {
            //只有一个线程能获取锁
            if(lock.tryLock(0,30000,TimeUnit.MILLISECONDS)){
                for (Long userId : mainUserList) {
                    String redisKey = String.format("daZi:user:recommend:%s",userId);
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    ValueOperations<Object, Object> ValueOperations = redisTemplate.opsForValue();
                    try{
                        ValueOperations.set(redisKey,userPage,30000, TimeUnit.MICROSECONDS);
                    }catch(Exception e){
                        log.error("redis set key error");
                    }
                }


            }
        } catch (InterruptedException e) {
            log.error("做缓存失败 error",e);
        }finally {
            //只能释放自己锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }


    }

}
