// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public class Isolate extends ServiceObject implements Owner {
  Isolate(VM vm) {
    super(vm);
    assert vm != null;
  }

  public VM getVM() {
    return owner.getVM();
  }

  public Isolate getIsolate() {
    return this;
  }

  public ServiceObject fromJSONObject(JSONObject obj) {
    return null;
  }

  public String relativeLink(String id) {
    return "/" + this.getId() + "/" + id;
  }

  public void get(String id, RequestCallback callback) {
    getVM().get(relativeLink(id), callback);
  }
}
