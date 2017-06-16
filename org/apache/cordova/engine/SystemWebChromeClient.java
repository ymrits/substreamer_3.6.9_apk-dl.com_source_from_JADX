package org.apache.cordova.engine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import java.util.Arrays;
import org.apache.cordova.CordovaDialogsHelper;
import org.apache.cordova.CordovaDialogsHelper.Result;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;

public class SystemWebChromeClient extends WebChromeClient {
    private static final int FILECHOOSER_RESULTCODE = 5173;
    private static final String LOG_TAG = "SystemWebChromeClient";
    private long MAX_QUOTA;
    private Context appContext;
    private CordovaDialogsHelper dialogsHelper;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private View mVideoProgressView;
    protected final SystemWebViewEngine parentEngine;

    /* renamed from: org.apache.cordova.engine.SystemWebChromeClient.1 */
    class C03231 implements Result {
        final /* synthetic */ JsResult val$result;

        C03231(JsResult jsResult) {
            this.val$result = jsResult;
        }

        public void gotResult(boolean success, String value) {
            if (success) {
                this.val$result.confirm();
            } else {
                this.val$result.cancel();
            }
        }
    }

    /* renamed from: org.apache.cordova.engine.SystemWebChromeClient.2 */
    class C03242 implements Result {
        final /* synthetic */ JsResult val$result;

        C03242(JsResult jsResult) {
            this.val$result = jsResult;
        }

        public void gotResult(boolean success, String value) {
            if (success) {
                this.val$result.confirm();
            } else {
                this.val$result.cancel();
            }
        }
    }

    /* renamed from: org.apache.cordova.engine.SystemWebChromeClient.3 */
    class C03253 implements Result {
        final /* synthetic */ JsPromptResult val$result;

        C03253(JsPromptResult jsPromptResult) {
            this.val$result = jsPromptResult;
        }

        public void gotResult(boolean success, String value) {
            if (success) {
                this.val$result.confirm(value);
            } else {
                this.val$result.cancel();
            }
        }
    }

    /* renamed from: org.apache.cordova.engine.SystemWebChromeClient.4 */
    class C03264 extends CordovaPlugin {
        final /* synthetic */ ValueCallback val$uploadMsg;

        C03264(ValueCallback valueCallback) {
            this.val$uploadMsg = valueCallback;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            Uri result = (intent == null || resultCode != -1) ? null : intent.getData();
            LOG.m4d(SystemWebChromeClient.LOG_TAG, "Receive file chooser URL: " + result);
            this.val$uploadMsg.onReceiveValue(result);
        }
    }

    /* renamed from: org.apache.cordova.engine.SystemWebChromeClient.5 */
    class C03275 extends CordovaPlugin {
        final /* synthetic */ ValueCallback val$filePathsCallback;

        C03275(ValueCallback valueCallback) {
            this.val$filePathsCallback = valueCallback;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent intent) {
            Uri[] result = FileChooserParams.parseResult(resultCode, intent);
            LOG.m4d(SystemWebChromeClient.LOG_TAG, "Receive file chooser URL: " + result);
            this.val$filePathsCallback.onReceiveValue(result);
        }
    }

    public SystemWebChromeClient(SystemWebViewEngine parentEngine) {
        this.MAX_QUOTA = 104857600;
        this.parentEngine = parentEngine;
        this.appContext = parentEngine.webView.getContext();
        this.dialogsHelper = new CordovaDialogsHelper(this.appContext);
    }

    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        this.dialogsHelper.showAlert(message, new C03231(result));
        return true;
    }

    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        this.dialogsHelper.showConfirm(message, new C03242(result));
        return true;
    }

    public boolean onJsPrompt(WebView view, String origin, String message, String defaultValue, JsPromptResult result) {
        String handledRet = this.parentEngine.bridge.promptOnJsPrompt(origin, message, defaultValue);
        if (handledRet != null) {
            result.confirm(handledRet);
        } else {
            this.dialogsHelper.showPrompt(message, defaultValue, new C03253(result));
        }
        return true;
    }

    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, QuotaUpdater quotaUpdater) {
        LOG.m6d(LOG_TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", Long.valueOf(estimatedSize), Long.valueOf(currentQuota), Long.valueOf(totalUsedQuota));
        quotaUpdater.updateQuota(this.MAX_QUOTA);
    }

    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        if (VERSION.SDK_INT == 7) {
            LOG.m6d(LOG_TAG, "%s: Line %d : %s", sourceID, Integer.valueOf(lineNumber), message);
            super.onConsoleMessage(message, lineNumber, sourceID);
        }
    }

    @TargetApi(8)
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message() != null) {
            LOG.m6d(LOG_TAG, "%s: Line %d : %s", consoleMessage.sourceId(), Integer.valueOf(consoleMessage.lineNumber()), consoleMessage.message());
        }
        return super.onConsoleMessage(consoleMessage);
    }

    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
        CordovaPlugin geolocation = this.parentEngine.pluginManager.getPlugin("Geolocation");
        if (geolocation != null && !geolocation.hasPermisssion()) {
            geolocation.requestPermissions(0);
        }
    }

    public void onShowCustomView(View view, CustomViewCallback callback) {
        this.parentEngine.getCordovaWebView().showCustomView(view, callback);
    }

    public void onHideCustomView() {
        this.parentEngine.getCordovaWebView().hideCustomView();
    }

    public View getVideoLoadingProgressView() {
        if (this.mVideoProgressView == null) {
            LinearLayout layout = new LinearLayout(this.parentEngine.getView().getContext());
            layout.setOrientation(1);
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.addRule(13);
            layout.setLayoutParams(layoutParams);
            ProgressBar bar = new ProgressBar(this.parentEngine.getView().getContext());
            LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(-2, -2);
            barLayoutParams.gravity = 17;
            bar.setLayoutParams(barLayoutParams);
            layout.addView(bar);
            this.mVideoProgressView = layout;
        }
        return this.mVideoProgressView;
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "*/*");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("*/*");
        this.parentEngine.cordova.startActivityForResult(new C03264(uploadMsg), intent, FILECHOOSER_RESULTCODE);
    }

    @TargetApi(21)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathsCallback, FileChooserParams fileChooserParams) {
        try {
            this.parentEngine.cordova.startActivityForResult(new C03275(filePathsCallback), fileChooserParams.createIntent(), FILECHOOSER_RESULTCODE);
        } catch (Throwable e) {
            LOG.m19w("No activity found to handle file chooser intent.", e);
            filePathsCallback.onReceiveValue(null);
        }
        return true;
    }

    @TargetApi(21)
    public void onPermissionRequest(PermissionRequest request) {
        LOG.m4d(LOG_TAG, "onPermissionRequest: " + Arrays.toString(request.getResources()));
        request.grant(request.getResources());
    }

    public void destroyLastDialog() {
        this.dialogsHelper.destroyLastDialog();
    }
}
