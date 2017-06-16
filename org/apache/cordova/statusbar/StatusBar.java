package org.apache.cordova.statusbar;

import android.graphics.Color;
import android.os.Build.VERSION;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.view.Window;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONException;

public class StatusBar extends CordovaPlugin {
    private static final String TAG = "StatusBar";

    /* renamed from: org.apache.cordova.statusbar.StatusBar.1 */
    class C02651 implements Runnable {
        final /* synthetic */ CordovaInterface val$cordova;

        C02651(CordovaInterface cordovaInterface) {
            this.val$cordova = cordovaInterface;
        }

        public void run() {
            this.val$cordova.getActivity().getWindow().clearFlags(AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
            StatusBar.this.setStatusBarBackgroundColor(StatusBar.this.preferences.getString("StatusBarBackgroundColor", "#000000"));
        }
    }

    /* renamed from: org.apache.cordova.statusbar.StatusBar.2 */
    class C02662 implements Runnable {
        final /* synthetic */ Window val$window;

        C02662(Window window) {
            this.val$window = window;
        }

        public void run() {
            if (VERSION.SDK_INT >= 19) {
                this.val$window.getDecorView().setSystemUiVisibility((this.val$window.getDecorView().getSystemUiVisibility() & -1025) & -5);
            }
            this.val$window.clearFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
    }

    /* renamed from: org.apache.cordova.statusbar.StatusBar.3 */
    class C02673 implements Runnable {
        final /* synthetic */ Window val$window;

        C02673(Window window) {
            this.val$window = window;
        }

        public void run() {
            if (VERSION.SDK_INT >= 19) {
                this.val$window.getDecorView().setSystemUiVisibility((this.val$window.getDecorView().getSystemUiVisibility() | AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) | 4);
            }
            this.val$window.addFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
    }

    /* renamed from: org.apache.cordova.statusbar.StatusBar.4 */
    class C02684 implements Runnable {
        final /* synthetic */ CordovaArgs val$args;

        C02684(CordovaArgs cordovaArgs) {
            this.val$args = cordovaArgs;
        }

        public void run() {
            try {
                StatusBar.this.setStatusBarBackgroundColor(this.val$args.getString(0));
            } catch (JSONException e) {
                LOG.m7e(StatusBar.TAG, "Invalid hexString argument, use f.i. '#777777'");
            }
        }
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        LOG.m13v(TAG, "StatusBar: initialization");
        super.initialize(cordova, webView);
        this.cordova.getActivity().runOnUiThread(new C02651(cordova));
    }

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        boolean statusBarVisible = false;
        LOG.m13v(TAG, "Executing action: " + action);
        Window window = this.cordova.getActivity().getWindow();
        if ("_ready".equals(action)) {
            if ((window.getAttributes().flags & AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) == 0) {
                statusBarVisible = true;
            }
            callbackContext.sendPluginResult(new PluginResult(Status.OK, statusBarVisible));
            return true;
        } else if ("show".equals(action)) {
            this.cordova.getActivity().runOnUiThread(new C02662(window));
            return true;
        } else if ("hide".equals(action)) {
            this.cordova.getActivity().runOnUiThread(new C02673(window));
            return true;
        } else if (!"backgroundColorByHexString".equals(action)) {
            return false;
        } else {
            this.cordova.getActivity().runOnUiThread(new C02684(args));
            return true;
        }
    }

    private void setStatusBarBackgroundColor(String colorPref) {
        if (VERSION.SDK_INT >= 21 && colorPref != null && !colorPref.isEmpty()) {
            Window window = this.cordova.getActivity().getWindow();
            window.clearFlags(67108864);
            window.addFlags(ExploreByTouchHelper.INVALID_ID);
            try {
                window.getClass().getDeclaredMethod("setStatusBarColor", new Class[]{Integer.TYPE}).invoke(window, new Object[]{Integer.valueOf(Color.parseColor(colorPref))});
            } catch (IllegalArgumentException e) {
                LOG.m7e(TAG, "Invalid hexString argument, use f.i. '#999999'");
            } catch (Exception e2) {
                LOG.m16w(TAG, "Method window.setStatusBarColor not found for SDK level " + VERSION.SDK_INT);
            }
        }
    }
}
