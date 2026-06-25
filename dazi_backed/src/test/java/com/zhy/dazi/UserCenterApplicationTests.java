package com.zhy.dazi;

import com.zhy.dazi.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.NoSuchAlgorithmException;

/**
 * 启动类测试
 *
 * @author <a href="https://github.com/donghaiyong">程序员zhy</a>

 */
@SpringBootTest
class DaZiApplicationTests {
    @Autowired
    private UserServiceImpl userServiceImpl;

    // https://yupi.icu/

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String newPassword = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(newPassword);
    }


    @Test
    void contextLoads() {

    }


}

