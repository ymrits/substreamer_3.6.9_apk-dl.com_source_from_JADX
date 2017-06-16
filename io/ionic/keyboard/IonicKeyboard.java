package io.ionic.keyboard;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

public class IonicKeyboard extends CordovaPlugin {

    /* renamed from: io.ionic.keyboard.IonicKeyboard.1 */
    class C01801 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        C01801(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            InputMethodManager inputManager = (InputMethodManager) IonicKeyboard.this.cordova.getActivity().getSystemService("input_method");
            View v = IonicKeyboard.this.cordova.getActivity().getCurrentFocus();
            if (v == null) {
                this.val$callbackContext.error("No current focus");
                return;
            }
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), 2);
            this.val$callbackContext.success();
        }
    }

    /* renamed from: io.ionic.keyboard.IonicKeyboard.2 */
    class C01812 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        C01812(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            ((InputMethodManager) IonicKeyboard.this.cordova.getActivity().getSystemService("input_method")).toggleSoftInput(0, 1);
            this.val$callbackContext.success();
        }
    }

    /* renamed from: io.ionic.keyboard.IonicKeyboard.3 */
    class C01833 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        /* renamed from: io.ionic.keyboard.IonicKeyboard.3.1 */
        class C01821 implements OnGlobalLayoutListener {
            int previousHeightDiff;
            final /* synthetic */ float val$density;
            final /* synthetic */ View val$rootView;

            C01821(View view, float f) {
                this.val$rootView = view;
                this.val$density = f;
                this.previousHeightDiff = 0;
            }

            public void onGlobalLayout() {
                int screenHeight;
                Rect r = new Rect();
                this.val$rootView.getWindowVisibleDisplayFrame(r);
                int rootViewHeight = this.val$rootView.getRootView().getHeight();
                int resultBottom = r.bottom;
                if (VERSION.SDK_INT >= 21) {
                    Display display = IonicKeyboard.this.cordova.getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    screenHeight = size.y;
                } else {
                    screenHeight = rootViewHeight;
                }
                int pixelHeightDiff = (int) (((float) (screenHeight - resultBottom)) / this.val$density);
                PluginResult result;
                if (pixelHeightDiff > 100 && pixelHeightDiff != this.previousHeightDiff) {
                    result = new PluginResult(Status.OK, "S" + Integer.toString(pixelHeightDiff));
                    result.setKeepCallback(true);
                    C01833.this.val$callbackContext.sendPluginResult(result);
                } else if (pixelHeightDiff != this.previousHeightDiff && this.previousHeightDiff - pixelHeightDiff > 100) {
                    result = new PluginResult(Status.OK, "H");
                    result.setKeepCallback(true);
                    C01833.this.val$callbackContext.sendPluginResult(result);
                }
                this.previousHeightDiff = pixelHeightDiff;
            }
        }

        C01833(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            DisplayMetrics dm = new DisplayMetrics();
            IonicKeyboard.this.cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            float density = dm.density;
            View rootView = IonicKeyboard.this.cordova.getActivity().getWindow().getDecorView().findViewById(16908290).getRootView();
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new C01821(rootView, density));
            PluginResult dataResult = new PluginResult(Status.OK);
            dataResult.setKeepCallback(true);
            this.val$callbackContext.sendPluginResult(dataResult);
        }
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("close".equals(action)) {
            this.cordova.getThreadPool().execute(new C01801(callbackContext));
            return true;
        } else if ("show".equals(action)) {
            this.cordova.getThreadPool().execute(new C01812(callbackContext));
            return true;
        } else if (!"init".equals(action)) {
            return false;
        } else {
            this.cordova.getThreadPool().execute(new C01833(callbackContext));
            return true;
        }
    }
}
