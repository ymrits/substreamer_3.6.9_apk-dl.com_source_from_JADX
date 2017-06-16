package org.apache.cordova;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.MessagingStyle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout.LayoutParams;
import com.ghenry22.substream2.C0173R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.cordova.CordovaWebViewEngine.Client;
import org.apache.cordova.CordovaWebViewEngine.EngineView;
import org.apache.cordova.NativeToJsMessageQueue.LoadUrlBridgeMode;
import org.apache.cordova.NativeToJsMessageQueue.NoOpBridgeMode;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.apache.cordova.file.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class CordovaWebViewImpl implements CordovaWebView {
    static final /* synthetic */ boolean $assertionsDisabled;
    public static final String TAG = "CordovaWebViewImpl";
    private CoreAndroid appPlugin;
    private Set<Integer> boundKeyCodes;
    private CordovaInterface cordova;
    protected final CordovaWebViewEngine engine;
    private EngineClient engineClient;
    private boolean hasPausedEver;
    private int loadUrlTimeout;
    String loadedUrl;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private NativeToJsMessageQueue nativeToJsMessageQueue;
    private PluginManager pluginManager;
    private CordovaPreferences preferences;
    private CordovaResourceApi resourceApi;

    /* renamed from: org.apache.cordova.CordovaWebViewImpl.1 */
    class C02011 implements Runnable {
        final /* synthetic */ String val$url;

        C02011(String str) {
            this.val$url = str;
        }

        public void run() {
            CordovaWebViewImpl.this.stopLoading();
            LOG.m7e(CordovaWebViewImpl.TAG, "CordovaWebView: TIMEOUT ERROR!");
            JSONObject data = new JSONObject();
            try {
                data.put("errorCode", -6);
                data.put("description", "The connection to the server was unsuccessful.");
                data.put("url", this.val$url);
            } catch (JSONException e) {
            }
            CordovaWebViewImpl.this.pluginManager.postMessage("onReceivedError", data);
        }
    }

    /* renamed from: org.apache.cordova.CordovaWebViewImpl.2 */
    class C02022 implements Runnable {
        final /* synthetic */ int val$currentLoadUrlTimeout;
        final /* synthetic */ Runnable val$loadError;
        final /* synthetic */ int val$loadUrlTimeoutValue;

        C02022(int i, int i2, Runnable runnable) {
            this.val$loadUrlTimeoutValue = i;
            this.val$currentLoadUrlTimeout = i2;
            this.val$loadError = runnable;
        }

        public void run() {
            try {
                synchronized (this) {
                    wait((long) this.val$loadUrlTimeoutValue);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (CordovaWebViewImpl.this.loadUrlTimeout == this.val$currentLoadUrlTimeout) {
                CordovaWebViewImpl.this.cordova.getActivity().runOnUiThread(this.val$loadError);
            }
        }
    }

    /* renamed from: org.apache.cordova.CordovaWebViewImpl.3 */
    class C02033 implements Runnable {
        final /* synthetic */ boolean val$_recreatePlugins;
        final /* synthetic */ int val$loadUrlTimeoutValue;
        final /* synthetic */ Runnable val$timeoutCheck;
        final /* synthetic */ String val$url;

        C02033(int i, Runnable runnable, String str, boolean z) {
            this.val$loadUrlTimeoutValue = i;
            this.val$timeoutCheck = runnable;
            this.val$url = str;
            this.val$_recreatePlugins = z;
        }

        public void run() {
            if (this.val$loadUrlTimeoutValue > 0) {
                CordovaWebViewImpl.this.cordova.getThreadPool().execute(this.val$timeoutCheck);
            }
            CordovaWebViewImpl.this.engine.loadUrl(this.val$url, this.val$_recreatePlugins);
        }
    }

    protected class EngineClient implements Client {

        /* renamed from: org.apache.cordova.CordovaWebViewImpl.EngineClient.1 */
        class C02051 implements Runnable {

            /* renamed from: org.apache.cordova.CordovaWebViewImpl.EngineClient.1.1 */
            class C02041 implements Runnable {
                C02041() {
                }

                public void run() {
                    CordovaWebViewImpl.this.pluginManager.postMessage("spinner", "stop");
                }
            }

            C02051() {
            }

            public void run() {
                try {
                    Thread.sleep(2000);
                    CordovaWebViewImpl.this.cordova.getActivity().runOnUiThread(new C02041());
                } catch (InterruptedException e) {
                }
            }
        }

        protected EngineClient() {
        }

        public void clearLoadTimeoutTimer() {
            CordovaWebViewImpl.this.loadUrlTimeout = CordovaWebViewImpl.this.loadUrlTimeout + 1;
        }

        public void onPageStarted(String newUrl) {
            LOG.m4d(CordovaWebViewImpl.TAG, "onPageDidNavigate(" + newUrl + ")");
            CordovaWebViewImpl.this.boundKeyCodes.clear();
            CordovaWebViewImpl.this.pluginManager.onReset();
            CordovaWebViewImpl.this.pluginManager.postMessage("onPageStarted", newUrl);
        }

        public void onReceivedError(int errorCode, String description, String failingUrl) {
            clearLoadTimeoutTimer();
            JSONObject data = new JSONObject();
            try {
                data.put("errorCode", errorCode);
                data.put("description", description);
                data.put("url", failingUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CordovaWebViewImpl.this.pluginManager.postMessage("onReceivedError", data);
        }

        public void onPageFinishedLoading(String url) {
            LOG.m4d(CordovaWebViewImpl.TAG, "onPageFinished(" + url + ")");
            clearLoadTimeoutTimer();
            CordovaWebViewImpl.this.pluginManager.postMessage("onPageFinished", url);
            if (CordovaWebViewImpl.this.engine.getView().getVisibility() != 0) {
                new Thread(new C02051()).start();
            }
            if (url.equals("about:blank")) {
                CordovaWebViewImpl.this.pluginManager.postMessage("exit", null);
            }
        }

        public Boolean onDispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            boolean isBackButton = keyCode == 4 ? true : CordovaWebViewImpl.$assertionsDisabled;
            if (event.getAction() == 0) {
                if (isBackButton && CordovaWebViewImpl.this.mCustomView != null) {
                    return Boolean.valueOf(true);
                }
                if (CordovaWebViewImpl.this.boundKeyCodes.contains(Integer.valueOf(keyCode))) {
                    return Boolean.valueOf(true);
                }
                if (isBackButton) {
                    return Boolean.valueOf(CordovaWebViewImpl.this.engine.canGoBack());
                }
            } else if (event.getAction() == 1) {
                if (isBackButton && CordovaWebViewImpl.this.mCustomView != null) {
                    CordovaWebViewImpl.this.hideCustomView();
                    return Boolean.valueOf(true);
                } else if (CordovaWebViewImpl.this.boundKeyCodes.contains(Integer.valueOf(keyCode))) {
                    String eventName = null;
                    switch (keyCode) {
                        case FileUtils.READ /*4*/:
                            eventName = "backbutton";
                            break;
                        case C0173R.styleable.Toolbar_navigationIcon /*24*/:
                            eventName = "volumeupbutton";
                            break;
                        case MessagingStyle.MAXIMUM_RETAINED_MESSAGES /*25*/:
                            eventName = "volumedownbutton";
                            break;
                        case C0173R.styleable.AppCompatTheme_listChoiceBackgroundIndicator /*82*/:
                            eventName = "menubutton";
                            break;
                        case C0173R.styleable.AppCompatTheme_colorPrimaryDark /*84*/:
                            eventName = "searchbutton";
                            break;
                    }
                    if (eventName != null) {
                        CordovaWebViewImpl.this.sendJavascriptEvent(eventName);
                        return Boolean.valueOf(true);
                    }
                } else if (isBackButton) {
                    return Boolean.valueOf(CordovaWebViewImpl.this.engine.goBack());
                }
            }
            return null;
        }

        public boolean onNavigationAttempt(String url) {
            if (CordovaWebViewImpl.this.pluginManager.onOverrideUrlLoading(url)) {
                return true;
            }
            if (CordovaWebViewImpl.this.pluginManager.shouldAllowNavigation(url)) {
                return CordovaWebViewImpl.$assertionsDisabled;
            }
            if (CordovaWebViewImpl.this.pluginManager.shouldOpenExternalUrl(url).booleanValue()) {
                CordovaWebViewImpl.this.showWebPage(url, true, CordovaWebViewImpl.$assertionsDisabled, null);
                return true;
            }
            LOG.m16w(CordovaWebViewImpl.TAG, "Blocked (possibly sub-frame) navigation to non-allowed URL: " + url);
            return true;
        }
    }

    static {
        $assertionsDisabled = !CordovaWebViewImpl.class.desiredAssertionStatus() ? true : $assertionsDisabled;
    }

    public static CordovaWebViewEngine createEngine(Context context, CordovaPreferences preferences) {
        try {
            return (CordovaWebViewEngine) Class.forName(preferences.getString("webview", SystemWebViewEngine.class.getCanonicalName())).getConstructor(new Class[]{Context.class, CordovaPreferences.class}).newInstance(new Object[]{context, preferences});
        } catch (Exception e) {
            throw new RuntimeException("Failed to create webview. ", e);
        }
    }

    public CordovaWebViewImpl(CordovaWebViewEngine cordovaWebViewEngine) {
        this.loadUrlTimeout = 0;
        this.engineClient = new EngineClient();
        this.boundKeyCodes = new HashSet();
        this.engine = cordovaWebViewEngine;
    }

    public void init(CordovaInterface cordova) {
        init(cordova, new ArrayList(), new CordovaPreferences());
    }

    public void init(CordovaInterface cordova, List<PluginEntry> pluginEntries, CordovaPreferences preferences) {
        if (this.cordova != null) {
            throw new IllegalStateException();
        }
        this.cordova = cordova;
        this.preferences = preferences;
        this.pluginManager = new PluginManager(this, this.cordova, pluginEntries);
        this.resourceApi = new CordovaResourceApi(this.engine.getView().getContext(), this.pluginManager);
        this.nativeToJsMessageQueue = new NativeToJsMessageQueue();
        this.nativeToJsMessageQueue.addBridgeMode(new NoOpBridgeMode());
        this.nativeToJsMessageQueue.addBridgeMode(new LoadUrlBridgeMode(this.engine, cordova));
        if (preferences.getBoolean("DisallowOverscroll", $assertionsDisabled)) {
            this.engine.getView().setOverScrollMode(2);
        }
        this.engine.init(this, cordova, this.engineClient, this.resourceApi, this.pluginManager, this.nativeToJsMessageQueue);
        if ($assertionsDisabled || (this.engine.getView() instanceof EngineView)) {
            this.pluginManager.addService(CoreAndroid.PLUGIN_NAME, "org.apache.cordova.CoreAndroid");
            this.pluginManager.init();
            return;
        }
        throw new AssertionError();
    }

    public boolean isInitialized() {
        return this.cordova != null ? true : $assertionsDisabled;
    }

    public void loadUrlIntoView(String url, boolean recreatePlugins) {
        LOG.m4d(TAG, ">>> loadUrl(" + url + ")");
        if (url.equals("about:blank") || url.startsWith("javascript:")) {
            this.engine.loadUrl(url, $assertionsDisabled);
            return;
        }
        if (recreatePlugins || this.loadedUrl == null) {
            recreatePlugins = true;
        } else {
            recreatePlugins = $assertionsDisabled;
        }
        if (recreatePlugins) {
            if (this.loadedUrl != null) {
                this.appPlugin = null;
                this.pluginManager.init();
            }
            this.loadedUrl = url;
        }
        int currentLoadUrlTimeout = this.loadUrlTimeout;
        int loadUrlTimeoutValue = this.preferences.getInteger("LoadUrlTimeoutValue", 20000);
        this.cordova.getActivity().runOnUiThread(new C02033(loadUrlTimeoutValue, new C02022(loadUrlTimeoutValue, currentLoadUrlTimeout, new C02011(url)), url, recreatePlugins));
    }

    public void loadUrl(String url) {
        loadUrlIntoView(url, true);
    }

    public void showWebPage(String url, boolean openExternal, boolean clearHistory, Map<String, Object> map) {
        LOG.m6d(TAG, "showWebPage(%s, %b, %b, HashMap)", url, Boolean.valueOf(openExternal), Boolean.valueOf(clearHistory));
        if (clearHistory) {
            this.engine.clearHistory();
        }
        if (!openExternal) {
            if (this.pluginManager.shouldAllowNavigation(url)) {
                loadUrlIntoView(url, true);
            } else {
                LOG.m16w(TAG, "showWebPage: Refusing to load URL into webview since it is not in the <allow-navigation> whitelist. URL=" + url);
            }
        }
        if (this.pluginManager.shouldOpenExternalUrl(url).booleanValue()) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.BROWSABLE");
                Uri uri = Uri.parse(url);
                if ("file".equals(uri.getScheme())) {
                    intent.setDataAndType(uri, this.resourceApi.getMimeType(uri));
                } else {
                    intent.setData(uri);
                }
                this.cordova.getActivity().startActivity(intent);
                return;
            } catch (Throwable e) {
                LOG.m8e(TAG, "Error loading url " + url, e);
                return;
            }
        }
        LOG.m16w(TAG, "showWebPage: Refusing to send intent for URL since it is not in the <allow-intent> whitelist. URL=" + url);
    }

    @Deprecated
    public void showCustomView(View view, CustomViewCallback callback) {
        LOG.m4d(TAG, "showing Custom View");
        if (this.mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        this.mCustomView = view;
        this.mCustomViewCallback = callback;
        ViewGroup parent = (ViewGroup) this.engine.getView().getParent();
        parent.addView(view, new LayoutParams(-1, -1, 17));
        this.engine.getView().setVisibility(8);
        parent.setVisibility(0);
        parent.bringToFront();
    }

    @Deprecated
    public void hideCustomView() {
        if (this.mCustomView != null) {
            LOG.m4d(TAG, "Hiding Custom View");
            this.mCustomView.setVisibility(8);
            ((ViewGroup) this.engine.getView().getParent()).removeView(this.mCustomView);
            this.mCustomView = null;
            this.mCustomViewCallback.onCustomViewHidden();
            this.engine.getView().setVisibility(0);
        }
    }

    @Deprecated
    public boolean isCustomViewShowing() {
        return this.mCustomView != null ? true : $assertionsDisabled;
    }

    @Deprecated
    public void sendJavascript(String statement) {
        this.nativeToJsMessageQueue.addJavaScript(statement);
    }

    public void sendPluginResult(PluginResult cr, String callbackId) {
        this.nativeToJsMessageQueue.addPluginResult(cr, callbackId);
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public CordovaPreferences getPreferences() {
        return this.preferences;
    }

    public ICordovaCookieManager getCookieManager() {
        return this.engine.getCookieManager();
    }

    public CordovaResourceApi getResourceApi() {
        return this.resourceApi;
    }

    public CordovaWebViewEngine getEngine() {
        return this.engine;
    }

    public View getView() {
        return this.engine.getView();
    }

    public Context getContext() {
        return this.engine.getView().getContext();
    }

    private void sendJavascriptEvent(String event) {
        if (this.appPlugin == null) {
            this.appPlugin = (CoreAndroid) this.pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME);
        }
        if (this.appPlugin == null) {
            LOG.m16w(TAG, "Unable to fire event without existing plugin");
        } else {
            this.appPlugin.fireJavascriptEvent(event);
        }
    }

    public void setButtonPlumbedToJs(int keyCode, boolean override) {
        switch (keyCode) {
            case FileUtils.READ /*4*/:
            case C0173R.styleable.Toolbar_navigationIcon /*24*/:
            case MessagingStyle.MAXIMUM_RETAINED_MESSAGES /*25*/:
            case C0173R.styleable.AppCompatTheme_listChoiceBackgroundIndicator /*82*/:
                if (override) {
                    this.boundKeyCodes.add(Integer.valueOf(keyCode));
                } else {
                    this.boundKeyCodes.remove(Integer.valueOf(keyCode));
                }
            default:
                throw new IllegalArgumentException("Unsupported keycode: " + keyCode);
        }
    }

    public boolean isButtonPlumbedToJs(int keyCode) {
        return this.boundKeyCodes.contains(Integer.valueOf(keyCode));
    }

    public Object postMessage(String id, Object data) {
        return this.pluginManager.postMessage(id, data);
    }

    public String getUrl() {
        return this.engine.getUrl();
    }

    public void stopLoading() {
        this.loadUrlTimeout++;
    }

    public boolean canGoBack() {
        return this.engine.canGoBack();
    }

    public void clearCache() {
        this.engine.clearCache();
    }

    @Deprecated
    public void clearCache(boolean b) {
        this.engine.clearCache();
    }

    public void clearHistory() {
        this.engine.clearHistory();
    }

    public boolean backHistory() {
        return this.engine.goBack();
    }

    public void onNewIntent(Intent intent) {
        if (this.pluginManager != null) {
            this.pluginManager.onNewIntent(intent);
        }
    }

    public void handlePause(boolean keepRunning) {
        if (isInitialized()) {
            this.hasPausedEver = true;
            this.pluginManager.onPause(keepRunning);
            sendJavascriptEvent("pause");
            if (!keepRunning) {
                this.engine.setPaused(true);
            }
        }
    }

    public void handleResume(boolean keepRunning) {
        if (isInitialized()) {
            this.engine.setPaused($assertionsDisabled);
            this.pluginManager.onResume(keepRunning);
            if (this.hasPausedEver) {
                sendJavascriptEvent("resume");
            }
        }
    }

    public void handleStart() {
        if (isInitialized()) {
            this.pluginManager.onStart();
        }
    }

    public void handleStop() {
        if (isInitialized()) {
            this.pluginManager.onStop();
        }
    }

    public void handleDestroy() {
        if (isInitialized()) {
            this.loadUrlTimeout++;
            this.pluginManager.onDestroy();
            loadUrl("about:blank");
            this.engine.destroy();
            hideCustomView();
        }
    }
}
