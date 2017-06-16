package org.apache.cordova.engine;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.cordova.CordovaBridge;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewEngine.Client;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.LOG;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.NativeToJsMessageQueue.EvalBridgeMode;
import org.apache.cordova.NativeToJsMessageQueue.OnlineEventsBridgeMode;
import org.apache.cordova.NativeToJsMessageQueue.OnlineEventsBridgeMode.OnlineEventsBridgeModeDelegate;
import org.apache.cordova.PluginManager;

public class SystemWebViewEngine implements CordovaWebViewEngine {
    public static final String TAG = "SystemWebViewEngine";
    protected CordovaBridge bridge;
    protected Client client;
    protected final SystemCookieManager cookieManager;
    protected CordovaInterface cordova;
    protected NativeToJsMessageQueue nativeToJsMessageQueue;
    protected CordovaWebView parentWebView;
    protected PluginManager pluginManager;
    protected CordovaPreferences preferences;
    private BroadcastReceiver receiver;
    protected CordovaResourceApi resourceApi;
    protected final SystemWebView webView;

    /* renamed from: org.apache.cordova.engine.SystemWebViewEngine.2 */
    class C02332 extends BroadcastReceiver {
        final /* synthetic */ WebSettings val$settings;

        C02332(WebSettings webSettings) {
            this.val$settings = webSettings;
        }

        public void onReceive(Context context, Intent intent) {
            this.val$settings.getUserAgentString();
        }
    }

    /* renamed from: org.apache.cordova.engine.SystemWebViewEngine.1 */
    class C03281 implements OnlineEventsBridgeModeDelegate {
        C03281() {
        }

        public void setNetworkAvailable(boolean value) {
            SystemWebViewEngine.this.webView.setNetworkAvailable(value);
        }

        public void runOnUiThread(Runnable r) {
            SystemWebViewEngine.this.cordova.getActivity().runOnUiThread(r);
        }
    }

    public SystemWebViewEngine(Context context, CordovaPreferences preferences) {
        this(new SystemWebView(context), preferences);
    }

    public SystemWebViewEngine(SystemWebView webView) {
        this(webView, null);
    }

    public SystemWebViewEngine(SystemWebView webView, CordovaPreferences preferences) {
        this.preferences = preferences;
        this.webView = webView;
        this.cookieManager = new SystemCookieManager(webView);
    }

    public void init(CordovaWebView parentWebView, CordovaInterface cordova, Client client, CordovaResourceApi resourceApi, PluginManager pluginManager, NativeToJsMessageQueue nativeToJsMessageQueue) {
        if (this.cordova != null) {
            throw new IllegalStateException();
        }
        if (this.preferences == null) {
            this.preferences = parentWebView.getPreferences();
        }
        this.parentWebView = parentWebView;
        this.cordova = cordova;
        this.client = client;
        this.resourceApi = resourceApi;
        this.pluginManager = pluginManager;
        this.nativeToJsMessageQueue = nativeToJsMessageQueue;
        this.webView.init(this, cordova);
        initWebViewSettings();
        nativeToJsMessageQueue.addBridgeMode(new OnlineEventsBridgeMode(new C03281()));
        if (VERSION.SDK_INT > 18) {
            nativeToJsMessageQueue.addBridgeMode(new EvalBridgeMode(this, cordova));
        }
        this.bridge = new CordovaBridge(pluginManager, nativeToJsMessageQueue);
        exposeJsInterface(this.webView, this.bridge);
    }

    public CordovaWebView getCordovaWebView() {
        return this.parentWebView;
    }

    public ICordovaCookieManager getCookieManager() {
        return this.cookieManager;
    }

    public View getView() {
        return this.webView;
    }

    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    private void initWebViewSettings() {
        this.webView.setInitialScale(0);
        this.webView.setVerticalScrollBarEnabled(false);
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        try {
            Method gingerbread_getMethod = WebSettings.class.getMethod("setNavDump", new Class[]{Boolean.TYPE});
            LOG.m4d(TAG, "CordovaWebView is running on device made by: " + Build.MANUFACTURER);
            if (VERSION.SDK_INT < 11 && Build.MANUFACTURER.contains("HTC")) {
                gingerbread_getMethod.invoke(settings, new Object[]{Boolean.valueOf(true)});
            }
        } catch (NoSuchMethodException e) {
            LOG.m4d(TAG, "We are on a modern version of Android, we will deprecate HTC 2.3 devices in 2.8");
        } catch (IllegalArgumentException e2) {
            LOG.m4d(TAG, "Doing the NavDump failed with bad arguments");
        } catch (IllegalAccessException e3) {
            LOG.m4d(TAG, "This should never happen: IllegalAccessException means this isn't Android anymore");
        } catch (InvocationTargetException e4) {
            LOG.m4d(TAG, "This should never happen: InvocationTargetException means this isn't Android anymore.");
        }
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        if (VERSION.SDK_INT >= 16) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (VERSION.SDK_INT >= 17) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }
        String databasePath = this.webView.getContext().getApplicationContext().getDir("database", 0).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);
        if ((this.webView.getContext().getApplicationContext().getApplicationInfo().flags & 2) != 0 && VERSION.SDK_INT >= 19) {
            enableRemoteDebugging();
        }
        settings.setGeolocationDatabasePath(databasePath);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAppCacheMaxSize(5242880);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        String defaultUserAgent = settings.getUserAgentString();
        String overrideUserAgent = this.preferences.getString("OverrideUserAgent", null);
        if (overrideUserAgent != null) {
            settings.setUserAgentString(overrideUserAgent);
        } else {
            String appendUserAgent = this.preferences.getString("AppendUserAgent", null);
            if (appendUserAgent != null) {
                settings.setUserAgentString(defaultUserAgent + " " + appendUserAgent);
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        if (this.receiver == null) {
            this.receiver = new C02332(settings);
            this.webView.getContext().registerReceiver(this.receiver, intentFilter);
        }
    }

    @TargetApi(19)
    private void enableRemoteDebugging() {
        try {
            WebView.setWebContentsDebuggingEnabled(true);
        } catch (IllegalArgumentException e) {
            LOG.m4d(TAG, "You have one job! To turn on Remote Web Debugging! YOU HAVE FAILED! ");
            e.printStackTrace();
        }
    }

    private static void exposeJsInterface(WebView webView, CordovaBridge bridge) {
        if (VERSION.SDK_INT < 17) {
            LOG.m10i(TAG, "Disabled addJavascriptInterface() bridge since Android version is old.");
        } else {
            webView.addJavascriptInterface(new SystemExposedJsApi(bridge), "_cordovaNative");
        }
    }

    public void loadUrl(String url, boolean clearNavigationStack) {
        this.webView.loadUrl(url);
    }

    public String getUrl() {
        return this.webView.getUrl();
    }

    public void stopLoading() {
        this.webView.stopLoading();
    }

    public void clearCache() {
        this.webView.clearCache(true);
    }

    public void clearHistory() {
        this.webView.clearHistory();
    }

    public boolean canGoBack() {
        return this.webView.canGoBack();
    }

    public boolean goBack() {
        if (!this.webView.canGoBack()) {
            return false;
        }
        this.webView.goBack();
        return true;
    }

    public void setPaused(boolean value) {
        if (value) {
            this.webView.onPause();
            this.webView.pauseTimers();
            return;
        }
        this.webView.onResume();
        this.webView.resumeTimers();
    }

    public void destroy() {
        this.webView.chromeClient.destroyLastDialog();
        this.webView.destroy();
        if (this.receiver != null) {
            try {
                this.webView.getContext().unregisterReceiver(this.receiver);
            } catch (Throwable e) {
                LOG.m8e(TAG, "Error unregistering configuration receiver: " + e.getMessage(), e);
            }
        }
    }

    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        if (VERSION.SDK_INT >= 19) {
            this.webView.evaluateJavascript(js, callback);
        } else {
            LOG.m4d(TAG, "This webview is using the old bridge");
        }
    }
}
