package com.panda.okhttp_ws.wswraper;

/**
 * Created by panda on 2018/2/7.
 */
public class WsWrapperException extends RuntimeException {

    public WsWrapperException(String message) {
        super(message);
    }

    public WsWrapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public WsWrapperException(Throwable cause) {
        super(cause);
    }
}
