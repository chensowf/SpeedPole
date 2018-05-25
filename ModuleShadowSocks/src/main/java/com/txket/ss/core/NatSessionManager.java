package com.txket.ss.core;

import android.util.SparseArray;

import com.txket.ss.tcpip.CommonMethods;


/**
 * NatSession管理器,保存所有经过虚拟网卡的tcp连接的穿透session配置
 */
public class NatSessionManager {

    static final int MAX_SESSION_COUNT = 600;        //最大保存session数量
    static final long SESSION_TIMEOUT_NS = 60 * 1000000000L;       //过时时间
    static final SparseArray<NatSession> Sessions = new SparseArray<NatSession>();

    /**
     * 根据端口号获取一个session
     * @param portKey
     * @return
     */
    public static NatSession getSession(int portKey) {
        NatSession session = Sessions.get(portKey);
        if (session!=null) {
            session.LastNanoTime = System.nanoTime();
        }
        return Sessions.get(portKey);
    }

    public static int getSessionCount() {
        return Sessions.size();
    }

    /**
     * 清理过时的配置
     */
    static void clearExpiredSessions() {
        long now = System.nanoTime();
        for (int i = Sessions.size() - 1; i >= 0; i--) {
            NatSession session = Sessions.valueAt(i);
            if (now - session.LastNanoTime > SESSION_TIMEOUT_NS) {
                Sessions.removeAt(i);
            }
        }
    }

    /**
     * 根据本地端口，远程目标ip，远程目标端口号生成一个穿透session
     * @param portKey
     * @param remoteIP
     * @param remotePort
     * @return
     */
    public static NatSession createSession(int portKey, int remoteIP, short remotePort) {
        if (Sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions();//清理过期的会话。
        }

        NatSession session = new NatSession();
        session.LastNanoTime = System.nanoTime();
        session.RemoteIP = remoteIP;
        session.RemotePort = remotePort;
        session.RemotePort = remotePort;

        if (ProxyConfig.isFakeIP(remoteIP)) {
            session.RemoteHost = DnsProxy.reverseLookup(remoteIP);
        }

        if (session.RemoteHost == null) {
            session.RemoteHost = CommonMethods.ipIntToString(remoteIP);
        }
        Sessions.put(portKey, session);
        return session;
    }
}
