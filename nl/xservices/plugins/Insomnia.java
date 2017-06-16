package nl.xservices.plugins;

import android.support.v4.media.TransportMediator;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

public class Insomnia extends CordovaPlugin {
    private static final String ACTION_ALLOW_SLEEP_AGAIN = "allowSleepAgain";
    private static final String ACTION_KEEP_AWAKE = "keepAwake";

    /* renamed from: nl.xservices.plugins.Insomnia.1 */
    class C01861 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        C01861(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            Insomnia.this.cordova.getActivity().getWindow().addFlags(TransportMediator.FLAG_KEY_MEDIA_NEXT);
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK));
        }
    }

    /* renamed from: nl.xservices.plugins.Insomnia.2 */
    class C01872 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        C01872(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            Insomnia.this.cordova.getActivity().getWindow().clearFlags(TransportMediator.FLAG_KEY_MEDIA_NEXT);
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK));
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_KEEP_AWAKE.equals(action)) {
                this.cordova.getActivity().runOnUiThread(new C01861(callbackContext));
                return true;
            } else if (ACTION_ALLOW_SLEEP_AGAIN.equals(action)) {
                this.cordova.getActivity().runOnUiThread(new C01872(callbackContext));
                return true;
            } else {
                callbackContext.error("insomnia." + action + " is not a supported function. Did you mean '" + ACTION_KEEP_AWAKE + "'?");
                return false;
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            return false;
        }
    }
}
