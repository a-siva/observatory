// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.service;

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
}
