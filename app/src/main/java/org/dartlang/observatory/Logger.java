// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package org.dartlang.observatory;

import android.util.Log;

public class Logger {
  private static final String TAG = "ObservatoryApplication";

  public static void info(String message) {
    Log.i(TAG, message);
  }

  public static void warning(String message) {
    Log.w(TAG, message);
  }

  public static void error(String message) {
    Log.e(TAG, message);
  }

}
