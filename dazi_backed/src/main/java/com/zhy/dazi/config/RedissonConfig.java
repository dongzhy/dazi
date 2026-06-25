package com.zhy.dazi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置
 */

/**
 * 正确的Redisson配置类
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    // 补充password属性，绑定配置文件中的redis密码
    private String host;
    private Integer port; // 改为Integer更贴合配置规范
    private String password;
    private Integer database; // 绑定数据库编号，避免硬编码

    /**
     * 向Spring容器注入RedissonClient Bean（供业务/测试类注入使用）
     * @return RedissonClient 实例
     */
    @Bean(destroyMethod = "shutdown") // 容器销毁时关闭Redisson客户端，释放资源
    public RedissonClient redissonClient() {
        // 1. 创建Redisson配置对象
        Config config = new Config();

        // 2. 拼接Redis地址（Redisson支持redis:// 格式）
        String redisAddress = String.format("redis://%s:%d", host, port);

        // 3. 配置单节点Redis（解决认证、数据库选择问题）
        config.useSingleServer()
                .setAddress(redisAddress) // 设置Redis地址
                .setPassword(password)    // 设置Redis密码（无密码则传null或不调用该方法）
                .setDatabase(database);   // 设置数据库编号（替代硬编码的3）

        // 4. 创建并返回RedissonClient实例（无需类型转换，直接返回）
        return Redisson.create(config);
    }
}
