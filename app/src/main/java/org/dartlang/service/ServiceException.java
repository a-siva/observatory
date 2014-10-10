// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public class ServiceException extends Response {
  public final JSONObject response;
  public final String kind;
  public final String exception;
  public ServiceException(JSONObject response, String kind, String exception) {
    this.response = response;
    this.kind = kind;
    this.exception = exception;
  }
  public ServiceException(JSONObject response) {
    this.response = response;
    this.kind = response.optString("kind");
    this.exception = response.optString("exception");
  }
}
