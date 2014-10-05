package org.dartlang.observatory;

import android.util.Log;

/**
 * Created by johnmccutchan on 10/4/14.
 */
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
