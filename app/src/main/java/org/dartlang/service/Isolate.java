// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.dartlang.observatory.Logger;
import org.json.JSONObject;

public class Isolate extends ServiceObject implements Owner {
  Isolate(VM vm) {
    super(vm);
    assert vm != null;
  }

  public VM getVM() {
    return (VM)owner;
  }

  public Isolate getIsolate() {
    return this;
  }

  protected void update(JSONObject object) {
  }

  public ServiceObject fromJSONObject(String id, JSONObject object) {
    Logger.info("fromJSONObject " + id);
    return null;
  }

  public String relativeLink(String id) {
    return "/" + this.getId() + "/" + id;
  }

  public void get(String id, final ResponseCallback callback) {
    assert callback != null;
    VM vm = getVM();
    assert vm != null;
    vm.connection.get(relativeLink(id), this, callback);
  }
}
