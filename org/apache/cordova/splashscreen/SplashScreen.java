package org.apache.cordova.splashscreen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.Display;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class SplashScreen extends CordovaPlugin {
    private static final int DEFAULT_FADE_DURATION = 500;
    private static final int DEFAULT_SPLASHSCREEN_DURATION = 3000;
    private static final boolean HAS_BUILT_IN_SPLASH_SCREEN;
    private static final String LOG_TAG = "SplashScreen";
    private static boolean firstShow;
    private static boolean lastHideAfterDelay;
    private static ProgressDialog spinnerDialog;
    private static Dialog splashDialog;
    private int orientation;
    private ImageView splashImageView;

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.1 */
    class C02551 implements Runnable {
        C02551() {
        }

        public void run() {
            SplashScreen.this.getView().setVisibility(4);
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.2 */
    class C02562 implements Runnable {
        C02562() {
        }

        public void run() {
            SplashScreen.this.webView.postMessage("splashscreen", "hide");
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.3 */
    class C02573 implements Runnable {
        C02573() {
        }

        public void run() {
            SplashScreen.this.webView.postMessage("splashscreen", "show");
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.4 */
    class C02594 implements Runnable {
        final /* synthetic */ boolean val$forceHideImmediately;

        /* renamed from: org.apache.cordova.splashscreen.SplashScreen.4.1 */
        class C02581 implements AnimationListener {
            C02581() {
            }

            public void onAnimationStart(Animation animation) {
                SplashScreen.this.spinnerStop();
            }

            public void onAnimationEnd(Animation animation) {
                if (SplashScreen.splashDialog != null && SplashScreen.splashDialog.isShowing()) {
                    SplashScreen.splashDialog.dismiss();
                    SplashScreen.splashDialog = null;
                    SplashScreen.this.splashImageView = null;
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        }

        C02594(boolean z) {
            this.val$forceHideImmediately = z;
        }

        public void run() {
            if (SplashScreen.splashDialog != null && SplashScreen.splashDialog.isShowing()) {
                int fadeSplashScreenDuration = SplashScreen.this.getFadeDuration();
                if (fadeSplashScreenDuration <= 0 || this.val$forceHideImmediately) {
                    SplashScreen.this.spinnerStop();
                    SplashScreen.splashDialog.dismiss();
                    SplashScreen.splashDialog = null;
                    SplashScreen.this.splashImageView = null;
                    return;
                }
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setInterpolator(new DecelerateInterpolator());
                fadeOut.setDuration((long) fadeSplashScreenDuration);
                SplashScreen.this.splashImageView.setAnimation(fadeOut);
                SplashScreen.this.splashImageView.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new C02581());
            }
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.5 */
    class C02615 implements Runnable {
        final /* synthetic */ int val$drawableId;
        final /* synthetic */ int val$effectiveSplashDuration;
        final /* synthetic */ boolean val$hideAfterDelay;

        /* renamed from: org.apache.cordova.splashscreen.SplashScreen.5.1 */
        class C02601 implements Runnable {
            C02601() {
            }

            public void run() {
                if (SplashScreen.lastHideAfterDelay) {
                    SplashScreen.this.removeSplashScreen(SplashScreen.HAS_BUILT_IN_SPLASH_SCREEN);
                }
            }
        }

        C02615(int i, boolean z, int i2) {
            this.val$drawableId = i;
            this.val$hideAfterDelay = z;
            this.val$effectiveSplashDuration = i2;
        }

        public void run() {
            Display display = SplashScreen.this.cordova.getActivity().getWindowManager().getDefaultDisplay();
            Context context = SplashScreen.this.webView.getContext();
            SplashScreen.this.splashImageView = new ImageView(context);
            SplashScreen.this.splashImageView.setImageResource(this.val$drawableId);
            SplashScreen.this.splashImageView.setLayoutParams(new LayoutParams(-1, -1));
            SplashScreen.this.splashImageView.setMinimumHeight(display.getHeight());
            SplashScreen.this.splashImageView.setMinimumWidth(display.getWidth());
            SplashScreen.this.splashImageView.setBackgroundColor(SplashScreen.this.preferences.getInteger("backgroundColor", ViewCompat.MEASURED_STATE_MASK));
            if (SplashScreen.this.isMaintainAspectRatio()) {
                SplashScreen.this.splashImageView.setScaleType(ScaleType.CENTER_CROP);
            } else {
                SplashScreen.this.splashImageView.setScaleType(ScaleType.FIT_XY);
            }
            SplashScreen.splashDialog = new Dialog(context, 16973840);
            if ((SplashScreen.this.cordova.getActivity().getWindow().getAttributes().flags & AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) == AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) {
                SplashScreen.splashDialog.getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            }
            SplashScreen.splashDialog.setContentView(SplashScreen.this.splashImageView);
            SplashScreen.splashDialog.setCancelable(SplashScreen.HAS_BUILT_IN_SPLASH_SCREEN);
            SplashScreen.splashDialog.show();
            if (SplashScreen.this.preferences.getBoolean("ShowSplashScreenSpinner", true)) {
                SplashScreen.this.spinnerStart();
            }
            if (this.val$hideAfterDelay) {
                new Handler().postDelayed(new C02601(), (long) this.val$effectiveSplashDuration);
            }
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.6 */
    class C02636 implements Runnable {

        /* renamed from: org.apache.cordova.splashscreen.SplashScreen.6.1 */
        class C02621 implements OnCancelListener {
            C02621() {
            }

            public void onCancel(DialogInterface dialog) {
                SplashScreen.spinnerDialog = null;
            }
        }

        C02636() {
        }

        public void run() {
            SplashScreen.this.spinnerStop();
            SplashScreen.spinnerDialog = new ProgressDialog(SplashScreen.this.webView.getContext());
            SplashScreen.spinnerDialog.setOnCancelListener(new C02621());
            SplashScreen.spinnerDialog.setCancelable(SplashScreen.HAS_BUILT_IN_SPLASH_SCREEN);
            SplashScreen.spinnerDialog.setIndeterminate(true);
            RelativeLayout centeredLayout = new RelativeLayout(SplashScreen.this.cordova.getActivity());
            centeredLayout.setGravity(17);
            centeredLayout.setLayoutParams(new RelativeLayout.LayoutParams(-2, -2));
            ProgressBar progressBar = new ProgressBar(SplashScreen.this.webView.getContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams.addRule(13, -1);
            progressBar.setLayoutParams(layoutParams);
            centeredLayout.addView(progressBar);
            SplashScreen.spinnerDialog.getWindow().clearFlags(2);
            SplashScreen.spinnerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            SplashScreen.spinnerDialog.show();
            SplashScreen.spinnerDialog.setContentView(centeredLayout);
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen.7 */
    class C02647 implements Runnable {
        C02647() {
        }

        public void run() {
            if (SplashScreen.spinnerDialog != null && SplashScreen.spinnerDialog.isShowing()) {
                SplashScreen.spinnerDialog.dismiss();
                SplashScreen.spinnerDialog = null;
            }
        }
    }

    static {
        boolean z = HAS_BUILT_IN_SPLASH_SCREEN;
        if (Integer.valueOf(CordovaWebView.CORDOVA_VERSION.split("\\.")[0]).intValue() < 4) {
            z = true;
        }
        HAS_BUILT_IN_SPLASH_SCREEN = z;
        firstShow = true;
    }

    private View getView() {
        try {
            return (View) this.webView.getClass().getMethod("getView", new Class[0]).invoke(this.webView, new Object[0]);
        } catch (Exception e) {
            return (View) this.webView;
        }
    }

    protected void pluginInitialize() {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            this.cordova.getActivity().runOnUiThread(new C02551());
            if (this.preferences.getInteger("SplashDrawableId", 0) == 0) {
                String splashResource = this.preferences.getString(LOG_TAG, "screen");
                if (splashResource != null) {
                    int drawableId = this.cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", this.cordova.getActivity().getClass().getPackage().getName());
                    if (drawableId == 0) {
                        drawableId = this.cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", this.cordova.getActivity().getPackageName());
                    }
                    this.preferences.set("SplashDrawableId", drawableId);
                }
            }
            this.orientation = this.cordova.getActivity().getResources().getConfiguration().orientation;
            if (firstShow) {
                showSplashScreen(this.preferences.getBoolean("AutoHideSplashScreen", true));
            }
            if (this.preferences.getBoolean("SplashShowOnlyFirstTime", true)) {
                firstShow = HAS_BUILT_IN_SPLASH_SCREEN;
            }
        }
    }

    private boolean isMaintainAspectRatio() {
        return this.preferences.getBoolean("SplashMaintainAspectRatio", HAS_BUILT_IN_SPLASH_SCREEN);
    }

    private int getFadeDuration() {
        int fadeSplashScreenDuration = this.preferences.getBoolean("FadeSplashScreen", true) ? this.preferences.getInteger("FadeSplashScreenDuration", DEFAULT_FADE_DURATION) : 0;
        if (fadeSplashScreenDuration < 30) {
            return fadeSplashScreenDuration * PointerIconCompat.TYPE_DEFAULT;
        }
        return fadeSplashScreenDuration;
    }

    public void onPause(boolean multitasking) {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            removeSplashScreen(true);
        }
    }

    public void onDestroy() {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            removeSplashScreen(true);
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("hide")) {
            this.cordova.getActivity().runOnUiThread(new C02562());
        } else if (!action.equals("show")) {
            return HAS_BUILT_IN_SPLASH_SCREEN;
        } else {
            this.cordova.getActivity().runOnUiThread(new C02573());
        }
        callbackContext.success();
        return true;
    }

    public Object onMessage(String id, Object data) {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            if ("splashscreen".equals(id)) {
                if ("hide".equals(data.toString())) {
                    removeSplashScreen(HAS_BUILT_IN_SPLASH_SCREEN);
                } else {
                    showSplashScreen(HAS_BUILT_IN_SPLASH_SCREEN);
                }
            } else if ("spinner".equals(id)) {
                if ("stop".equals(data.toString())) {
                    getView().setVisibility(0);
                }
            } else if ("onReceivedError".equals(id)) {
                spinnerStop();
            }
        }
        return null;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation != this.orientation) {
            this.orientation = newConfig.orientation;
            if (this.splashImageView != null) {
                int drawableId = this.preferences.getInteger("SplashDrawableId", 0);
                if (drawableId != 0) {
                    this.splashImageView.setImageDrawable(this.cordova.getActivity().getResources().getDrawable(drawableId));
                }
            }
        }
    }

    private void removeSplashScreen(boolean forceHideImmediately) {
        this.cordova.getActivity().runOnUiThread(new C02594(forceHideImmediately));
    }

    private void showSplashScreen(boolean hideAfterDelay) {
        int splashscreenTime = this.preferences.getInteger("SplashScreenDelay", DEFAULT_SPLASHSCREEN_DURATION);
        int drawableId = this.preferences.getInteger("SplashDrawableId", 0);
        int effectiveSplashDuration = Math.max(0, splashscreenTime - getFadeDuration());
        lastHideAfterDelay = hideAfterDelay;
        if ((splashDialog != null && splashDialog.isShowing()) || drawableId == 0) {
            return;
        }
        if (splashscreenTime > 0 || !hideAfterDelay) {
            this.cordova.getActivity().runOnUiThread(new C02615(drawableId, hideAfterDelay, effectiveSplashDuration));
        }
    }

    private void spinnerStart() {
        this.cordova.getActivity().runOnUiThread(new C02636());
    }

    private void spinnerStop() {
        this.cordova.getActivity().runOnUiThread(new C02647());
    }
}
