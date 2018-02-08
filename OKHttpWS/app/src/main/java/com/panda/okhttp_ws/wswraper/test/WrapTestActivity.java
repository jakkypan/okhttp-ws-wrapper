package com.panda.okhttp_ws.wswraper.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.panda.okhttp_ws.R;
import com.panda.okhttp_ws.wswraper.IWsManager;
import com.panda.okhttp_ws.wswraper.WrappedWebSocketListener;
import com.panda.okhttp_ws.wswraper.WsManager;
import com.panda.okhttp_ws.wswraper.WsStatus;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.ByteString;

/**
 * Created by panda on 2018/2/7.
 */

public class WrapTestActivity extends AppCompatActivity {
    IWsManager wsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrap_test);
    }

    public void connect(View v) {
        wsManager = new WsManager
                .Builder(this)
                .wsClient(new OkHttpClient().newBuilder()
                        .pingInterval(10, TimeUnit.SECONDS)
                        .build())
                .wsUrl("ws://121.40.165.18:8088")
                .retry(2)
                .retryInveral(1000)
                .wsListener(new WrappedWebSocketListener() {
                    @Override
                    public void onOpen(Response response) {
                        Log.e("111", "=====open");
                    }

                    @Override
                    public void onRetry(int count) {
                        Log.e("111", "=====onRetry" + count);
                    }

                    @Override
                    public void onMessage(String text) {
                        Log.e("111", "=====onMessage: " + text);
                    }

                    @Override
                    public void onMessage(ByteString text) {
                        Log.e("111", "=====onMessage: " + text);
                    }

                    @Override
                    public void onClosing(int code, String reason) {
                        Log.e("111", "=====onClosing");
                    }

                    @Override
                    public void onClosed(int code, String reason) {
                        Log.e("111", "=====onClosed");
                    }

                    @Override
                    public void onFailure(Throwable t, Response response) {
                        Log.e("111", "=====onFailure");
                    }
                })
                .build();
        wsManager.wsConnect();
    }

    public void clientSend(View v) {
        if (wsManager.status() == WsStatus.CONNECTED) {
            wsManager.sendMessage("123");
        }
    }

    public void close(View v) {
        wsManager.wsDisConnect();
    }


}