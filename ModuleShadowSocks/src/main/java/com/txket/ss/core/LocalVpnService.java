package com.txket.ss.core;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import com.txket.ss.R;
import com.txket.ss.dns.DnsPacket;
import com.txket.ss.tcpip.CommonMethods;
import com.txket.ss.tcpip.IPHeader;
import com.txket.ss.tcpip.TCPHeader;
import com.txket.ss.tcpip.UDPHeader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 继承系统自带的VpnService
 */
public class LocalVpnService extends VpnService implements Runnable {

    public static LocalVpnService Instance;
    public static String ProxyUrl;
    public static boolean IsRunning = false;

    private static int ID;
    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> m_OnStatusChangedListeners = new ConcurrentHashMap<onStatusChangedListener, Object>();

    private Thread m_VPNThread;
    private ParcelFileDescriptor m_VPNInterface;
    private TcpProxyServer m_TcpProxyServer;
    private DnsProxy m_DnsProxy;
    private FileOutputStream m_VPNOutputStream;

    private byte[] m_Packet;
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private Handler m_Handler;
    private long m_SentBytes;
    private long m_ReceivedBytes;

    public LocalVpnService() {
        ID++;
        m_Handler = new Handler();
        m_Packet = new byte[20000];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
        Instance = this;

        System.out.printf("New VPNService(%d)\n", ID);
    }

    @Override
    public void onCreate() {
        System.out.printf("VPNService(%s) created.\n", ID);
        // Start a new session by creating a new thread.
        m_VPNThread = new Thread(this, "VPNServiceThread");
        m_VPNThread.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IsRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    public interface onStatusChangedListener {
        public void onStatusChanged(String status, Boolean isRunning);

        public void onLogReceived(String logString);
    }

    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.remove(listener);
        }
    }

    private void onStatusChanged(final String status, final boolean isRunning) {
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning);
                }
            }
        });
    }

    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
            }
        });
    }

    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getAppInstallID() {
        SharedPreferences preferences = getSharedPreferences("SmartProxy", MODE_PRIVATE);
        String appInstallID = preferences.getString("AppInstallID", null);
        if (appInstallID == null || appInstallID.isEmpty()) {
            appInstallID = UUID.randomUUID().toString();
            Editor editor = preferences.edit();
            editor.putString("AppInstallID", appInstallID);
            editor.apply();
        }
        return appInstallID;
    }

    String getVersionName() {
        try {
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            return "0.0";
        }
    }

    @Override
    public synchronized void run() {
        try {
            System.out.printf("VPNService(%s) work thread is runing...\n", ID);

            ProxyConfig.AppInstallID = getAppInstallID();//获取安装ID
            ProxyConfig.AppVersion = getVersionName();//获取版本号
            System.out.printf("AppInstallID: %s\n", ProxyConfig.AppInstallID);
            writeLog("Android version: %s", Build.VERSION.RELEASE);
            writeLog("App version: %s", ProxyConfig.AppVersion);


            ChinaIpMaskManager.loadFromFile(getResources().openRawResource(R.raw.ipmask));//加载中国的IP段，用于IP分流。
            waitUntilPreapred();//检查是否准备完毕。

            writeLog("Load config from file ...");
            try {
                ProxyConfig.Instance.loadFromFile(getResources().openRawResource(R.raw.config));   //加载配置
                writeLog("Load done");
            } catch (Exception e) {
                String errString = e.getMessage();
                if (errString == null || errString.isEmpty()) {
                    errString = e.toString();
                }
                writeLog("Load failed with error: %s", errString);
            }

            m_TcpProxyServer = new TcpProxyServer(0);    //建立本地代理服务器
            m_TcpProxyServer.start();
            writeLog("LocalTcpServer started.");

            m_DnsProxy = new DnsProxy();                        //建立本地dns服务器
            m_DnsProxy.start();
            writeLog("LocalDnsProxy started.");

            while (true) {
                if (IsRunning) {
                    //加载配置文件

                    writeLog("set org.qimi.shadowsocks/(http proxy)");
                    try {
                        ProxyConfig.Instance.m_ProxyList.clear();
                        ProxyConfig.Instance.addProxyToList(ProxyUrl);
                        writeLog("Proxy is: %s", ProxyConfig.Instance.getDefaultProxy());
                    } catch (Exception e) {
                        ;
                        String errString = e.getMessage();
                        if (errString == null || errString.isEmpty()) {
                            errString = e.toString();
                        }
                        IsRunning = false;
                        onStatusChanged(errString, false);
                        continue;
                    }
                    String welcomeInfoString = ProxyConfig.Instance.getWelcomeInfo();
                    if (welcomeInfoString != null && !welcomeInfoString.isEmpty()) {
                        writeLog("%s", ProxyConfig.Instance.getWelcomeInfo());
                    }
                    writeLog("Global mode is " + (ProxyConfig.Instance.globalMode ? "on" : "off"));

                    runVPN();
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
            writeLog("Fatal error: %s", e.toString());
        } finally {
            writeLog("App terminated.");
            dispose();
        }
    }

    /**
     * vpn跑起来吧
     * @throws Exception
     */
    private void runVPN() throws Exception {
        this.m_VPNInterface = establishVPN();
        this.m_VPNOutputStream = new FileOutputStream(m_VPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(m_VPNInterface.getFileDescriptor());
        int size = 0;
        while (size != -1 && IsRunning) {
            while ((size = in.read(m_Packet)) > 0 && IsRunning) {
                if (m_DnsProxy.Stopped || m_TcpProxyServer.Stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                onIPPacketReceived(m_IPHeader, size);
            }
            Thread.sleep(20);
        }
        in.close();
        disconnectVPN();
    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:      //tcp数据包
                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP) {
                    if (tcpHeader.getSourcePort() == m_TcpProxyServer.Port) {// 收到本地TCP服务器数据
                        NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                        if (session != null) {
                            ipHeader.setSourceIP(ipHeader.getDestinationIP());
                            tcpHeader.setSourcePort(session.RemotePort);
                            ipHeader.setDestinationIP(LOCAL_IP);

                            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                            m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                            m_ReceivedBytes += size;
                        } else {
                            System.out.printf("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
                        }
                    } else {    //源端口不是本地服务器的端口，所以可以判定是其他设备上的app的socket数据，进行转发到本地服务器操作

                        // 添加端口映射
                        int portKey = tcpHeader.getSourcePort();   //获取本地端口做一个端口映射操作
                        NatSession session = NatSessionManager.getSession(portKey);
                        if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {
                            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                        }

                        session.LastNanoTime = System.nanoTime();
                        session.PacketSent++;//注意顺序

                        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength(); //拿到原始数据长度
                        if (session.PacketSent == 2 && tcpDataSize == 0) {    //判别是否是第2个报文
                            return;//丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                        }

                        //分析数据，找到host
                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength();
                            String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);//如果是http或https协议，获取host
                            if (host != null) {
                                session.RemoteHost = host;
                            } else {
                                System.out.printf("No host name found: %s", session.RemoteHost);
                            }
                        }

                        // 转发给本地TCP服务器
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());   //把这个ip包的目标ip设置为源发送ip
                        ipHeader.setDestinationIP(LOCAL_IP);                 //目标ip设置为本地服务器ip
                        tcpHeader.setDestinationPort(m_TcpProxyServer.Port);  //目标端口设置为本地端口

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);   //从新计算ip数据包的校验和
                        m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);  //写进网卡
                        session.BytesSent += tcpDataSize;//注意顺序
                        m_SentBytes += size;
                    }
                }
                break;
            case IPHeader.UDP:
                // 转发DNS数据包：
                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {   //判定为dns数据包，因为dns端口为53，做dns数据包转发
                    m_DNSBuffer.clear();
                    m_DNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(m_DNSBuffer);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        m_DnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }

    private void waitUntilPreapred() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配置系统虚拟网卡
     * @return
     * @throws Exception
     */
    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(1500);   //设置Ip包大小
        if (ProxyConfig.IS_DEBUG)
            System.out.printf("setMtu: %d\n", ProxyConfig.Instance.getMTU());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);     //设置虚拟网卡ip地址和掩码
        if (ProxyConfig.IS_DEBUG)
            System.out.printf("addAddress: %s/%d\n", ipAddress.Address, ipAddress.PrefixLength);

        for (ProxyConfig.IPAddress dns : ProxyConfig.Instance.getDnsList()) {
            builder.addDnsServer(dns.Address);                                  //设置虚拟网卡dns地址，可以设置多个
            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addDnsServer: %s\n", dns.Address);
        }

        if (ProxyConfig.Instance.getRouteList().size() > 0) {
            for (ProxyConfig.IPAddress routeAddress : ProxyConfig.Instance.getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);   //设置要经过虚拟网卡路由地址，我这里设置的是0.0.0.0/0，默认设备上所有的ip地址都经过
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("addRoute: %s/%d\n", routeAddress.Address, routeAddress.PrefixLength);
            }
            builder.addRoute(CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16); //设置被污染ip的路由，要是设置上面设置不为0.0.0.0/0，才生效，这里可以忽略

            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addRoute for FAKE_NETWORK: %s/%d\n", CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16);
        } else {
            builder.addRoute("0.0.0.0", 0);
            if (ProxyConfig.IS_DEBUG)
                System.out.printf("addDefaultRoute: 0.0.0.0/0\n");
        }

        //设置app代理时才会用到，
        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<String>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                if (value.replaceAll("\\d", "").length() == 3){//防止IPv6地址导致问题
                    builder.addRoute(value, 32);
                } else {
                    builder.addRoute(value, 128);
                }
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("%s=%s\n", name, value);
            }
        }

        if (AppProxyManager.isLollipopOrAbove){
            if (AppProxyManager.Instance.proxyAppInfo.size() == 0){
                writeLog("Proxy All Apps");
            }
            for (AppInfo app : AppProxyManager.Instance.proxyAppInfo){
                builder.addAllowedApplication(getPackageName());//需要把自己加入代理，不然会无法进行网络连接
                try{
                    builder.addAllowedApplication(app.getPkgName());
                    writeLog("Proxy App: " + app.getAppLabel());
                } catch (Exception e){
                    e.printStackTrace();
                    writeLog("Proxy App Fail: " + app.getAppLabel());
                }
            }
        } else {
            writeLog("No Pre-App proxy, due to low Android version.");
        }

       /* Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);*/

        builder.setSession(ProxyConfig.Instance.getSessionName());    //可以认为设置虚拟网卡的名字
        ParcelFileDescriptor pfdDescriptor = builder.establish();    //开启虚拟网卡，返回一个文件描述符，用于网卡数据读写操作
        onStatusChanged("connected", true);
        return pfdDescriptor;
    }

    public void disconnectVPN() {
        try {
            if (m_VPNInterface != null) {
                m_VPNInterface.close();
                m_VPNInterface = null;
            }
        } catch (Exception e) {
            // ignore
        }
        onStatusChanged("disconnected", false);
        this.m_VPNOutputStream = null;
    }

    private synchronized void dispose() {
        // 断开VPN
        disconnectVPN();

        // 停止TcpServer
        if (m_TcpProxyServer != null) {
            m_TcpProxyServer.stop();
            m_TcpProxyServer = null;
            writeLog("LocalTcpServer stopped.");
        }

        // 停止DNS解析器
        if (m_DnsProxy != null) {
            m_DnsProxy.stop();
            m_DnsProxy = null;
            writeLog("LocalDnsProxy stopped.");
        }

        stopSelf();
        IsRunning = false;
  //      System.exit(0);
    }

    @Override
    public void onDestroy() {
        System.out.printf("VPNService(%s) destoried.\n", ID);
        if (m_VPNThread != null) {
            m_VPNThread.interrupt();
        }
    }

}
