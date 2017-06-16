package org.apache.cordova.inappbrowser;

import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

public class InAppChromeClient extends WebChromeClient {
    private String LOG_TAG;
    private long MAX_QUOTA;
    private CordovaWebView webView;

    public InAppChromeClient(CordovaWebView webView) {
        this.LOG_TAG = "InAppChromeClient";
        this.MAX_QUOTA = 104857600;
        this.webView = webView;
    }

    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, QuotaUpdater quotaUpdater) {
        LOG.m6d(this.LOG_TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", Long.valueOf(estimatedSize), Long.valueOf(currentQuota), Long.valueOf(totalUsedQuota));
        quotaUpdater.updateQuota(this.MAX_QUOTA);
    }

    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
    }

    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (defaultValue != null && defaultValue.startsWith("gap")) {
            if (defaultValue.startsWith("gap-iab://")) {
                String scriptCallbackId = defaultValue.substring(10);
                if (scriptCallbackId.startsWith("InAppBrowser")) {
                    PluginResult scriptResult;
                    if (message == null || message.length() == 0) {
                        scriptResult = new PluginResult(Status.OK, new JSONArray());
                    } else {
                        try {
                            scriptResult = new PluginResult(Status.OK, new JSONArray(message));
                        } catch (JSONException e) {
                            scriptResult = new PluginResult(Status.JSON_EXCEPTION, e.getMessage());
                        }
                    }
                    this.webView.sendPluginResult(scriptResult, scriptCallbackId);
                    result.confirm(BuildConfig.FLAVOR);
                    return true;
                }
            }
            LOG.m16w(this.LOG_TAG, "InAppBrowser does not support Cordova API calls: " + url + " " + defaultValue);
            result.cancel();
            return true;
        }
        return false;
    }
}
