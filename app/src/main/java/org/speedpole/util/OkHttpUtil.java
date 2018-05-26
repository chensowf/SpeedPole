package org.speedpole.util;

import android.util.Log;

import org.speedpole.BuildConfig;

import java.util.HashMap;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Admin on 2018/4/4.
 */

public class OkHttpUtil {

    private static final String TAG = OkHttpClient.class.getSimpleName();

    private static OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .build();

    public static void request(String url, HashMap<String,String> params, Callback callback)
    {
        String data = null;
        if(params != null)
        {
            data = Util.genRequestParams(params);
            if(BuildConfig.DEBUG)
                Log.e("data",data);
            data = Util.encryptApiRequestParams(data);
        }
        if(data != null)
        {
            url = url + "?data="+data;
            if(BuildConfig.DEBUG)
                Log.e("url",url);
        }

        Request request = new Request
                .Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }

}
