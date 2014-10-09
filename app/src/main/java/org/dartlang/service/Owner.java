// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public interface Owner {
  public ServiceObject fromJSONObject(JSONObject object);

  public VM getVM();

  public Isolate getIsolate();

  public String relativeLink(String id);

  public void get(String id, RequestCallback callback);

  public static interface RequestCallback {
    public void onResponse(Response response);
  }
}
