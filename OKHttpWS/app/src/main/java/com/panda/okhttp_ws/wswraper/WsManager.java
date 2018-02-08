package com.panda.okhttp_ws.wswraper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by panda on 2018/2/7.
 */
public class WsManager extends WebSocketListener implements IWsManager {
    private final static int RECONNECT_INTERVAL = 5 * 1000;  // 5s
    private Context mContext;
    private String mWsUrl;
    private WebSocket mWebSocket;
    private WsStatus mWStatus = WsStatus.CLOSED;
    private WrappedWebSocketListener wrappedWebSocketListener;
    private OkHttpClient mOkHttpClient;
    private Lock mLock;
    private int connectRetryCount = 0;
    private int connectRetryIntervalMis = RECONNECT_INTERVAL;
    private int thisConnectRetryCount = 1;
    private Handler wsMainHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (wrappedWebSocketListener != null) {
                wrappedWebSocketListener.onRetry(thisConnectRetryCount);
            }

            doWsConnect();
            thisConnectRetryCount++;
        }
    };

    private WsManager() {
        mLock = new ReentrantLock();
    }

    @Override
    public WebSocket webSocket() {
        return mWebSocket;
    }

    @Override
    public void wsConnect() {
        thisConnectRetryCount = 1;
        doWsConnect();
    }

    @Override
    public void wsDisConnect() {
        if (mWStatus == WsStatus.CLOSED) {
            return;
        }
//        mWStatus = WsStatus.CLOSING;
        wsMainHandler.removeCallbacks(reconnectRunnable);
        // 防止close进入failed而触发重试
        thisConnectRetryCount = connectRetryCount + 1;
        if (mOkHttpClient != null) {
            mOkHttpClient.dispatcher().cancelAll();
        }

        if (mWebSocket != null) {
            mWebSocket.close(1000, "close");
        }
    }

    @Override
    public boolean sendMessage(String message) {
        return doSend(message);
    }

    @Override
    public boolean sendMessage(ByteString message) {
        return doSend(message);
    }

    @Override
    public WsStatus status() {
        return mWStatus;
    }

    @Override
    public void onOpen(WebSocket webSocket, final Response response) {
        mWebSocket = webSocket;
        thisConnectRetryCount = 1;
        mWStatus = WsStatus.CONNECTED;
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onOpen(response);
                    }
                });
            } else {
                wrappedWebSocketListener.onOpen(response);
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, final String text) {
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onMessage(text);
                    }
                });
            } else {
                wrappedWebSocketListener.onMessage(text);
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, final ByteString bytes) {
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onMessage(bytes);
                    }
                });
            } else {
                wrappedWebSocketListener.onMessage(bytes);
            }
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, final int code, final String reason) {
        mWStatus = WsStatus.CLOSING;
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onClosing(code, reason);
                    }
                });
            } else {
                wrappedWebSocketListener.onClosing(code, reason);
            }
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, final int code, final String reason) {
        mWStatus = WsStatus.CLOSED;
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onClosed(code, reason);
                    }
                });
            } else {
                wrappedWebSocketListener.onClosed(code, reason);
            }
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, final Throwable t, @Nullable final Response response) {
        mWStatus = WsStatus.CLOSED;
        if (wrappedWebSocketListener != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wrappedWebSocketListener.onFailure(t, response);
                    }
                });
            } else {
                wrappedWebSocketListener.onFailure(t, response);
            }
        }
        // 同时尝试重连
        tryRetryConnect();
    }

    private boolean doSend(Object message) {
        boolean isSend = false;
        if (mWebSocket != null && mWStatus == WsStatus.CONNECTED) {
            if (message instanceof String) {
                isSend = mWebSocket.send((String) message);
            } else if (message instanceof ByteString) {
                isSend = mWebSocket.send((ByteString) message);
            }
        }

        if (!isSend) {
            tryRetryConnect();
        }
        return isSend;
    }

    private void doWsConnect() {
        if (!NetHelper.isOnline(mContext)) {
            if (wrappedWebSocketListener != null) {
                wrappedWebSocketListener.onFailure(new WsWrapperException("network is offline"), null);
            }
            return;
        }

        mWStatus = WsStatus.CONNECTING;
        mOkHttpClient.dispatcher().cancelAll();
        try {
            mLock.lockInterruptibly();
            try {
                Request request = new Request.Builder().url(mWsUrl).build();
                mOkHttpClient.newWebSocket(request, this);
            } finally {
                mLock.unlock();
            }
        } catch (InterruptedException e) {
            if (wrappedWebSocketListener != null) {
                wrappedWebSocketListener.onFailure(e, null);
            }
        }
    }

    /**
     * 失败重试
     */
    private void tryRetryConnect() {
        // 尝试的测试已经超过了设定的最大值，则不再尝试
        if (thisConnectRetryCount > connectRetryCount) {
            return;
        }
        if (!NetHelper.isOnline(mContext)) {
            return;
        }

        mWStatus = WsStatus.CONNECTING;
        long delay = thisConnectRetryCount * connectRetryIntervalMis;
        wsMainHandler.postDelayed(reconnectRunnable, delay);
    }

    public static class Builder {
        private Context mContext;
        private String mWsUrl;
        private OkHttpClient mOkHttpClient;
        private WrappedWebSocketListener wrappedWebSocketListener;
        private int connectRetryCount;
        private int connectRetryIntervalMis;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder wsUrl(String wsUrl) {
            this.mWsUrl = wsUrl;
            return this;
        }

        public Builder wsClient(OkHttpClient okHttpClient) {
            this.mOkHttpClient = okHttpClient;
            return this;
        }

        public Builder wsListener(WrappedWebSocketListener wrappedWebSocketListener) {
            this.wrappedWebSocketListener = wrappedWebSocketListener;
            return this;
        }

        public Builder retry(int connectRetryCount) {
            if (connectRetryCount < 0) {
                connectRetryCount = 0;
            }
            this.connectRetryCount = connectRetryCount;
            return this;
        }

        public Builder retryInveral(int connectRetryIntervalMis) {
            if (connectRetryIntervalMis < 0) {
                connectRetryIntervalMis = RECONNECT_INTERVAL;
            }
            this.connectRetryIntervalMis = connectRetryIntervalMis;
            return this;
        }

        public IWsManager build() {
            if (mContext == null) {
                throw new WsWrapperException("mContext cannot be null");
            }

            if (TextUtils.isEmpty(mWsUrl)) {
                throw new WsWrapperException("mWsUrl cannot be null");
            }

            if (mOkHttpClient == null) {
                mOkHttpClient = new OkHttpClient.Builder().build();
            }

            WsManager wsManager = new WsManager();
            wsManager.mContext = mContext;
            wsManager.mOkHttpClient = mOkHttpClient;
            wsManager.mWsUrl = mWsUrl;
            wsManager.connectRetryCount = connectRetryCount;
            wsManager.connectRetryIntervalMis = connectRetryIntervalMis;
            wsManager.wrappedWebSocketListener = wrappedWebSocketListener;
            return wsManager;
        }
    }
}
