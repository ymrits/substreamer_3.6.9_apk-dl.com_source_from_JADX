package org.apache.cordova;

import java.util.Arrays;

public class PermissionHelper {
    private static final String LOG_TAG = "CordovaPermissionHelper";

    public static void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
        requestPermissions(plugin, requestCode, new String[]{permission});
    }

    public static void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {
        try {
            CordovaInterface.class.getDeclaredMethod("requestPermissions", new Class[]{CordovaPlugin.class, Integer.TYPE, String[].class}).invoke(plugin.cordova, new Object[]{plugin, Integer.valueOf(requestCode), permissions});
        } catch (NoSuchMethodException e) {
            LOG.m4d(LOG_TAG, "No need to request permissions " + Arrays.toString(permissions));
            deliverPermissionResult(plugin, requestCode, permissions);
        } catch (Throwable illegalAccessException) {
            LOG.m8e(LOG_TAG, "IllegalAccessException when requesting permissions " + Arrays.toString(permissions), illegalAccessException);
        } catch (Throwable invocationTargetException) {
            LOG.m8e(LOG_TAG, "invocationTargetException when requesting permissions " + Arrays.toString(permissions), invocationTargetException);
        }
    }

    public static boolean hasPermission(CordovaPlugin plugin, String permission) {
        try {
            return ((Boolean) CordovaInterface.class.getDeclaredMethod("hasPermission", new Class[]{String.class}).invoke(plugin.cordova, new Object[]{permission})).booleanValue();
        } catch (NoSuchMethodException e) {
            LOG.m4d(LOG_TAG, "No need to check for permission " + permission);
            return true;
        } catch (Throwable illegalAccessException) {
            LOG.m8e(LOG_TAG, "IllegalAccessException when checking permission " + permission, illegalAccessException);
            return false;
        } catch (Throwable invocationTargetException) {
            LOG.m8e(LOG_TAG, "invocationTargetException when checking permission " + permission, invocationTargetException);
            return false;
        }
    }

    private static void deliverPermissionResult(CordovaPlugin plugin, int requestCode, String[] permissions) {
        Arrays.fill(new int[permissions.length], 0);
        try {
            CordovaPlugin.class.getDeclaredMethod("onRequestPermissionResult", new Class[]{Integer.TYPE, String[].class, int[].class}).invoke(plugin, new Object[]{Integer.valueOf(requestCode), permissions, requestResults});
        } catch (Throwable noSuchMethodException) {
            LOG.m8e(LOG_TAG, "NoSuchMethodException when delivering permissions results", noSuchMethodException);
        } catch (Throwable illegalAccessException) {
            LOG.m8e(LOG_TAG, "IllegalAccessException when delivering permissions results", illegalAccessException);
        } catch (Throwable invocationTargetException) {
            LOG.m8e(LOG_TAG, "InvocationTargetException when delivering permissions results", invocationTargetException);
        }
    }
}
