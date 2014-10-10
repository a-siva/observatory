// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

import org.json.JSONObject;

public class Response {
  public boolean isError() {
    return this instanceof ServiceError;
  }

  public boolean isException() {
    return this instanceof ServiceException;
  }

  public boolean isObject() {
    return this instanceof ServiceObject;
  }

  // Response factory.
  public static Response makeResponse(Connection connection,
                                      Owner owner,
                                      String id,
                                      JSONObject object) {
    // Check if object isn't a service map -> error.
    // Check if object is an service error -> error.
    // Check if object is a service exception -> exception.

    // Special case the VM.
    if (id.equals("vm")) {
      assert owner == null;
      VM vm = new VM(connection);
      vm.update(object);
      return vm;
    }

    return owner.fromJSONObject(id, object);
  }
}
