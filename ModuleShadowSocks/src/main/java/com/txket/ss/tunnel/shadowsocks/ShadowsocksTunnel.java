package com.txket.ss.tunnel.shadowsocks;

import android.os.SystemClock;
import android.util.Log;

import com.txket.ss.tunnel.Tunnel;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;

/**
 * ss协议数据通信通道
 */
public class ShadowsocksTunnel extends Tunnel {

    private ICrypt m_Encryptor;
    private ShadowsocksConfig m_Config;
    private boolean m_TunnelEstablished;

    public ShadowsocksTunnel(ShadowsocksConfig config, Selector selector) throws Exception {
        super(config.ServerAddress, selector);
        m_Config = config;
        m_Encryptor = CryptFactory.get(m_Config.EncryptMethod, m_Config.Password);

    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {

        buffer.clear();
        // https://shadowsocks.org/en/spec/protocol.html
        /**
         * ss协议头格式，0x03开头,跟着后面8位的域名或者ip的长度，后面再跟着域名或者Ip的byte，
         * 后面再跟着一个short类型的端口
         */
        buffer.put((byte) 0x03);//ss协议开头标识
        byte[] domainBytes = m_DestAddress.getHostName().getBytes();
        buffer.put((byte) domainBytes.length);//domain length;
        buffer.put(domainBytes);
        buffer.putShort((short) m_DestAddress.getPort());
        buffer.flip();
        byte[] _header = new byte[buffer.limit()];
        buffer.get(_header);

        buffer.clear();
        buffer.put(m_Encryptor.encrypt(_header));         //协议头数据进行加密
        buffer.flip();

        if (write(buffer, true)) {
            m_TunnelEstablished = true;
            onTunnelEstablished();
        } else {
            m_TunnelEstablished = true;
            this.beginReceive();
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return m_TunnelEstablished;
    }

    /**
     * 发送之前进行流数据加密
     * @param buffer
     * @throws Exception
     */
    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        byte[] newbytes = m_Encryptor.encrypt(bytes);

        buffer.clear();
        buffer.put(newbytes);
        buffer.flip();
    }

    /**
     * 收到回来的数据进行解密
     * @param buffer
     * @throws Exception
     */
    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        byte[] newbytes = m_Encryptor.decrypt(bytes);
        buffer.clear();
        buffer.put(newbytes);
        buffer.flip();
    }

    @Override
    protected void onDispose() {
        m_Config = null;
        m_Encryptor = null;
    }

}
