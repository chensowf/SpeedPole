package com.txket.ss.core;


import com.txket.ss.tunnel.Config;
import com.txket.ss.tunnel.RawTunnel;
import com.txket.ss.tunnel.Tunnel;
import com.txket.ss.tunnel.httpconnect.HttpConnectConfig;
import com.txket.ss.tunnel.httpconnect.HttpConnectTunnel;
import com.txket.ss.tunnel.shadowsocks.ShadowsocksConfig;
import com.txket.ss.tunnel.shadowsocks.ShadowsocksTunnel;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfig.Instance.getDefaultTunnelConfig(destAddress);
            if (config instanceof HttpConnectConfig) {
                return new HttpConnectTunnel((HttpConnectConfig) config, selector);
            } else if (config instanceof ShadowsocksConfig) {
                return new ShadowsocksTunnel((ShadowsocksConfig) config, selector);
            }
            throw new Exception("The config is unknow.");
        } else {
            return new RawTunnel(destAddress, selector);
        }
    }

}
