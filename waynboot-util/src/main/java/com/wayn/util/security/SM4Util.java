package com.wayn.util.security;

import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.wayn.util.exception.BusinessException;
import com.wayn.util.enums.ReturnCodeEnum;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SM4加密工具类
 * 国密SM4对称加密算法实现
 */
public class SM4Util {

    // SM4加密算法实例
    private static final SymmetricCrypto SM4_CRYPTO;

    // 密钥长度必须为16字节
    private static final String SM4_KEY = "waynboot-mall-sm4-key"; // 实际应用中应从安全存储获取

    static {
        try {
            // 初始化SM4加密实例，使用ECB模式和PKCS7填充
            SM4_CRYPTO = new SM4(SM4_KEY.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.SYSTEM_ERROR, "SM4加密初始化失败: " + e.getMessage());
        }
    }

    /**
     * SM4加密
     *
     * @param data 待加密数据
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            byte[] encrypted = SM4_CRYPTO.encrypt(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.SYSTEM_ERROR, "SM4加密失败: " + e.getMessage());
        }
    }

    /**
     * SM4解密
     *
     * @param encryptedData 加密后的Base64字符串
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return SM4_CRYPTO.decryptStr(decoded);
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.SYSTEM_ERROR, "SM4解密失败: " + e.getMessage());
        }
    }

    /**
     * SM4加密（字节数组）
     *
     * @param data 待加密字节数组
     * @return 加密后的字节数组
     */
    public static byte[] encrypt(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return SM4_CRYPTO.encrypt(data);
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.SYSTEM_ERROR, "SM4加密失败: " + e.getMessage());
        }
    }

    /**
     * SM4解密（字节数组）
     *
     * @param encryptedData 加密后的字节数组
     * @return 解密后的字节数组
     */
    public static byte[] decrypt(byte[] encryptedData) {
        if (encryptedData == null) {
            return null;
        }
        try {
            return SM4_CRYPTO.decrypt(encryptedData);
        } catch (Exception e) {
            throw new BusinessException(ReturnCodeEnum.SYSTEM_ERROR, "SM4解密失败: " + e.getMessage());
        }
    }
}
