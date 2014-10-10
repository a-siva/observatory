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
  protected final Connection connection;
  public String targetCPU;
  public String hostCPU;
  public String version;

  public VM(Connection connection) {
    super(null);
    this.connection = connection;
  }

  public VM getVM() {
    return this;
  }

  public Isolate getIsolate() {
    return null;
  }

  public String relativeLink(String id) {
    return id;
  }

  protected void update(JSONObject object) {
    updateCommon(object);
    targetCPU = object.optString("targetCPU");
    hostCPU = object.optString("hostCPU");
    version = object.optString("version");
  }

  public void get(String id, final ResponseCallback callback) {
    assert callback != null;
    connection.get(relativeLink(id), this, callback);
  }

  public ServiceObject fromJSONObject(String id, JSONObject object) {
    Logger.info("fromJSONObject " + id);
    return null;
  }
}

