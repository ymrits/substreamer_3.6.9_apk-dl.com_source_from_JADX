package org.apache.cordova;

import java.util.ArrayList;
import java.util.List;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONException;
import org.json.JSONObject;

public class ResumeCallback extends CallbackContext {
    private final String TAG;
    private PluginManager pluginManager;
    private String serviceName;

    public ResumeCallback(String serviceName, PluginManager pluginManager) {
        super("resumecallback", null);
        this.TAG = "CordovaResumeCallback";
        this.serviceName = serviceName;
        this.pluginManager = pluginManager;
    }

    public void sendPluginResult(PluginResult pluginResult) {
        synchronized (this) {
            if (this.finished) {
                LOG.m16w("CordovaResumeCallback", this.serviceName + " attempted to send a second callback to ResumeCallback\nResult was: " + pluginResult.getMessage());
                return;
            }
            this.finished = true;
            JSONObject event = new JSONObject();
            JSONObject pluginResultObject = new JSONObject();
            try {
                pluginResultObject.put("pluginServiceName", this.serviceName);
                pluginResultObject.put("pluginStatus", PluginResult.StatusMessages[pluginResult.getStatus()]);
                event.put("action", "resume");
                event.put("pendingResult", pluginResultObject);
            } catch (JSONException e) {
                LOG.m7e("CordovaResumeCallback", "Unable to create resume object for Activity Result");
            }
            PluginResult eventResult = new PluginResult(Status.OK, event);
            List result = new ArrayList();
            result.add(eventResult);
            result.add(pluginResult);
            ((CoreAndroid) this.pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME)).sendResumeEvent(new PluginResult(Status.OK, result));
        }
    }
}
