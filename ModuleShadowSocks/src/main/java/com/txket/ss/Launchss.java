package com.txket.ss;

import android.app.Activity;
import android.content.Intent;

import com.txket.ss.core.AppProxyManager;
import com.txket.ss.core.LocalVpnService;

/**
 * Created by Admin on 2018/4/2.
 */

public class Launchss {

    public final static int LaunchSSCode = 0x53;
    /**
     *  加密方法：密码@服务器@端口
     */
    private String proxyUrl = "ss://%s:%s@%s:%s";
    private Activity activity;

    public void startSSVpn(Activity activity, String method, String password, String host, int port
            , String[] packNames)
    {
        String proxy = String.format(proxyUrl,method,password,host,port);
        LocalVpnService.ProxyUrl = proxy;
        AppProxyManager appProxyManager = new AppProxyManager(activity);
        if(packNames!=null)
            appProxyManager.addProxyApp(packNames);
        this.activity = activity;
        Intent intent = LocalVpnService.prepare(activity);
        if(intent == null)
        {
            startSSVpn();
        }
        else
        {
            activity.startActivityForResult(intent,LaunchSSCode);
        }
    }

    public void startSSVpn()
    {
        LocalVpnService.IsRunning = true;
        activity.startService(new Intent(activity,LocalVpnService.class));
    }

    public void stopSSVpn()
    {
        if(LocalVpnService.IsRunning) {
            LocalVpnService.IsRunning = false;
            LocalVpnService.Instance.disconnectVPN();
            activity.stopService(new Intent(activity, LocalVpnService.class));
        }
    }

}
