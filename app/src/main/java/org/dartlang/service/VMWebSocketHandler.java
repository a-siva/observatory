// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.dartlang.observatory.Logger;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Forwards network events back to VM.
 */
class VMWebSocketHandler extends WebSocketHandler {
  public final WebSocketConnection webSocket = new WebSocketConnection();
  public final VM vm;

  public boolean isConnected() { return webSocket.isConnected(); }

  public void disconnect() {
    if (webSocket.isConnected()) {
      webSocket.disconnect();
    }
  }

  VMWebSocketHandler(VM vm, String uri) {
    this.vm = vm;
    try {
      if (!uri.startsWith("ws://")) {
        uri = "ws://" + uri + "/ws";
      }
      webSocket.connect(uri, this);
    } catch (WebSocketException ex) {
      Logger.info("WebSocket connect call failed: " + ex.toString());
      vm.onConnectionFailed();
    }
  }

  public void onOpen() {
    vm.onConnection();
  }

  public void onClose(int code, String reason) {
    Logger.info("WebSocket connection closed (code=" + Integer.toString(code) + "): " + reason);
    if (code == CLOSE_CANNOT_CONNECT) {
      vm.onConnectionFailed();
    } else {
      vm.onConnectionLost();
    }
  }

  public void sendTextMessage(String message) {
    webSocket.sendTextMessage(message);
  }

  public void onTextMessage(String payload) {
    vm.onResponse(payload);
  }

  public void onRawTextMessage(byte[] payload) {
    assert false;
  }

  public void onBinaryMessage(byte[] payload) {
    assert false;
  }
}
