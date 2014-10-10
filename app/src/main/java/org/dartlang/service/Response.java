// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.dartlang.observatory.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
  public boolean isError() {
    return this instanceof ServiceError;
  }

  public boolean isException() {
    return this instanceof ServiceException;
  }

  public boolean isServiceObject() {
    return this instanceof ServiceObject;
  }

  // Returns id of object or "".
  protected static String getIdFromMap(JSONObject object) {
    return object.optString("id");
  }

  protected static String getTypeFromMap(JSONObject object) {
    return object.optString("type", null);
  }

  protected static String getVmTypeFromMap(JSONObject object) {
    String type = getTypeFromMap(object);
    String vmType = object.optString("_vmType");
    return (vmType == null) ? type : vmType;
  }

  // True if object is a service map.
  protected static boolean isServiceMap(JSONObject object) {
    return (object.optString("id", null) != null) &&
           (object.optString("type", null) != null);
  }

  protected static boolean isServiceException(JSONObject object) {
    return getTypeFromMap(object) == "ServiceException";
  }

  protected static boolean isServiceError(JSONObject object) {
    return getTypeFromMap(object) == "ServiceError";
  }

  // Response factory.
  public static Response makeResponse(Connection connection,
                                      Owner owner,
                                      String id,
                                      JSONObject object) {
    if (!isServiceMap(object)) {
      return new ServiceException(object,
                                  "JSONException",
                                  "Expected a service map");
    }
    if (isServiceException(object)) {
      return new ServiceException(object);
    }
    if (isServiceError(object)) {
      return new ServiceError(object);
    }

    // Special case the VM.
    if (owner == null) {
      assert id.equals("vm");
      VM vm = new VM(connection);
      vm.update(object);
      return vm;
    }

    assert owner != null;

    return owner.fromJSONObject(id, object);
  }
}
