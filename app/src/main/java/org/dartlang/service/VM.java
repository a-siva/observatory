package org.dartlang.service;

import org.dartlang.observatory.Logger;
import org.dartlang.observatory.ObservatoryApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by johnmccutchan on 10/4/14.
 */

public class VM extends WebSocketHandler implements Owner {
  private final WebSocketConnection webSocket = new WebSocketConnection();
  public final String uri;
  public final ObservatoryApplication app;
  private final Map<String, VMRequest> pendingRequests = new HashMap<String, VMRequest>();
  private final Map<String, VMRequest> delayedRequests = new HashMap<String, VMRequest>();
  private boolean didConnect = false;
  private int requestSerial = 0;


  public VM(ObservatoryApplication app, String uri) {
    this.uri = uri;
    this.app = app;
    try {
      webSocket.connect(uri, this);
    } catch (WebSocketException ex) {
      Logger.info("WebSocket connect call failed: " + ex.toString());
      app.connectionFailed(this);
    }
  }

  public boolean hasConnected() {
    return didConnect;
  }

  public boolean isConnected() {
    return webSocket.isConnected();
  }

  public void disconnect() {
    if (webSocket.isConnected()) {
      webSocket.disconnect();
    }
  }

  public void onOpen() {
    assert didConnect == false;
    didConnect = true;
    Logger.info("VM Connected to " + uri);
    sendAllDelayedRequests();
    app.connected(this);
  }

  public void onClose(int code, String reason) {
    Logger.info("WebSocket connection closed (code=" + Integer.toString(code) + "): " + reason);
    cancelAllRequests();
    if (code != CLOSE_NORMAL) {
      app.connectionFailed(this);
    } else {
      app.connectionClosed(this);
    }
  }

  public void onTextMessage(String payload) {
    Logger.info("WebSocket got: " + payload);
    onResponse(payload);
  }

  public void onRawTextMessage(byte[] payload) {
    assert false;
  }

  public void onBinaryMessage(byte[] payload) {
    assert false;
  }

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
    webSocket.sendTextMessage(message.toString());
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
      if (isConnected()) {
        // Send immediately.
        sendRequest(serial, request);
      } else {
        // Lost connection. Cancel immediately.
        request.cancel();
      }
    } else {
      // Queue request for connection.
      assert isConnected() == false;
      delayRequest(serial, request);
    }
  }

  public ServiceObject fromJSONObject(JSONObject obj) {
    return null;
  }

  public String relativeLink(String id) {
    return id;
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
}

