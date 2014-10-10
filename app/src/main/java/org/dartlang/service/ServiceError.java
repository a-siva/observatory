// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public class ServiceError extends Response {
  public final JSONObject response;
  public final String kind;
  public final String error;

  public ServiceError(JSONObject response) {
    this.response = response;
    this.kind = response.optString("kind");
    this.error = response.optString("error");
  }
}
