// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.dartlang.observatory.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VM extends ServiceObject implements Owner {
  public final String uri;

  private final VMWebSocketHandler webSocketHandler;
  public final EventListener listener;
  private final Map<String, VMRequest> pendingRequests = new HashMap<String, VMRequest>();
  private final Map<String, VMRequest> delayedRequests = new HashMap<String, VMRequest>();
  private int requestSerial = 0;

  private boolean didConnect = false;

  public interface EventListener {
    public void onConnectionFailed(final VM vm);
    public void onConnection(final VM vm);
    public void onConnectionLost(final VM vm);
    public void onResponse(final VM vm, final RequestCallback callback, final Response response);
  }

  protected void onConnectionFailed() {
    cancelAllRequests();
    listener.onConnectionLost(this);
  }

  protected void onConnection() {
    assert didConnect == false;
    didConnect = true;
    Logger.info("VM Connected to " + uri);
    sendAllDelayedRequests();
    listener.onConnection(this);
  }

  protected void onConnectionLost() {
    cancelAllRequests();
    listener.onConnectionLost(this);
  }

  class VMRequest {
    public final String id;
    public final RequestCallback callback;

    VMRequest(String id, RequestCallback callback) {
      this.id = id;
      this.callback = callback;
    }
  }

  protected void onResponse(String response) {
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
    listener.onResponse(this, request.callback, null);
  }

  public boolean hasConnected() {
    return didConnect;
  }

  public boolean isConnected() {
    return webSocketHandler.isConnected();
  }

  public void disconnect() {
    webSocketHandler.disconnect();
  }

  public VM(EventListener listener, String uri) {
    super(null);
    setOwner(this);
    this.uri = uri;
    this.listener = listener;
    webSocketHandler = new VMWebSocketHandler(this, uri);
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
    webSocketHandler.sendTextMessage(message.toString());
  }

  private void delayRequest(String serial, VMRequest request) {
    delayedRequests.put(serial, request);
  }

  private void cancelRequest(VMRequest request) {
    // TODO(johnmccutchan): Use something other than null to indicate a cancel/fail.
    listener.onResponse(this, request.callback, null);
  }

  private void cancelAllRequests() {
    for (Map.Entry<String, VMRequest> entry : delayedRequests.entrySet()) {
      VMRequest request = entry.getValue();
      cancelRequest(request);
    }
    delayedRequests.clear();
    for (Map.Entry<String, VMRequest> entry : pendingRequests.entrySet()) {
      VMRequest request = entry.getValue();
      cancelRequest(request);
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
        cancelRequest(request);
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
}

