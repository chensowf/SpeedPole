package org.speedpole.util;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by Admin on 2018/5/26.
 */

public class Util {

    public static String encryptApiRequestParams(String params)
    {
        byte[] buffer = Encryptor.encrypt(Encryptor.key, Encryptor.iv, params.getBytes(), Encryptor.NoPadding);
        params = genSafeUrlStr(buffer);
        return params;
    }

    /**
     * 对加密后的上传数据进行处理成安全得URL请求参数
     * @param buffer
     * @return
     */
    public static String genSafeUrlStr(byte[] buffer)
    {
        buffer = Base64.encode(buffer, Base64.DEFAULT);
        try {
            return URLEncoder.encode(new String(buffer),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String genRequestParams(HashMap<String, String> paramsMap)
    {
        int pos = 0;
        StringBuilder params = new StringBuilder();
        for(String key : paramsMap.keySet())
        {
            if(pos > 0)
                params.append("&");
            params.append(String.format("%s=%s",key,paramsMap.get(key)));
            pos++;
        }
        return params.toString();
    }

    /**
     * 2维码生成工具
     * @param content
     * @param width
     * @param height
     * @return
     */
    public Bitmap createQRBitmap(String content,int width, int height)
    {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType,String> config = new HashMap<>();
        config.put(EncodeHintType.CHARACTER_SET,"utf-8");
        try
        {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE,width,height,config);
            int pixels[] = new int[width*height];
            for(int i = 0; i < height; i++)
            {
                for(int j = 0; j < width; j++)
                {
                    if(encode.get(j,i))
                        pixels[i*width+j] = 0x00000000;
                    else
                        pixels[i*width+j] = 0xffffffff;
                }
            }
            return Bitmap.createBitmap(pixels,width,width,height,height,Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * gzip压缩数据解压接口
     * @param bytes
     * @return
     */
    public static byte[] uncompress(byte[] bytes) {
        if(bytes == null || bytes.length == 0){
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[128];
            int n;
            while((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
