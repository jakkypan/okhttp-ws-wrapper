package com.panda.okhttp_ws.mock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.panda.okhttp_ws.R;

import java.io.IOException;

/**
 * Created by panda on 2018/2/6.
 */

public class MockTestActivity extends AppCompatActivity {
    MockWClient mockWClient;
    MockWServer mockWServer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mock_test);
    }

    public void open(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mockWServer = new MockWServer();
                mockWClient = new MockWClient(mockWServer.wsUrl());
            }
        }).start();
    }

    public void clientSend(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mockWClient.clientSocket.send("12345");
            }
        }).start();
    }

    public void serverSend(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mockWServer.serverSocket.send("reply: 12345");
            }
        }).start();
    }

    public void close(View v) throws IOException {
        mockWClient.close();
//        mockWServer.close();
    }


}
