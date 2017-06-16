package org.apache.cordova.inappbrowser;

import android.app.Dialog;
import android.content.Context;

public class InAppBrowserDialog extends Dialog {
    Context context;
    InAppBrowser inAppBrowser;

    public InAppBrowserDialog(Context context, int theme) {
        super(context, theme);
        this.inAppBrowser = null;
        this.context = context;
    }

    public void setInAppBroswer(InAppBrowser browser) {
        this.inAppBrowser = browser;
    }

    public void onBackPressed() {
        if (this.inAppBrowser == null) {
            dismiss();
        } else if (this.inAppBrowser.hardwareBack() && this.inAppBrowser.canGoBack()) {
            this.inAppBrowser.goBack();
        } else {
            this.inAppBrowser.closeDialog();
        }
    }
}
