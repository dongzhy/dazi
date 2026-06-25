package com.zhy.dazi;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 *
 *
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@MapperScan("com.zhy.dazi.mapper")
public class DaZiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaZiApplication.class, args);
    }

}

