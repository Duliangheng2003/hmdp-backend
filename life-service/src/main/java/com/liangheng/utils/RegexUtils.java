package com.liangheng.utils;

import cn.hutool.core.util.StrUtil;


public class RegexUtils {
    /**
     * 是否是无效手机格式
     */
    public static boolean isPhoneInvalid(String phone){
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }

    /**
     * 是否是无效邮箱格式
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 是否是无效密码格式
     */
    public static boolean isPassWordInvalid(String password){
        return mismatch(password, RegexPatterns.PASSWORD_REGEX);
    }

    // 校验是否不符合正则格式
    private static boolean mismatch(String str, String regex){
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }
}


