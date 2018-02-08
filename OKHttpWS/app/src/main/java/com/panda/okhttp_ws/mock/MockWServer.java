package com.panda.okhttp_ws.mock;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;

/**
 * Created by panda on 2018/2/6.
 */
public class MockWServer {
    MockWebServer mockWServer;
    public WebSocket serverSocket;
    String tag = "server";

    public MockWServer() {
        mockWServer = new MockWebServer();
        mockWServer.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                serverSocket = webSocket;
                Log.e(tag, "===server open===");
                Log.e(tag, "server request header:" + response.request().toString());
                Log.e(tag, "server response:" + response.toString());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(tag, "===server message===");
                Log.e(tag, "message: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.e(tag, "===server closing===");
                Log.e(tag, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                serverSocket = null;
                mockWServer = null;
                Log.e(tag, "===server closed===");
                Log.e(tag, "code:" + code + " reason:" + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                Log.e(tag, "===server failed===");
                Log.e(tag, "throwable: " + t);
                Log.e(tag, "response:" + response);
            }
        }));
    }

    public String wsUrl() {
        String wsurl =  "http://" + mockWServer.getHostName() + ":" + mockWServer.getPort();
        Log.e(tag, "wsurl: " + wsurl + "   " + mockWServer.toProxyAddress().toString());
        return wsurl;
    }

    public void close() throws IOException {
        mockWServer.close();
    }
}
