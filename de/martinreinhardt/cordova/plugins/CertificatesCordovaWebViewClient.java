package de.martinreinhardt.cordova.plugins;

import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;

public class CertificatesCordovaWebViewClient extends SystemWebViewClient {
    public static final String TAG = "CertificatesCordovaWebViewClient";
    private boolean allowUntrusted;

    public CertificatesCordovaWebViewClient(SystemWebViewEngine parentEngine) {
        super(parentEngine);
        this.allowUntrusted = false;
    }

    public boolean isAllowUntrusted() {
        return this.allowUntrusted;
    }

    public void setAllowUntrusted(boolean pAllowUntrusted) {
        this.allowUntrusted = pAllowUntrusted;
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Log.d(TAG, "onReceivedSslError. Proceed? " + isAllowUntrusted());
        if (isAllowUntrusted()) {
            handler.proceed();
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }
}
