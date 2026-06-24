package com.dormitory.util;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 验证码工具类
 */
@Component
public class VerificationCodeUtil {

    /**
     * 生成6位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
