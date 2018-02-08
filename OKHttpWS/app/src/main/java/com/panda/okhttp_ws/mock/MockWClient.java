package com.panda.okhttp_ws.mock;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by panda on 2018/2/6.
 */

public class MockWClient {
    String tag = "client";
    public WebSocket clientSocket;

    public MockWClient(String wsUrl) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(1000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder().url(wsUrl).build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                clientSocket = webSocket;
                Log.e(tag, "---client open---");
                Log.e(tag, "client request header:" + response.request().toString());
                Log.e(tag, "client response:" + response.toString());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(tag, "---client message---");
                Log.e(tag, "message: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.e(tag, "---client closed---");
                Log.e(tag, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                clientSocket = null;
                Log.e(tag, "---client closing---");
                Log.e(tag, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                Log.e(tag, "---client failed---");
                Log.e(tag, "throwable: " + t);
                Log.e(tag, "response:" + response);
            }
        });
    }

    public void close() {
        clientSocket.close(1000, "close");
    }
}
