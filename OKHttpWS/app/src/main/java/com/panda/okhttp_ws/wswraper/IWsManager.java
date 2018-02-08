package com.panda.okhttp_ws.wswraper;

import okhttp3.WebSocket;
import okio.ByteString;

/**
 * Created by panda on 2018/2/7.
 */
public interface IWsManager {
    /**
     * 获取到websocket对象
     *
     * @return
     */
    WebSocket webSocket();

    /**
     * 发起连接
     */
    void wsConnect();

    /**
     * 关闭连接
     */
    void wsDisConnect();

    /**
     * 发送文本消息
     *
     * @param message
     * @return
     */
    boolean sendMessage(String message);

    /**
     * 发送二进制消息
     *
     * @param message
     * @return
     */
    boolean sendMessage(ByteString message);

    /**
     * 连接的状态
     *
     * @return
     */
    WsStatus status();
}
