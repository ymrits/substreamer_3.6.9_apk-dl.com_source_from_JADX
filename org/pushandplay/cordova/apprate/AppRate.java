package org.pushandplay.cordova.apprate;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class AppRate extends CordovaPlugin {
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            PackageManager packageManager = this.cordova.getActivity().getPackageManager();
            if (action.equals("getAppVersion")) {
                callbackContext.success(packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), 0).versionName);
                return true;
            } else if (!action.equals("getAppTitle")) {
                return false;
            } else {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.cordova.getActivity().getApplicationContext().getApplicationInfo().packageName, 0);
                callbackContext.success((String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown"));
                return true;
            }
        } catch (NameNotFoundException e) {
            callbackContext.success("N/A");
            return true;
        }
    }
}
