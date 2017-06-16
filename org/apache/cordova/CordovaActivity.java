package org.apache.cordova;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout.LayoutParams;
import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class CordovaActivity extends Activity {
    private static int ACTIVITY_EXITING;
    private static int ACTIVITY_RUNNING;
    private static int ACTIVITY_STARTING;
    public static String TAG;
    protected CordovaWebView appView;
    protected CordovaInterfaceImpl cordovaInterface;
    protected boolean immersiveMode;
    protected boolean keepRunning;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaPreferences preferences;

    /* renamed from: org.apache.cordova.CordovaActivity.2 */
    class C01882 implements Runnable {
        final /* synthetic */ String val$errorUrl;
        final /* synthetic */ CordovaActivity val$me;

        C01882(CordovaActivity cordovaActivity, String str) {
            this.val$me = cordovaActivity;
            this.val$errorUrl = str;
        }

        public void run() {
            this.val$me.appView.showWebPage(this.val$errorUrl, false, true, null);
        }
    }

    /* renamed from: org.apache.cordova.CordovaActivity.3 */
    class C01893 implements Runnable {
        final /* synthetic */ String val$description;
        final /* synthetic */ boolean val$exit;
        final /* synthetic */ String val$failingUrl;
        final /* synthetic */ CordovaActivity val$me;

        C01893(boolean z, CordovaActivity cordovaActivity, String str, String str2) {
            this.val$exit = z;
            this.val$me = cordovaActivity;
            this.val$description = str;
            this.val$failingUrl = str2;
        }

        public void run() {
            if (this.val$exit) {
                this.val$me.appView.getView().setVisibility(8);
                this.val$me.displayError("Application Error", this.val$description + " (" + this.val$failingUrl + ")", "OK", this.val$exit);
            }
        }
    }

    /* renamed from: org.apache.cordova.CordovaActivity.4 */
    class C01914 implements Runnable {
        final /* synthetic */ String val$button;
        final /* synthetic */ boolean val$exit;
        final /* synthetic */ CordovaActivity val$me;
        final /* synthetic */ String val$message;
        final /* synthetic */ String val$title;

        /* renamed from: org.apache.cordova.CordovaActivity.4.1 */
        class C01901 implements OnClickListener {
            C01901() {
            }

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (C01914.this.val$exit) {
                    CordovaActivity.this.finish();
                }
            }
        }

        C01914(CordovaActivity cordovaActivity, String str, String str2, String str3, boolean z) {
            this.val$me = cordovaActivity;
            this.val$message = str;
            this.val$title = str2;
            this.val$button = str3;
            this.val$exit = z;
        }

        public void run() {
            try {
                Builder dlg = new Builder(this.val$me);
                dlg.setMessage(this.val$message);
                dlg.setTitle(this.val$title);
                dlg.setCancelable(false);
                dlg.setPositiveButton(this.val$button, new C01901());
                dlg.create();
                dlg.show();
            } catch (Exception e) {
                CordovaActivity.this.finish();
            }
        }
    }

    /* renamed from: org.apache.cordova.CordovaActivity.1 */
    class C03451 extends CordovaInterfaceImpl {
        C03451(Activity activity) {
            super(activity);
        }

        public Object onMessage(String id, Object data) {
            return CordovaActivity.this.onMessage(id, data);
        }
    }

    public CordovaActivity() {
        this.keepRunning = true;
    }

    static {
        TAG = "CordovaActivity";
        ACTIVITY_STARTING = 0;
        ACTIVITY_RUNNING = 1;
        ACTIVITY_EXITING = 2;
    }

    public void onCreate(Bundle savedInstanceState) {
        loadConfig();
        LOG.setLogLevel(this.preferences.getString("loglevel", "ERROR"));
        LOG.m10i(TAG, "Apache Cordova native platform version 6.2.1 is starting");
        LOG.m4d(TAG, "CordovaActivity.onCreate()");
        if (!this.preferences.getBoolean("ShowTitle", false)) {
            getWindow().requestFeature(1);
        }
        if (this.preferences.getBoolean("SetFullscreen", false)) {
            LOG.m4d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            this.preferences.set("Fullscreen", true);
        }
        if (!this.preferences.getBoolean("Fullscreen", false)) {
            getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
        } else if (VERSION.SDK_INT < 19 || this.preferences.getBoolean("FullscreenNotImmersive", false)) {
            getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        } else {
            this.immersiveMode = true;
        }
        super.onCreate(savedInstanceState);
        this.cordovaInterface = makeCordovaInterface();
        if (savedInstanceState != null) {
            this.cordovaInterface.restoreInstanceState(savedInstanceState);
        }
    }

    protected void init() {
        this.appView = makeWebView();
        createViews();
        if (!this.appView.isInitialized()) {
            this.appView.init(this.cordovaInterface, this.pluginEntries, this.preferences);
        }
        this.cordovaInterface.onCordovaInit(this.appView.getPluginManager());
        if ("media".equals(this.preferences.getString("DefaultVolumeStream", BuildConfig.FLAVOR).toLowerCase(Locale.ENGLISH))) {
            setVolumeControlStream(3);
        }
    }

    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse((Context) this);
        this.preferences = parser.getPreferences();
        this.preferences.setPreferencesBundle(getIntent().getExtras());
        this.launchUrl = parser.getLaunchUrl();
        this.pluginEntries = parser.getPluginEntries();
        Config.parser = parser;
    }

    protected void createViews() {
        this.appView.getView().setId(100);
        this.appView.getView().setLayoutParams(new LayoutParams(-1, -1));
        setContentView(this.appView.getView());
        if (this.preferences.contains("BackgroundColor")) {
            try {
                this.appView.getView().setBackgroundColor(this.preferences.getInteger("BackgroundColor", ViewCompat.MEASURED_STATE_MASK));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        this.appView.getView().requestFocusFromTouch();
    }

    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(this, this.preferences);
    }

    protected CordovaInterfaceImpl makeCordovaInterface() {
        return new C03451(this);
    }

    public void loadUrl(String url) {
        if (this.appView == null) {
            init();
        }
        this.keepRunning = this.preferences.getBoolean("KeepRunning", true);
        this.appView.loadUrlIntoView(url, true);
    }

    protected void onPause() {
        super.onPause();
        LOG.m4d(TAG, "Paused the activity.");
        if (this.appView != null) {
            boolean keepRunning = this.keepRunning || this.cordovaInterface.activityResultCallback != null;
            this.appView.handlePause(keepRunning);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.appView != null) {
            this.appView.onNewIntent(intent);
        }
    }

    protected void onResume() {
        super.onResume();
        LOG.m4d(TAG, "Resumed the activity.");
        if (this.appView != null) {
            getWindow().getDecorView().requestFocus();
            this.appView.handleResume(this.keepRunning);
        }
    }

    protected void onStop() {
        super.onStop();
        LOG.m4d(TAG, "Stopped the activity.");
        if (this.appView != null) {
            this.appView.handleStop();
        }
    }

    protected void onStart() {
        super.onStart();
        LOG.m4d(TAG, "Started the activity.");
        if (this.appView != null) {
            this.appView.handleStart();
        }
    }

    public void onDestroy() {
        LOG.m4d(TAG, "CordovaActivity.onDestroy()");
        super.onDestroy();
        if (this.appView != null) {
            this.appView.handleDestroy();
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && this.immersiveMode) {
            getWindow().getDecorView().setSystemUiVisibility(5894);
        }
    }

    @SuppressLint({"NewApi"})
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        this.cordovaInterface.setActivityResultRequestCode(requestCode);
        super.startActivityForResult(intent, requestCode, options);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        LOG.m4d(TAG, "Incoming Result. Request code = " + requestCode);
        super.onActivityResult(requestCode, resultCode, intent);
        this.cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    public void onReceivedError(int errorCode, String description, String failingUrl) {
        CordovaActivity me = this;
        String errorUrl = this.preferences.getString("errorUrl", null);
        if (errorUrl == null || failingUrl.equals(errorUrl) || this.appView == null) {
            runOnUiThread(new C01893(errorCode != -2, me, description, failingUrl));
        } else {
            runOnUiThread(new C01882(me, errorUrl));
        }
    }

    public void displayError(String title, String message, String button, boolean exit) {
        runOnUiThread(new C01914(this, message, title, button, exit));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.appView != null) {
            this.appView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.appView != null) {
            this.appView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.appView != null) {
            this.appView.getPluginManager().postMessage("onOptionsItemSelected", item);
        }
        return true;
    }

    public Object onMessage(String id, Object data) {
        if ("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject) data;
            try {
                onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("exit".equals(id)) {
            finish();
        }
        return null;
    }

    protected void onSaveInstanceState(Bundle outState) {
        this.cordovaInterface.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.appView != null) {
            PluginManager pm = this.appView.getPluginManager();
            if (pm != null) {
                pm.onConfigurationChanged(newConfig);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            this.cordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
        } catch (JSONException e) {
            LOG.m4d(TAG, "JSONException: Parameters fed into the method are not valid");
            e.printStackTrace();
        }
    }
}
