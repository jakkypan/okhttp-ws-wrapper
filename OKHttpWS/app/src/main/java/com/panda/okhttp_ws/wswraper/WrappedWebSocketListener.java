package com.panda.okhttp_ws.wswraper;

import okhttp3.Response;
import okio.ByteString;

/**
 * 对{@link okhttp3.WebSocketListener}的中间封装
 *
 * Created by panda on 2018/2/7.
 */
public abstract class WrappedWebSocketListener {
    /**
     * ws连接打开
     *
     * @param response
     */
    public void onOpen(Response response) {

    }

    /**
     * ws连接的重试
     *
     * @param count 第几次重试
     */
    public void onRetry(int count) {

    }

    /**
     * 新消息到达
     * @param text
     */
    public void onMessage(String text) {

    }

    /**
     * 新消息到达，二进制
     * @param text
     */
    public void onMessage(ByteString text) {

    }

    /**
     * 连接关闭中
     *
     * @param code
     * @param reason
     */
    public void onClosing(int code, String reason) {

    }

    /**
     * 连接已经关闭
     *
     * @param code
     * @param reason
     */
    public void onClosed(int code, String reason) {

    }

    /**
     * 连接中的各种失败
     *
     * @param t
     * @param response
     */
    public void onFailure(Throwable t, Response response) {

    }
}
