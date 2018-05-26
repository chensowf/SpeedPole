package org.speedpole.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by miqi on 2017/7/12.
 */

public class Encryptor {

    public static final String key = "345ac9226c94523aeb2be459a5d63ce3";
    public static final String iv = "97043ef635f60558c5c9169252b3bdc9";
    /**
     * 补码方式为nopadding
     */
    public static final String NoPadding = "AES/CBC/NoPadding";

    /**
     * 补码方式为PKCS5PADDING
     */
    public static final String PKCS5Padding = "AES/CBC/PKCS5PADDING";

    /**
     * 补码方式为PKCS7PADDING
     */
    public static final String PKCS7Padding = "AES/CBC/PKCS7PADDING";

    /**
     * hex转为byte参数
     * @param hexStr
     * @return
     */
    public static byte[] hexStr2Bytes(String hexStr)
    {
        hexStr = hexStr.toLowerCase();
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length()/2];
        int n;
        int j;
        for(int i = 0; i < bytes.length; i++)
        {
            if(hexs[2*i] < 0x3a)
            {
                n = hexs[2*i] - 0x30;//3 12312  16
            }
            else
            {
                n = hexs[2*i] - 0x57;//10 b11 acbd
            }

            if(hexs[2*i+1] < 0x3a)
            {
                j = hexs[2*i+1] - 0x30;
            }
            else
            {
                j = hexs[2*i+1] - 0x57;
            }
            bytes[i] = (byte)(n*16+j);//byte
        }
        return bytes;
    }

    /**
     * aes-128-cbc解密
     * @param key 解密密钥
     * @param ivStr 解密要用到得补全码
     * @param data 要进行解密得数据
     * @return 解密后得数据
     */
    public static byte[] decrypt(String key, String ivStr, byte[] data, String paddingType)
    {
        byte[] bkeys = hexStr2Bytes(key);
        byte[] bivs = hexStr2Bytes(ivStr);
        try{
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bivs);
            SecretKeySpec secretKeySpec = new SecretKeySpec(bkeys,"AES");
            Cipher cipher = Cipher.getInstance(paddingType);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] original = cipher.doFinal(fixDataLength(data));
            int n = 0;
            for(int i = original.length-1; i > original.length-30; i--)
            {
                if(original[i] == 0)
                {
                    n++;
                }
            }
            byte[] originall = new byte[original.length-n];
            System.arraycopy(original,0,originall,0,original.length-n);
            return originall;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt1(String key, String ivStr, byte[] data, String paddingType)
    {
        byte[] bkeys = hexStr2Bytes(key);
        byte[] bivs = hexStr2Bytes(ivStr);
        try{
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bivs);
            SecretKeySpec secretKeySpec = new SecretKeySpec(bkeys,"AES");
            Cipher cipher = Cipher.getInstance(paddingType);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            return cipher.doFinal(fixDataLength(data));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * aes-128-cbc加密
     * @param key 加密得密钥
     * @param ivStr 加密得补全码
     * @param data 要加密得数据
     * @return 加密后得数据
     */
    public static byte[] encrypt(String key, String ivStr, byte[] data, String paddingType)
    {
        byte[] bkeys = hexStr2Bytes(key);
        byte[] bivs = hexStr2Bytes(ivStr);
        try{
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bivs);
            SecretKeySpec secretKeySpec = new SecretKeySpec(bkeys,"AES");

            Cipher cipher = Cipher.getInstance(paddingType);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(fixDataLength(data));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 流数据数据补全，不够128位得倍数要补全128位的倍数
     * @param data
     * @return
     */
    public static byte[] fixDataLength(byte[] data)
    {
        int fixLength = data.length%16;
        if(fixLength == 0)
        {
            return data;
        }
        byte[] fixData = new byte[data.length + (16-fixLength)];
        for(int i = fixData.length-1; i >= data.length; i--)
        {
            fixData[i] = (byte) 0;
        }
        System.arraycopy(data,0,fixData,0,data.length);
        return fixData;
    }
}
