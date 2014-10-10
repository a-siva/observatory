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

// A connection to the Dart VM Service protocol.
public class Connection extends WebSocketHandler {
  public final WebSocketConnection webSocket = new WebSocketConnection();
  private boolean disconnectForced = false;
  private boolean didConnect = false;
  public final EventListener listener;
  public final String uri;
  private final Map<String, Request> pendingRequests = new HashMap<>();
  private final Map<String, Request> delayedRequests = new HashMap<>();
  private int nextRequestId = 0;

  public interface EventListener {
    // Network connection attempt failed.
    public void onConnectFailed(final Connection connection,
                                final String message);
    // Network connection established.
    public void onConnect(final Connection connection);
    // App initiated disconnect.
    public void onDisconnect(final Connection connection);
    // Network connection lost.
    public void onConnectionLost(final Connection connection);
    // WebSocket response.
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
      listener.onDisconnect(this);
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
      listener.onConnectFailed(this, ex.toString());
    }
  }

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

  private JSONObject makeServiceExceptionMap(final JSONObject response,
                                             final String kind,
                                             final String exception) {
    JSONObject serviceException = new JSONObject();
    try {
      serviceException.put("type", "ServiceException");
      serviceException.put("id", "");
      serviceException.put("kind", kind);
      serviceException.put("response", response);
      serviceException.put("exception", exception);
    } catch (JSONException ex) {
      Logger.error("makeServiceExceptionResponse exception: " + ex.toString());
    }
    return serviceException;
  }

  private void sendRequest(String requestId, Request request) {
    // Construct JSON request.
    //
    JSONObject message = new JSONObject();
    try {
      message.put("seq", requestId);
      message.put("request", request.id);
    } catch (JSONException ex) {
      Logger.error("Could not create JSON for request url: " + request.id);
      return;
    }
    pendingRequests.put(requestId, request);
    webSocket.sendTextMessage(message.toString());
  }

  private void delayRequest(String requestId, Request request) {
    delayedRequests.put(requestId, request);
  }

  private void cancelRequest(Request request, String reason) {
    JSONObject map = makeServiceExceptionMap(null, "ConnectionClosed", reason);
    listener.onResponse(this, request.owner, request.callback, request.id, null);
  }

  private void cancelAllRequests(String reason) {
    for (Map.Entry<String, Request> entry : delayedRequests.entrySet()) {
      Request request = entry.getValue();
      cancelRequest(request, reason);
    }
    delayedRequests.clear();
    for (Map.Entry<String, Request> entry : pendingRequests.entrySet()) {
      Request request = entry.getValue();
      cancelRequest(request, reason);
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
    String requestId = Integer.toString(nextRequestId++);
    if (didConnect) {
      if (isConnected()) {
        // Send immediately.
        sendRequest(requestId, request);
      } else {
        // Lost connection. Cancel immediately.
        cancelRequest(request, "No connection");
      }
    } else {
      // Queue request for connection.
      assert isConnected() == false;
      delayRequest(requestId, request);
    }
  }

  public void onOpen() {
    assert didConnect == false;
    didConnect = true;
    sendAllDelayedRequests();
    listener.onConnect(this);
  }

  public void onClose(int code, String reason) {
    cancelAllRequests(reason);
    if (disconnectForced) {
      // Handled in disconnect().
      return;
    } else if (code == CLOSE_CANNOT_CONNECT) {
      listener.onConnectFailed(this, reason);
    } else {
      listener.onConnectionLost(this);
    }
  }

  public void onTextMessage(String message) {
    JSONObject map;

    try {
      map = new JSONObject(message);
    } catch (JSONException e) {
      Logger.error("JSON Exception: " + e.toString());
      return;
    }
    String requestId = map.optString("seq", null);
    if (requestId == null) {
      Logger.warning("Event response handling not implemented yet.");
      return;
    }
    Request request = pendingRequests.remove(requestId);
    if (request == null) {
      Logger.error("No pending request with request id: " + requestId);
      return;
    }
    String response = map.optString("response", null);
    try {
      map = new JSONObject(response);
    } catch (JSONException ex) {
      map = makeServiceExceptionMap(map, "JSONException", ex.toString());
    }

    listener.onResponse(this, request.owner, request.callback, request.id, map);
  }

  public void onRawTextMessage(byte[] payload) {
    throw new UnsupportedOperationException("");
  }

  public void onBinaryMessage(byte[] payload) {
    throw new UnsupportedOperationException("");
  }
}
