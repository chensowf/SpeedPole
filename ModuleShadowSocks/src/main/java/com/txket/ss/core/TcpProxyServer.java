package com.txket.ss.core;


import com.txket.ss.tcpip.CommonMethods;
import com.txket.ss.tunnel.Tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *  这是一个tcp本地代理服务器，用于接受和转发拦截本地数据包得中转站。并且使用了nio socket技术进行
 *  多socket并发编程技术
 */
public class TcpProxyServer implements Runnable {

    public boolean Stopped;
    public short Port;

    Selector m_Selector;
    ServerSocketChannel m_ServerSocketChannel;
    Thread m_ServerThread;

    public TcpProxyServer(int port) throws IOException {
        m_Selector = Selector.open();   //获取一个选择器
        m_ServerSocketChannel = ServerSocketChannel.open();   //打开一个服务器socket通道
        m_ServerSocketChannel.configureBlocking(false);      //设置非阻塞模式
        m_ServerSocketChannel.socket().bind(new InetSocketAddress(port));  //绑定一个给定得本地监听端口
        m_ServerSocketChannel.register(m_Selector, SelectionKey.OP_ACCEPT);  //注册当前为服务器监听请求建立连接事件
        this.Port = (short) m_ServerSocketChannel.socket().getLocalPort();
        System.out.printf("AsyncTcpServer listen on %d success.\n", this.Port & 0xFFFF);
    }

    public void start() {
        m_ServerThread = new Thread(this);
        m_ServerThread.setName("TcpProxyServerThread");
        m_ServerThread.start();
    }

    public void stop() {
        this.Stopped = true;
        if (m_Selector != null) {
            try {
                m_Selector.close();
                m_Selector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (m_ServerSocketChannel != null) {
            try {
                m_ServerSocketChannel.close();
                m_ServerSocketChannel = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                m_Selector.select();
                Iterator<SelectionKey> keyIterator = m_Selector.selectedKeys().iterator();   //获取通道迭代器用于监听事件

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isReadable()) {
                                ((Tunnel) key.attachment()).onReadable(key);   //监听可读事件
                            } else if (key.isWritable()) {
                                ((Tunnel) key.attachment()).onWritable(key);   //监听可写事件
                            } else if (key.isConnectable()) {
                                ((Tunnel) key.attachment()).onConnectable();   //监听连接上事件
                            } else if (key.isAcceptable()) {
                                onAccepted(key);                             //收到建立连接事件，进行数据通道建立处理等操作
                            }
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop();
            System.out.println("TcpServer thread exited.");
        }
    }

    /**
     * 根据本地端口号获取一个NatSession设置
     * @param localChannel
     * @return
     */
    InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            if (ProxyConfig.Instance.needProxy(session.RemoteHost, session.RemoteIP)) {
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("%d/%d:[PROXY] %s=>%s:%d\n", NatSessionManager.getSessionCount(), Tunnel.SessionCount, session.RemoteHost, CommonMethods.ipIntToString(session.RemoteIP), session.RemotePort & 0xFFFF);
                return InetSocketAddress.createUnresolved(session.RemoteHost, session.RemotePort & 0xFFFF);
            } else {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }

    /**
     *
     * @param key
     */
    void onAccepted(SelectionKey key) {
        Tunnel localTunnel = null;
        try {
            SocketChannel localChannel = m_ServerSocketChannel.accept();    //获取一个请求连接socket通道
            localTunnel = TunnelFactory.wrap(localChannel, m_Selector);      //建立一个通往本地虚拟网卡数据通道

            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                Tunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, m_Selector);
                remoteTunnel.setBrotherTunnel(localTunnel);//关联兄弟
                localTunnel.setBrotherTunnel(remoteTunnel);//关联兄弟
                remoteTunnel.connect(destAddress);//开始连接
            } else {
                LocalVpnService.Instance.writeLog("Error: socket(%s:%d) target host is null.", localChannel.socket().getInetAddress().toString(), localChannel.socket().getPort());
                localTunnel.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LocalVpnService.Instance.writeLog("Error: remote socket create failed: %s", e.toString());
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}
