package org.dartlang.service;

import com.koushikdutta.async.http.WebSocket;

import org.dartlang.observatory.Logger;
import org.dartlang.observatory.ObservatoryApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnmccutchan on 10/4/14.
 */

public class VM implements Network.WebSocketNetworkEvents, Owner  {
    private WebSocket socket;
    public final String uri;
    public final ObservatoryApplication app;

    private boolean didConnect = false;
    private boolean connected = false;

    public boolean getDidConnect() { return didConnect; }
    public boolean getConnected() { return connected; }

    public void disconnect() {
        if (socket != null) {
            socket.close();
        }
    }


    public VM(ObservatoryApplication app, String uri) {
        this.uri = uri;
        this.app = app;
        Network.connect(uri, this);
    }

    public void onWebSocketError(Exception ex) {
        assert didConnect == false;
        assert connected == false;
        Logger.error("Could not connect to " + uri + " reason: " + ex.toString());
        socket = null;
        cancelAllRequests();
        app.connectionFailed(this);
    }

    public void onWebSocketConnect(com.koushikdutta.async.http.WebSocket webSocket) {
        assert didConnect == false;
        assert connected == false;
        didConnect = true;
        connected = true;
        Logger.info("VM Connected to " + uri);
        sendAllDelayedRequests();
        app.connected(this);
    }

    public void onWebSocketClose() {
        assert didConnect == true;
        connected = false;
        Logger.info("VM Disconnected from " + uri);
        cancelAllRequests();
        app.connectionClosed(this);
    }

    class VMRequest {
        public final String id;
        public final RequestCallback callback;

        VMRequest(String id, RequestCallback callback) {
            this.id = id;
            this.callback = callback;
        }

        public void cancel() {
            callback.onResponse(null);
        }
    }

    public void onWebSocketString(String payload) {
        Logger.info("VM Got: " + payload);
        onResponse(payload);
    }

    private final Map<String, VMRequest> pendingRequests = new HashMap<String, VMRequest>();
    private final Map<String, VMRequest> delayedRequests = new HashMap<String, VMRequest>();
    private int requestSerial = 0;

    private void sendRequest(String serial, VMRequest request) {
        // Construct JSON request.
        //
        JSONObject message = new JSONObject();
        try {
            message.put("seq", serial);
            message.put("request", request.id);
        } catch (JSONException ex) {
            Logger.error("Could not create JSON for request url: " + request.id);
            return;
        }
        Logger.info("Sending request to VM: " + message.toString());
        pendingRequests.put(serial, request);
        socket.send(message.toString());
    }

    private void delayRequest(String serial, VMRequest request) {
        delayedRequests.put(serial, request);
    }

    private void onResponse(String response) {
        JSONObject map;

        Logger.info("Got following from VM: " + response);
        try {
            map = new JSONObject(response);
        } catch (JSONException e) {
            Logger.error("JSON Exception: " + e.toString());
            return;
        }
        String serial = map.optString("seq", null);
        if (serial == null) {
            Logger.warning("Event response handling not implemented yet.");
            return;
        }
        VMRequest request = pendingRequests.remove(serial);
        if (request == null) {
            Logger.error("No pending request with serial: " + serial);
            return;
        }
        request.callback.onResponse(null);
    }

    private void cancelAllRequests() {
        for (Map.Entry<String, VMRequest> entry : delayedRequests.entrySet()) {
            VMRequest request = entry.getValue();
            request.cancel();
        }
        delayedRequests.clear();
        for (Map.Entry<String, VMRequest> entry : pendingRequests.entrySet()) {
            VMRequest request = entry.getValue();
            request.cancel();
        }
        pendingRequests.clear();
    }

    private void sendAllDelayedRequests() {
        for (Map.Entry<String, VMRequest> entry : delayedRequests.entrySet()) {
            VMRequest request = entry.getValue();
            sendRequest(entry.getKey(), request);
        }
        delayedRequests.clear();
    }

    public void get(String id, RequestCallback callback) {
        assert callback != null;
        VMRequest request = new VMRequest(id, callback);
        String serial = Integer.toString(requestSerial++);
        if (didConnect) {
            if (connected) {
                // Send immediately.
                sendRequest(serial, request);
            } else {
                // Lost connection. Cancel immediately.
                request.cancel();
            }
        } else {
            // Queue request for connection.
            assert connected == false;
            delayRequest(serial, request);
        }
    }

    public ServiceObject fromJSONObject(JSONObject obj) {
        return null;
    }

    public String relativeLink(String id) {
        return id;
    }
}

