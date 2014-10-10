// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.observatory;

import android.app.Application;

import org.dartlang.service.Response;
import org.dartlang.service.ResponseCallback;
import org.dartlang.service.VM;
import org.dartlang.service.Connection;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class ObservatoryApplication extends Application {
  protected Connection connection;
  protected VM vm;

  Connection getConnection() { return connection; }

  void setConnection(Connection connection) {
    this.connection = connection;
    if (connection != null) {
      // New connection, clear the VM.
      setVM(null);
    }
  }

  public boolean hasConnection() {
    return (connection != null) && (connection.isConnected());
  }

  VM getVM(final ResponseCallback callback) {
    if (vm != null) {
      return vm;
    }
    if (connection == null) {
      Logger.error("No connection");
      return null;
    }
    connection.get("vm", null, new ResponseCallback() {
      @Override
      public void onResponse(Response response) {
        // Capture VM
        Logger.info("Fetched VM");
        setVM((VM)response);
        callback.onResponse(response);
      }
    });
    return null;
  }

  private void setVM(VM vm) {
    this.vm = vm;
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    if (connection != null) {
      connection.disconnect();
    }
  }
}
