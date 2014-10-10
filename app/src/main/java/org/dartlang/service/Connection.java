// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.dartlang.observatory.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class Connection extends WebSocketHandler {
  public final WebSocketConnection webSocket = new WebSocketConnection();
  private boolean disconnectForced = false;
  private boolean didConnect = false;
  public final EventListener listener;
  public final String uri;

  public interface EventListener {
    public void onConnectionFailed(final Connection connection);
    public void onConnection(final Connection connection);
    public void onConnectionLost(final Connection connection);
    public void onResponse(final Connection connection,
                           final Owner owner,
                           final ResponseCallback callback,
                           final String id,
                           final JSONObject response);
  }

  public boolean isConnected() { return webSocket.isConnected(); }

  public void disconnect() {
    if (webSocket.isConnected()) {
      disconnectForced = true;
      webSocket.disconnect();
    }
  }

  public Connection(EventListener listener, String uri) {
    this.listener = listener;
    this.uri = uri;
    try {
      if (!uri.startsWith("ws://")) {
        uri = "ws://" + uri + "/ws";
      }
      webSocket.connect(uri, this);
    } catch (WebSocketException ex) {
      Logger.info("WebSocket connect call failed: " + ex.toString());
      listener.onConnectionFailed(this);
    }
  }

  public void onOpen() {
    assert didConnect == false;
    didConnect = true;
    sendAllDelayedRequests();
    listener.onConnection(this);
  }

  public void onClose(int code, String reason) {
    cancelAllRequests();
    if (code == CLOSE_CANNOT_CONNECT) {
      listener.onConnectionFailed(this);
    } else {
      listener.onConnectionLost(this);
    }
  }

  public void onTextMessage(String response) {
    JSONObject map;

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
    Request request = pendingRequests.remove(serial);
    if (request == null) {
      Logger.error("No pending request with serial: " + serial);
      return;
    }
    String responseString = map.optString("response", null);
    try {
      map = new JSONObject(responseString);
    } catch (JSONException e) {
      map = null;
      Logger.error("JSON Exception: " + e.toString());
    }

    listener.onResponse(this, request.owner, request.callback, request.id, map);
  }

  private final Map<String, Request> pendingRequests = new HashMap<>();
  private final Map<String, Request> delayedRequests = new HashMap<>();
  private int requestSerial = 0;

  public interface RequestCallback {
    public void onResponse(JSONObject response);
  }

  protected class Request {
    public final String id;
    public final Owner owner;
    public final ResponseCallback callback;

    Request(String id, Owner owner, ResponseCallback callback) {
      this.id = id;
      this.owner = owner;
      this.callback = callback;
    }
  }

  private void sendRequest(String serial, Request request) {
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
    pendingRequests.put(serial, request);
    webSocket.sendTextMessage(message.toString());
  }

  private void delayRequest(String serial, Request request) {
    delayedRequests.put(serial, request);
  }

  private void cancelRequest(Request request) {
    listener.onResponse(this, request.owner, request.callback, request.id, null);
  }

  private void cancelAllRequests() {
    for (Map.Entry<String, Request> entry : delayedRequests.entrySet()) {
      Request request = entry.getValue();
      cancelRequest(request);
    }
    delayedRequests.clear();
    for (Map.Entry<String, Request> entry : pendingRequests.entrySet()) {
      Request request = entry.getValue();
      cancelRequest(request);
    }
    pendingRequests.clear();
  }

  private void sendAllDelayedRequests() {
    for (Map.Entry<String, Request> entry : delayedRequests.entrySet()) {
      Request request = entry.getValue();
      sendRequest(entry.getKey(), request);
    }
    delayedRequests.clear();
  }

  public void get(String id, Owner owner, ResponseCallback callback) {
    Request request = new Request(id, owner, callback);
    String serial = Integer.toString(requestSerial++);
    if (didConnect) {
      if (isConnected()) {
        // Send immediately.
        sendRequest(serial, request);
      } else {
        // Lost connection. Cancel immediately.
        cancelRequest(request);
      }
    } else {
      // Queue request for connection.
      assert isConnected() == false;
      delayRequest(serial, request);
    }
  }

  public void onRawTextMessage(byte[] payload) {
    assert false;
  }

  public void onBinaryMessage(byte[] payload) {
    assert false;
  }
}
