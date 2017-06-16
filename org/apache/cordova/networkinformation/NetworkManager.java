package org.apache.cordova.networkinformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.util.Locale;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.globalization.Globalization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkManager extends CordovaPlugin {
    public static final String CDMA = "cdma";
    public static final String CELLULAR = "cellular";
    public static final String EDGE = "edge";
    public static final String EHRPD = "ehrpd";
    public static final String FOUR_G = "4g";
    public static final String GPRS = "gprs";
    public static final String GSM = "gsm";
    public static final String HSDPA = "hsdpa";
    public static final String HSPA = "hspa";
    public static final String HSPA_PLUS = "hspa+";
    public static final String HSUPA = "hsupa";
    private static final String LOG_TAG = "NetworkManager";
    public static final String LTE = "lte";
    public static final String MOBILE = "mobile";
    public static int NOT_REACHABLE = 0;
    public static final String ONEXRTT = "1xrtt";
    public static int REACHABLE_VIA_CARRIER_DATA_NETWORK = 0;
    public static int REACHABLE_VIA_WIFI_NETWORK = 0;
    public static final String THREE_G = "3g";
    public static final String TWO_G = "2g";
    public static final String TYPE_2G = "2g";
    public static final String TYPE_3G = "3g";
    public static final String TYPE_4G = "4g";
    public static final String TYPE_ETHERNET = "ethernet";
    public static final String TYPE_ETHERNET_SHORT = "eth";
    public static final String TYPE_NONE = "none";
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_WIFI = "wifi";
    public static final String UMB = "umb";
    public static final String UMTS = "umts";
    public static final String WIFI = "wifi";
    public static final String WIMAX = "wimax";
    private CallbackContext connectionCallbackContext;
    private JSONObject lastInfo;
    BroadcastReceiver receiver;
    ConnectivityManager sockMan;

    /* renamed from: org.apache.cordova.networkinformation.NetworkManager.1 */
    class C02541 extends BroadcastReceiver {
        C02541() {
        }

        public void onReceive(Context context, Intent intent) {
            if (NetworkManager.this.webView != null) {
                NetworkManager.this.updateConnectionInfo(NetworkManager.this.sockMan.getActiveNetworkInfo());
            }
        }
    }

    public NetworkManager() {
        this.lastInfo = null;
    }

    static {
        NOT_REACHABLE = 0;
        REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
        REACHABLE_VIA_WIFI_NETWORK = 2;
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.sockMan = (ConnectivityManager) cordova.getActivity().getSystemService("connectivity");
        this.connectionCallbackContext = null;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        if (this.receiver == null) {
            this.receiver = new C02541();
            webView.getContext().registerReceiver(this.receiver, intentFilter);
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (!action.equals("getConnectionInfo")) {
            return false;
        }
        this.connectionCallbackContext = callbackContext;
        NetworkInfo info = this.sockMan.getActiveNetworkInfo();
        String connectionType = BuildConfig.FLAVOR;
        try {
            connectionType = getConnectionInfo(info).get(Globalization.TYPE).toString();
        } catch (JSONException e) {
            LOG.m4d(LOG_TAG, e.getLocalizedMessage());
        }
        PluginResult pluginResult = new PluginResult(Status.OK, connectionType);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        return true;
    }

    public void onDestroy() {
        if (this.receiver != null) {
            try {
                this.webView.getContext().unregisterReceiver(this.receiver);
            } catch (Throwable e) {
                LOG.m8e(LOG_TAG, "Error unregistering network receiver: " + e.getMessage(), e);
            } finally {
                this.receiver = null;
            }
        }
    }

    private void updateConnectionInfo(NetworkInfo info) {
        JSONObject thisInfo = getConnectionInfo(info);
        if (!thisInfo.equals(this.lastInfo)) {
            String connectionType = BuildConfig.FLAVOR;
            try {
                connectionType = thisInfo.get(Globalization.TYPE).toString();
            } catch (JSONException e) {
                LOG.m4d(LOG_TAG, e.getLocalizedMessage());
            }
            sendUpdate(connectionType);
            this.lastInfo = thisInfo;
        }
    }

    private JSONObject getConnectionInfo(NetworkInfo info) {
        String type = TYPE_NONE;
        String extraInfo = BuildConfig.FLAVOR;
        if (info != null) {
            if (info.isConnected()) {
                type = getType(info);
            } else {
                type = TYPE_NONE;
            }
            extraInfo = info.getExtraInfo();
        }
        LOG.m4d(LOG_TAG, "Connection Type: " + type);
        LOG.m4d(LOG_TAG, "Connection Extra Info: " + extraInfo);
        JSONObject connectionInfo = new JSONObject();
        try {
            connectionInfo.put(Globalization.TYPE, type);
            connectionInfo.put("extraInfo", extraInfo);
        } catch (JSONException e) {
            LOG.m4d(LOG_TAG, e.getLocalizedMessage());
        }
        return connectionInfo;
    }

    private void sendUpdate(String type) {
        if (this.connectionCallbackContext != null) {
            PluginResult result = new PluginResult(Status.OK, type);
            result.setKeepCallback(true);
            this.connectionCallbackContext.sendPluginResult(result);
        }
        this.webView.postMessage("networkconnection", type);
    }

    private String getType(NetworkInfo info) {
        if (info == null) {
            return TYPE_NONE;
        }
        String type = info.getTypeName().toLowerCase(Locale.US);
        LOG.m4d(LOG_TAG, "toLower : " + type.toLowerCase());
        LOG.m4d(LOG_TAG, "wifi : wifi");
        if (type.equals(WIFI)) {
            return WIFI;
        }
        if (type.toLowerCase().equals(TYPE_ETHERNET) || type.toLowerCase().startsWith(TYPE_ETHERNET_SHORT)) {
            return TYPE_ETHERNET;
        }
        if (type.equals(MOBILE) || type.equals(CELLULAR)) {
            type = info.getSubtypeName().toLowerCase(Locale.US);
            if (type.equals(GSM) || type.equals(GPRS) || type.equals(EDGE) || type.equals(TYPE_2G)) {
                return TYPE_2G;
            }
            if (type.startsWith(CDMA) || type.equals(UMTS) || type.equals(ONEXRTT) || type.equals(EHRPD) || type.equals(HSUPA) || type.equals(HSDPA) || type.equals(HSPA) || type.equals(TYPE_3G)) {
                return TYPE_3G;
            }
            if (type.equals(LTE) || type.equals(UMB) || type.equals(HSPA_PLUS) || type.equals(TYPE_4G)) {
                return TYPE_4G;
            }
        }
        return TYPE_UNKNOWN;
    }
}
