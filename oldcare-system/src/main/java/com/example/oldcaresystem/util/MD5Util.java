package com.example.oldcaresystem.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5密码加密工具类
 */
public class MD5Util {

    /**
     * 将密码进行MD5加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不存在", e);
        }
    }

    /**
     * 验证密码是否正确
     *
     * @param password      原始密码
     * @param encryptedPassword 加密后的密码
     * @return true-密码正确，false-密码错误
     */
    public static boolean verify(String password, String encryptedPassword) {
        return encrypt(password).equals(encryptedPassword);
    }
}
