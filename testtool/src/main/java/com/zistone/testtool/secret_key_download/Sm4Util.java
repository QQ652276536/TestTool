package com.zistone.testtool.secret_key_download;

import com.zistone.mylibrary.util.MyConvertUtil;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author LiWei
 * @date 2020/8/31 10:42
 * @email 652276536@qq.com
 */
public class Sm4Util {
    public static final int KEY_SIZE = 128;

    public static final String ALGORITHM = "SM4";

    public static final String IV = "根据自己的项目定义";

    public static final String ALGORITHM_ECB_PADDING = "SM4/ECB/NoPadding";

    public static final String ALGORITHM_CBC_PADDING = "SM4/CBC/PKCS7Padding";


    public static byte[] encryptECBToByte(byte[] data, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] enBytes = cipher.doFinal(data);
            return enBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密
     *
     * @param data 数据
     * @param keys 密钥
     * @return 16进制字符串
     */
    public static byte[] encryptECB(byte[] data, byte[] keys) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_ECB_PADDING, new BouncyCastleProvider());
            Key key = new SecretKeySpec(keys, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptECBToByte(byte[] data, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] deBytes = cipher.doFinal(data);
            return deBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param data 数据
     * @param keys 密钥
     * @return 16进制字符串
     */
    public static byte[] decryptECB(byte[] data, byte[] keys) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_ECB_PADDING, new BouncyCastleProvider());
            Key key = new SecretKeySpec(keys, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptCBCToByte(byte[] data, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            AlgorithmParameterSpec paramSpec = ivSpec;
            cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            byte[] enBytes = cipher.doFinal(data);
            return enBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptCBC(String data, String keyStr, String encoding) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            AlgorithmParameterSpec paramSpec = ivSpec;
            cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            byte[] enBytes = cipher.doFinal(data.getBytes(encoding));
            //            Base64Encoder base64Encoder = new Base64Encoder();
            //            return base64Encoder.encode(enBytes);
            return MyConvertUtil.ByteArrayToHexStr(enBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptCBCToByte(byte[] data, String keyStr) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            AlgorithmParameterSpec paramSpec = ivSpec;
            cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            byte[] deBytes = cipher.doFinal(data);
            return deBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptCBC(String data, String keyStr, String encoding) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_CBC_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            Key key = new SecretKeySpec(keyStr.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
            AlgorithmParameterSpec paramSpec = ivSpec;
            cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            //            byte[] deBytes = cipher.doFinal(Base64.decodeBase64(data));
            //            Base64Encoder base64Encoder = new Base64Encoder();
            byte[] deBytes = cipher.doFinal(MyConvertUtil.HexStrToByteArray(data));
            return new String(deBytes, encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
