package top.yms.note.conpont.crawler.impl;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyFactory {

    public static Proxy getProxy() {
        // 示例：HTTP 代理
        return new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress("127.0.0.1", 10809)
        );
    }

    private static final Proxy SOCKS_PROXY =
            new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress("127.0.0.1", 10808)
            );

    private ProxyFactory() {}

    public static Proxy socks() {
        return SOCKS_PROXY;
    }

    private static final Proxy HTTP_PROXY =
            new Proxy(
                    Proxy.Type.HTTP,
                    new InetSocketAddress("127.0.0.1", 10809)
            );

    public static Proxy http() {
        return HTTP_PROXY;
    }
}
