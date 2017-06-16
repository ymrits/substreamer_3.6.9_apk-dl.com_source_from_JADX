package de.martinreinhardt.cordova.plugins;

import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONArray;
import org.json.JSONException;

public class CertificatesPlugin extends CordovaPlugin {
    private static final String LOG_TAG = "Certificates";
    private boolean allowUntrusted;

    /* renamed from: de.martinreinhardt.cordova.plugins.CertificatesPlugin.1 */
    class C01791 implements Runnable {
        C01791() {
        }

        public void run() {
            try {
                CordovaActivity ca = (CordovaActivity) CertificatesPlugin.this.cordova.getActivity();
                SystemWebView view = (SystemWebView) CertificatesPlugin.this.webView.getView();
                CertificatesCordovaWebViewClient cWebClient = new CertificatesCordovaWebViewClient((SystemWebViewEngine) CertificatesPlugin.this.webView.getEngine());
                cWebClient.setAllowUntrusted(CertificatesPlugin.this.allowUntrusted);
                CertificatesPlugin.this.webView.clearCache();
                view.setWebViewClient(cWebClient);
            } catch (Exception e) {
                Log.e(CertificatesPlugin.LOG_TAG, "Got unkown error during setting webview in activity", e);
            }
        }
    }

    public CertificatesPlugin() {
        this.allowUntrusted = false;
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setUntrusted")) {
            try {
                this.allowUntrusted = args.getBoolean(0);
                Log.d(LOG_TAG, "Setting allowUntrusted to " + this.allowUntrusted);
                this.cordova.getActivity().runOnUiThread(new C01791());
                callbackContext.success();
                return true;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Got unkown error during passing to UI Thread", e);
            }
        }
        callbackContext.error("Invalid Command");
        return false;
    }
}
