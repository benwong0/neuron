package com.humanless.neuron.debug;

/**
 * Internal debug log.
 */
public final class Log {
    public static void d(Object object, String message) {
        d(object.getClass().getSimpleName(), message);
    }

    public static void d(String tag, String message) {
        android.util.Log.d(tag, message);
    }

    public static void e(Object object, String message) {
        e(object.getClass().getSimpleName(), message);
    }

    public static void e(String tag, String message) {
        android.util.Log.e(tag, message);
    }
}
