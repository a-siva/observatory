package org.dartlang.service;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.dartlang.observatory.Logger;

public class Network {
    private static AsyncHttpClient client;

    public static void initialize() {
        client = AsyncHttpClient.getDefaultInstance();
        Logger.info("Network.initialize");
    }

    public static void connect(String uri, final WebSocketNetworkEvents networkEvents) {
        assert networkEvents != null;
        client.websocket(uri, "", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    networkEvents.onWebSocketError(ex);
                    return;
                }
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        networkEvents.onWebSocketClose();
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        networkEvents.onWebSocketString(s);
                    }
                });
                networkEvents.onWebSocketConnect(webSocket);;
            }
        });
    }

    public static interface WebSocketNetworkEvents {
        void onWebSocketError(Exception ex);
        void onWebSocketConnect(com.koushikdutta.async.http.WebSocket webSocket);
        void onWebSocketClose();
        void onWebSocketString(String payload);
    }

}
