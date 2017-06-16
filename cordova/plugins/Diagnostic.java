package cordova.plugins;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.Log;
import com.ghenry22.substream2.C0173R;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.globalization.Globalization;
import org.apache.cordova.networkinformation.NetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Diagnostic extends CordovaPlugin {
    private static final String BLUETOOTH_STATE_POWERED_OFF = "powered_off";
    private static final String BLUETOOTH_STATE_POWERED_ON = "powered_on";
    private static final String BLUETOOTH_STATE_POWERING_OFF = "powering_off";
    private static final String BLUETOOTH_STATE_POWERING_ON = "powering_on";
    private static final String BLUETOOTH_STATE_UNKNOWN = "unknown";
    private static final Integer GET_EXTERNAL_SD_CARD_DETAILS_PERMISSION_REQUEST;
    private static final String LOCATION_MODE_BATTERY_SAVING = "battery_saving";
    private static final String LOCATION_MODE_DEVICE_ONLY = "device_only";
    private static final String LOCATION_MODE_HIGH_ACCURACY = "high_accuracy";
    private static final String LOCATION_MODE_OFF = "location_off";
    private static final String LOCATION_MODE_UNKNOWN = "unknown";
    public static final String NFC_STATE_OFF = "powered_off";
    public static final String NFC_STATE_ON = "powered_on";
    public static final String NFC_STATE_TURNING_OFF = "powering_off";
    public static final String NFC_STATE_TURNING_ON = "powering_on";
    public static final String NFC_STATE_UNKNOWN = "unknown";
    public static final int NFC_STATE_VALUE_OFF = 1;
    public static final int NFC_STATE_VALUE_ON = 3;
    public static final int NFC_STATE_VALUE_TURNING_OFF = 4;
    public static final int NFC_STATE_VALUE_TURNING_ON = 2;
    public static final int NFC_STATE_VALUE_UNKNOWN = 0;
    private static final String STATUS_DENIED = "DENIED";
    private static final String STATUS_GRANTED = "GRANTED";
    private static final String STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS = "STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS";
    public static final String TAG = "Diagnostic";
    private static String externalStoragePermission;
    private static String gpsLocationPermission;
    public static Diagnostic instance;
    public static LocationManager locationManager;
    private static String networkLocationPermission;
    public static NfcManager nfcManager;
    private static final Map<String, String> permissionsMap;
    private final BroadcastReceiver blueoothStateChangeReceiver;
    private boolean bluetoothListenerInitialized;
    private HashMap<String, CallbackContext> callbackContexts;
    protected CallbackContext currentContext;
    private String currentLocationMode;
    private String currentNFCState;
    private HashMap<String, JSONObject> permissionStatuses;

    /* renamed from: cordova.plugins.Diagnostic.1 */
    class C01771 implements Runnable {
        final /* synthetic */ String val$jsString;

        C01771(String str) {
            this.val$jsString = str;
        }

        public void run() {
            Diagnostic.this.webView.loadUrl("javascript:cordova.plugins.diagnostic." + this.val$jsString);
        }
    }

    /* renamed from: cordova.plugins.Diagnostic.2 */
    class C01782 extends BroadcastReceiver {
        C01782() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                String bluetoothState;
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", ExploreByTouchHelper.INVALID_ID)) {
                    case C0173R.styleable.Toolbar_contentInsetEndWithActions /*10*/:
                        bluetoothState = Diagnostic.NFC_STATE_OFF;
                        break;
                    case C0173R.styleable.Toolbar_popupTheme /*11*/:
                        bluetoothState = Diagnostic.NFC_STATE_TURNING_ON;
                        break;
                    case C0173R.styleable.Toolbar_titleTextAppearance /*12*/:
                        bluetoothState = Diagnostic.NFC_STATE_ON;
                        break;
                    case C0173R.styleable.Toolbar_subtitleTextAppearance /*13*/:
                        bluetoothState = Diagnostic.NFC_STATE_TURNING_OFF;
                        break;
                    default:
                        bluetoothState = Diagnostic.NFC_STATE_UNKNOWN;
                        break;
                }
                Diagnostic.instance.executeGlobalJavascript("_onBluetoothStateChange(\"" + bluetoothState + "\");");
            }
        }
    }

    public static class LocationProviderChangedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (Diagnostic.instance != null) {
                Log.v(Diagnostic.TAG, "onReceiveLocationProviderChange");
                Diagnostic.instance.notifyLocationStateChange();
            }
        }
    }

    public static class NFCStateChangedReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int stateValue = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", -1);
            if (Diagnostic.instance != null) {
                String state;
                switch (stateValue) {
                    case Diagnostic.NFC_STATE_VALUE_OFF /*1*/:
                        state = Diagnostic.NFC_STATE_OFF;
                        break;
                    case Diagnostic.NFC_STATE_VALUE_TURNING_ON /*2*/:
                        state = Diagnostic.NFC_STATE_TURNING_ON;
                        break;
                    case Diagnostic.NFC_STATE_VALUE_ON /*3*/:
                        state = Diagnostic.NFC_STATE_ON;
                        break;
                    case Diagnostic.NFC_STATE_VALUE_TURNING_OFF /*4*/:
                        state = Diagnostic.NFC_STATE_TURNING_OFF;
                        break;
                    default:
                        state = Diagnostic.NFC_STATE_UNKNOWN;
                        break;
                }
                Log.v(Diagnostic.TAG, "onReceiveNFCStateChange: " + state);
                Diagnostic.instance.notifyNFCStateChange(state);
            }
        }
    }

    static {
        Map<String, String> _permissionsMap = new HashMap();
        addBiDirMapEntry(_permissionsMap, "READ_CALENDAR", "android.permission.READ_CALENDAR");
        addBiDirMapEntry(_permissionsMap, "WRITE_CALENDAR", "android.permission.WRITE_CALENDAR");
        addBiDirMapEntry(_permissionsMap, "CAMERA", "android.permission.CAMERA");
        addBiDirMapEntry(_permissionsMap, "READ_CONTACTS", "android.permission.READ_CONTACTS");
        addBiDirMapEntry(_permissionsMap, "WRITE_CONTACTS", "android.permission.WRITE_CONTACTS");
        addBiDirMapEntry(_permissionsMap, "GET_ACCOUNTS", "android.permission.GET_ACCOUNTS");
        addBiDirMapEntry(_permissionsMap, "ACCESS_FINE_LOCATION", "android.permission.ACCESS_FINE_LOCATION");
        addBiDirMapEntry(_permissionsMap, "ACCESS_COARSE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION");
        addBiDirMapEntry(_permissionsMap, "RECORD_AUDIO", "android.permission.RECORD_AUDIO");
        addBiDirMapEntry(_permissionsMap, "READ_PHONE_STATE", "android.permission.READ_PHONE_STATE");
        addBiDirMapEntry(_permissionsMap, "CALL_PHONE", "android.permission.CALL_PHONE");
        addBiDirMapEntry(_permissionsMap, "ADD_VOICEMAIL", "com.android.voicemail.permission.ADD_VOICEMAIL");
        addBiDirMapEntry(_permissionsMap, "USE_SIP", "android.permission.USE_SIP");
        addBiDirMapEntry(_permissionsMap, "PROCESS_OUTGOING_CALLS", "android.permission.PROCESS_OUTGOING_CALLS");
        addBiDirMapEntry(_permissionsMap, "SEND_SMS", "android.permission.SEND_SMS");
        addBiDirMapEntry(_permissionsMap, "RECEIVE_SMS", "android.permission.RECEIVE_SMS");
        addBiDirMapEntry(_permissionsMap, "READ_SMS", "android.permission.READ_SMS");
        addBiDirMapEntry(_permissionsMap, "RECEIVE_WAP_PUSH", "android.permission.RECEIVE_WAP_PUSH");
        addBiDirMapEntry(_permissionsMap, "RECEIVE_MMS", "android.permission.RECEIVE_MMS");
        addBiDirMapEntry(_permissionsMap, "WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE");
        addBiDirMapEntry(_permissionsMap, "READ_CALL_LOG", "android.permission.READ_CALL_LOG");
        addBiDirMapEntry(_permissionsMap, "WRITE_CALL_LOG", "android.permission.WRITE_CALL_LOG");
        addBiDirMapEntry(_permissionsMap, "READ_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE");
        addBiDirMapEntry(_permissionsMap, "BODY_SENSORS", "android.permission.BODY_SENSORS");
        permissionsMap = Collections.unmodifiableMap(_permissionsMap);
        gpsLocationPermission = "ACCESS_FINE_LOCATION";
        networkLocationPermission = "ACCESS_COARSE_LOCATION";
        externalStoragePermission = "READ_EXTERNAL_STORAGE";
        GET_EXTERNAL_SD_CARD_DETAILS_PERMISSION_REQUEST = Integer.valueOf(PointerIconCompat.TYPE_DEFAULT);
        instance = null;
    }

    public Diagnostic() {
        this.callbackContexts = new HashMap();
        this.permissionStatuses = new HashMap();
        this.bluetoothListenerInitialized = false;
        this.currentLocationMode = null;
        this.currentNFCState = NFC_STATE_UNKNOWN;
        this.blueoothStateChangeReceiver = new C01782();
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.d(TAG, "initialize()");
        instance = this;
        locationManager = (LocationManager) this.cordova.getActivity().getSystemService("location");
        nfcManager = (NfcManager) this.cordova.getActivity().getApplicationContext().getSystemService("nfc");
        this.currentNFCState = isNFCAvailable() ? NFC_STATE_ON : NFC_STATE_OFF;
        super.initialize(cordova, webView);
    }

    public void onDestroy() {
        try {
            if (this.bluetoothListenerInitialized) {
                this.cordova.getActivity().unregisterReceiver(this.blueoothStateChangeReceiver);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to unregister Bluetooth receiver: " + e.getMessage());
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.currentContext = callbackContext;
        try {
            if (action.equals("switchToSettings")) {
                switchToAppSettings();
                callbackContext.success();
            } else if (action.equals("switchToLocationSettings")) {
                switchToLocationSettings();
                callbackContext.success();
            } else if (action.equals("switchToMobileDataSettings")) {
                switchToMobileDataSettings();
                callbackContext.success();
            } else if (action.equals("switchToBluetoothSettings")) {
                switchToBluetoothSettings();
                callbackContext.success();
            } else if (action.equals("switchToWifiSettings")) {
                switchToWifiSettings();
                callbackContext.success();
            } else if (action.equals("switchToWirelessSettings")) {
                switchToWirelessSettings();
                callbackContext.success();
            } else if (action.equals("switchToNFCSettings")) {
                switchToNFCSettings();
                callbackContext.success();
            } else if (action.equals("isLocationAvailable")) {
                r3 = (isGpsLocationAvailable() || isNetworkLocationAvailable()) ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN;
                callbackContext.success(r3);
            } else if (action.equals("isLocationEnabled")) {
                r3 = (isGpsLocationEnabled() || isNetworkLocationEnabled()) ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN;
                callbackContext.success(r3);
            } else if (action.equals("isGpsLocationAvailable")) {
                callbackContext.success(isGpsLocationAvailable() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isNetworkLocationAvailable")) {
                callbackContext.success(isNetworkLocationAvailable() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isGpsLocationEnabled")) {
                callbackContext.success(isGpsLocationEnabled() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isNetworkLocationEnabled")) {
                callbackContext.success(isNetworkLocationEnabled() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("getLocationMode")) {
                callbackContext.success(getLocationModeName());
            } else if (action.equals("isWifiAvailable")) {
                callbackContext.success(isWifiAvailable() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isCameraPresent")) {
                callbackContext.success(isCameraPresent() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isBluetoothAvailable")) {
                callbackContext.success(isBluetoothAvailable() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isBluetoothEnabled")) {
                callbackContext.success(isBluetoothEnabled() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("hasBluetoothSupport")) {
                callbackContext.success(hasBluetoothSupport() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("hasBluetoothLESupport")) {
                callbackContext.success(hasBluetoothLESupport() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("hasBluetoothLEPeripheralSupport")) {
                callbackContext.success(hasBluetoothLEPeripheralSupport() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("setWifiState")) {
                setWifiState(args.getBoolean(NFC_STATE_VALUE_UNKNOWN));
                callbackContext.success();
            } else if (action.equals("setBluetoothState")) {
                setBluetoothState(args.getBoolean(NFC_STATE_VALUE_UNKNOWN));
                callbackContext.success();
            } else if (action.equals("getBluetoothState")) {
                callbackContext.success(getBluetoothState());
            } else if (action.equals("initializeBluetoothListener")) {
                initializeBluetoothListener();
                callbackContext.success();
            } else if (action.equals("getPermissionAuthorizationStatus")) {
                getPermissionAuthorizationStatus(args);
            } else if (action.equals("getPermissionsAuthorizationStatus")) {
                getPermissionsAuthorizationStatus(args);
            } else if (action.equals("requestRuntimePermission")) {
                requestRuntimePermission(args);
            } else if (action.equals("requestRuntimePermissions")) {
                requestRuntimePermissions(args);
            } else if (action.equals("getExternalSdCardDetails")) {
                getExternalSdCardDetails();
            } else if (action.equals("isNFCPresent")) {
                callbackContext.success(isNFCPresent() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isNFCEnabled")) {
                callbackContext.success(isNFCEnabled() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else if (action.equals("isNFCAvailable")) {
                callbackContext.success(isNFCAvailable() ? NFC_STATE_VALUE_OFF : NFC_STATE_VALUE_UNKNOWN);
            } else {
                handleError("Invalid action");
                return false;
            }
            return true;
        } catch (Exception e) {
            handleError("Exception occurred: ".concat(e.getMessage()));
            return false;
        }
    }

    public boolean isGpsLocationAvailable() throws Exception {
        boolean result = isGpsLocationEnabled() && isLocationAuthorized();
        Log.d(TAG, "GPS location available: " + result);
        return result;
    }

    public boolean isGpsLocationEnabled() throws Exception {
        boolean result = true;
        int mode = getLocationMode();
        if (!(mode == NFC_STATE_VALUE_ON || mode == NFC_STATE_VALUE_OFF)) {
            result = false;
        }
        Log.d(TAG, "GPS location setting enabled: " + result);
        return result;
    }

    public boolean isNetworkLocationAvailable() throws Exception {
        boolean result = isNetworkLocationEnabled() && isLocationAuthorized();
        Log.d(TAG, "Network location available: " + result);
        return result;
    }

    public boolean isNetworkLocationEnabled() throws Exception {
        int mode = getLocationMode();
        boolean result = mode == NFC_STATE_VALUE_ON || mode == NFC_STATE_VALUE_TURNING_ON;
        Log.d(TAG, "Network location setting enabled: " + result);
        return result;
    }

    public String getLocationModeName() throws Exception {
        String modeName;
        switch (getLocationMode()) {
            case NFC_STATE_VALUE_UNKNOWN /*0*/:
                modeName = LOCATION_MODE_OFF;
                break;
            case NFC_STATE_VALUE_OFF /*1*/:
                modeName = LOCATION_MODE_DEVICE_ONLY;
                break;
            case NFC_STATE_VALUE_TURNING_ON /*2*/:
                modeName = LOCATION_MODE_BATTERY_SAVING;
                break;
            case NFC_STATE_VALUE_ON /*3*/:
                modeName = LOCATION_MODE_HIGH_ACCURACY;
                break;
            default:
                modeName = NFC_STATE_UNKNOWN;
                break;
        }
        this.currentLocationMode = modeName;
        return modeName;
    }

    public void notifyLocationStateChange() {
        try {
            if (!getLocationModeName().equals(this.currentLocationMode)) {
                Log.d(TAG, "Location mode change to: " + getLocationModeName());
                executeGlobalJavascript("_onLocationStateChange(\"" + getLocationModeName() + "\");");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving current location mode on location state change: " + e.toString());
        }
    }

    public boolean isWifiAvailable() {
        return ((WifiManager) this.cordova.getActivity().getSystemService(NetworkManager.WIFI)).isWifiEnabled();
    }

    public boolean isCameraPresent() {
        return this.cordova.getActivity().getPackageManager().hasSystemFeature("android.hardware.camera");
    }

    public boolean isBluetoothAvailable() {
        return hasBluetoothSupport() && isBluetoothEnabled();
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean hasBluetoothSupport() {
        return this.cordova.getActivity().getPackageManager().hasSystemFeature("android.hardware.bluetooth");
    }

    public boolean hasBluetoothLESupport() {
        return this.cordova.getActivity().getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    public boolean hasBluetoothLEPeripheralSupport() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isMultipleAdvertisementSupported();
    }

    public void switchToAppSettings() {
        Log.d(TAG, "Switch to App Settings");
        Intent appIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        appIntent.setData(Uri.fromParts("package", this.cordova.getActivity().getPackageName(), null));
        this.cordova.getActivity().startActivity(appIntent);
    }

    public void switchToLocationSettings() {
        Log.d(TAG, "Switch to Location Settings");
        this.cordova.getActivity().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
    }

    public void switchToMobileDataSettings() {
        Log.d(TAG, "Switch to Mobile Data Settings");
        this.cordova.getActivity().startActivity(new Intent("android.settings.DATA_ROAMING_SETTINGS"));
    }

    public void switchToBluetoothSettings() {
        Log.d(TAG, "Switch to Bluetooth Settings");
        this.cordova.getActivity().startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));
    }

    public void switchToWifiSettings() {
        Log.d(TAG, "Switch to Wifi Settings");
        this.cordova.getActivity().startActivity(new Intent("android.settings.WIFI_SETTINGS"));
    }

    public void switchToWirelessSettings() {
        Log.d(TAG, "Switch to wireless Settings");
        this.cordova.getActivity().startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
    }

    public void switchToNFCSettings() {
        Log.d(TAG, "Switch to NFC Settings");
        Intent settingsIntent = new Intent("android.settings.WIRELESS_SETTINGS");
        if (VERSION.SDK_INT >= 16) {
            settingsIntent = new Intent("android.settings.NFC_SETTINGS");
        }
        this.cordova.getActivity().startActivity(settingsIntent);
    }

    public void setWifiState(boolean enable) {
        WifiManager wifiManager = (WifiManager) this.cordova.getActivity().getSystemService(NetworkManager.WIFI);
        if (enable && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        } else if (!enable && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    public static boolean setBluetoothState(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        if (enable || !isEnabled) {
            return true;
        }
        return bluetoothAdapter.disable();
    }

    public String getBluetoothState() {
        String bluetoothState = NFC_STATE_UNKNOWN;
        if (!hasBluetoothSupport()) {
            return bluetoothState;
        }
        switch (BluetoothAdapter.getDefaultAdapter().getState()) {
            case C0173R.styleable.Toolbar_contentInsetEndWithActions /*10*/:
                return NFC_STATE_OFF;
            case C0173R.styleable.Toolbar_popupTheme /*11*/:
                return NFC_STATE_TURNING_ON;
            case C0173R.styleable.Toolbar_titleTextAppearance /*12*/:
                return NFC_STATE_ON;
            case C0173R.styleable.Toolbar_subtitleTextAppearance /*13*/:
                return NFC_STATE_TURNING_OFF;
            default:
                return bluetoothState;
        }
    }

    public void getPermissionsAuthorizationStatus(JSONArray args) throws Exception {
        this.currentContext.success(_getPermissionsAuthorizationStatus(jsonArrayToStringArray(args.getJSONArray(NFC_STATE_VALUE_UNKNOWN))));
    }

    public void getPermissionAuthorizationStatus(JSONArray args) throws Exception {
        String permission = args.getString(NFC_STATE_VALUE_UNKNOWN);
        JSONArray permissions = new JSONArray();
        permissions.put(permission);
        this.currentContext.success(_getPermissionsAuthorizationStatus(jsonArrayToStringArray(permissions)).getString(permission));
    }

    public void requestRuntimePermissions(JSONArray args) throws Exception {
        _requestRuntimePermissions(args.getJSONArray(NFC_STATE_VALUE_UNKNOWN), storeContextByRequestId());
    }

    public void requestRuntimePermission(JSONArray args) throws Exception {
        requestRuntimePermission(args.getString(NFC_STATE_VALUE_UNKNOWN));
    }

    public void requestRuntimePermission(String permission) throws Exception {
        requestRuntimePermission(permission, storeContextByRequestId());
    }

    public void requestRuntimePermission(String permission, int requestId) throws Exception {
        JSONArray permissions = new JSONArray();
        permissions.put(permission);
        _requestRuntimePermissions(permissions, requestId);
    }

    public void getExternalSdCardDetails() throws Exception {
        String permission = (String) permissionsMap.get(externalStoragePermission);
        if (hasPermission(permission)) {
            _getExternalSdCardDetails();
        } else {
            requestRuntimePermission(permission, GET_EXTERNAL_SD_CARD_DETAILS_PERMISSION_REQUEST.intValue());
        }
    }

    public boolean isNFCPresent() {
        try {
            return nfcManager.getDefaultAdapter() != null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public boolean isNFCEnabled() {
        try {
            NfcAdapter adapter = nfcManager.getDefaultAdapter();
            return adapter != null && adapter.isEnabled();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public boolean isNFCAvailable() {
        return isNFCPresent() && isNFCEnabled();
    }

    public void notifyNFCStateChange(String state) {
        try {
            if (state != this.currentNFCState) {
                Log.d(TAG, "NFC state changed to: " + state);
                executeGlobalJavascript("_onNFCStateChange(\"" + state + "\");");
                this.currentNFCState = state;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving current NFC state on state change: " + e.toString());
        }
    }

    private void handleError(String errorMsg, CallbackContext context) {
        try {
            Log.e(TAG, errorMsg);
            context.error(errorMsg);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void handleError(String errorMsg) {
        handleError(errorMsg, this.currentContext);
    }

    private void handleError(String errorMsg, int requestId) {
        CallbackContext context;
        String sRequestId = String.valueOf(requestId);
        if (this.callbackContexts.containsKey(sRequestId)) {
            context = (CallbackContext) this.callbackContexts.get(sRequestId);
        } else {
            context = this.currentContext;
        }
        handleError(errorMsg, context);
        clearRequest(requestId);
    }

    private int getLocationMode() throws Exception {
        if (VERSION.SDK_INT >= 19) {
            return Secure.getInt(this.cordova.getActivity().getContentResolver(), "location_mode");
        }
        if (isLocationProviderEnabled("gps") && isLocationProviderEnabled("network")) {
            return NFC_STATE_VALUE_ON;
        }
        if (isLocationProviderEnabled("gps")) {
            return NFC_STATE_VALUE_OFF;
        }
        if (isLocationProviderEnabled("network")) {
            return NFC_STATE_VALUE_TURNING_ON;
        }
        return NFC_STATE_VALUE_UNKNOWN;
    }

    private boolean isLocationAuthorized() throws Exception {
        boolean authorized = hasPermission((String) permissionsMap.get(gpsLocationPermission)) || hasPermission((String) permissionsMap.get(networkLocationPermission));
        Log.v(TAG, "Location permission is " + (authorized ? "authorized" : "unauthorized"));
        return authorized;
    }

    private boolean isLocationProviderEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    private void initializeBluetoothListener() {
        if (!this.bluetoothListenerInitialized) {
            this.cordova.getActivity().registerReceiver(this.blueoothStateChangeReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
            this.bluetoothListenerInitialized = true;
        }
    }

    private JSONObject _getPermissionsAuthorizationStatus(String[] permissions) throws Exception {
        JSONObject statuses = new JSONObject();
        int i = NFC_STATE_VALUE_UNKNOWN;
        while (i < permissions.length) {
            String permission = permissions[i];
            if (permissionsMap.containsKey(permission)) {
                String androidPermission = (String) permissionsMap.get(permission);
                Log.v(TAG, "Get authorisation status for " + androidPermission);
                if (hasPermission(androidPermission)) {
                    statuses.put(permission, STATUS_GRANTED);
                } else if (shouldShowRequestPermissionRationale(this.cordova.getActivity(), androidPermission)) {
                    statuses.put(permission, STATUS_DENIED);
                } else {
                    statuses.put(permission, STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS);
                }
                i += NFC_STATE_VALUE_OFF;
            } else {
                throw new Exception("Permission name '" + permission + "' is not a valid permission");
            }
        }
        return statuses;
    }

    private void _requestRuntimePermissions(JSONArray permissions, int requestId) throws Exception {
        JSONObject currentPermissionsStatuses = _getPermissionsAuthorizationStatus(jsonArrayToStringArray(permissions));
        JSONArray permissionsToRequest = new JSONArray();
        for (int i = NFC_STATE_VALUE_UNKNOWN; i < currentPermissionsStatuses.names().length(); i += NFC_STATE_VALUE_OFF) {
            String permission = currentPermissionsStatuses.names().getString(i);
            if (currentPermissionsStatuses.getString(permission) == STATUS_GRANTED) {
                Log.d(TAG, "Permission already granted for " + permission);
                JSONObject requestStatuses = (JSONObject) this.permissionStatuses.get(String.valueOf(requestId));
                requestStatuses.put(permission, STATUS_GRANTED);
                this.permissionStatuses.put(String.valueOf(requestId), requestStatuses);
            } else {
                String androidPermission = (String) permissionsMap.get(permission);
                Log.d(TAG, "Requesting permission for " + androidPermission);
                permissionsToRequest.put(androidPermission);
            }
        }
        if (permissionsToRequest.length() > 0) {
            Log.v(TAG, "Requesting permissions");
            requestPermissions(this, requestId, jsonArrayToStringArray(permissionsToRequest));
            return;
        }
        Log.d(TAG, "No permissions to request: returning result");
        sendRuntimeRequestResult(requestId);
    }

    private void sendRuntimeRequestResult(int requestId) {
        String sRequestId = String.valueOf(requestId);
        CallbackContext context = (CallbackContext) this.callbackContexts.get(sRequestId);
        JSONObject statuses = (JSONObject) this.permissionStatuses.get(sRequestId);
        Log.v(TAG, "Sending runtime request result for id=" + sRequestId);
        context.success(statuses);
    }

    private int storeContextByRequestId() {
        String requestId = generateRandomRequestId();
        this.callbackContexts.put(requestId, this.currentContext);
        this.permissionStatuses.put(requestId, new JSONObject());
        return Integer.valueOf(requestId).intValue();
    }

    private String generateRandomRequestId() {
        String requestId = null;
        while (requestId == null) {
            requestId = generateRandom();
            if (this.callbackContexts.containsKey(requestId)) {
                requestId = null;
            }
        }
        return requestId;
    }

    private String generateRandom() {
        return Integer.toString(new Random().nextInt(1000000) + NFC_STATE_VALUE_OFF);
    }

    private String[] jsonArrayToStringArray(JSONArray array) throws JSONException {
        if (array == null) {
            return null;
        }
        String[] arr = new String[array.length()];
        for (int i = NFC_STATE_VALUE_UNKNOWN; i < arr.length; i += NFC_STATE_VALUE_OFF) {
            arr[i] = array.optString(i);
        }
        return arr;
    }

    private CallbackContext getContextById(String requestId) throws Exception {
        if (this.callbackContexts.containsKey(requestId)) {
            return (CallbackContext) this.callbackContexts.get(requestId);
        }
        throw new Exception("No context found for request id=" + requestId);
    }

    private void clearRequest(int requestId) {
        String sRequestId = String.valueOf(requestId);
        if (this.callbackContexts.containsKey(sRequestId)) {
            this.callbackContexts.remove(sRequestId);
            this.permissionStatuses.remove(sRequestId);
        }
    }

    private static void addBiDirMapEntry(Map map, Object key, Object value) {
        map.put(key, value);
        map.put(value, key);
    }

    private boolean hasPermission(String permission) throws Exception {
        boolean hasPermission = true;
        try {
            Class[] clsArr = new Class[NFC_STATE_VALUE_OFF];
            clsArr[NFC_STATE_VALUE_UNKNOWN] = permission.getClass();
            Method method = this.cordova.getClass().getMethod("hasPermission", clsArr);
            CordovaInterface cordovaInterface = this.cordova;
            Object[] objArr = new Object[NFC_STATE_VALUE_OFF];
            objArr[NFC_STATE_VALUE_UNKNOWN] = permission;
            hasPermission = ((Boolean) method.invoke(cordovaInterface, objArr)).booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v6.2.1 does not support runtime permissions so defaulting to GRANTED for " + permission);
        }
        return hasPermission;
    }

    private void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) throws Exception {
        try {
            Class[] clsArr = new Class[NFC_STATE_VALUE_ON];
            clsArr[NFC_STATE_VALUE_UNKNOWN] = CordovaPlugin.class;
            clsArr[NFC_STATE_VALUE_OFF] = Integer.TYPE;
            clsArr[NFC_STATE_VALUE_TURNING_ON] = String[].class;
            Method method = this.cordova.getClass().getMethod("requestPermissions", clsArr);
            CordovaInterface cordovaInterface = this.cordova;
            Object[] objArr = new Object[NFC_STATE_VALUE_ON];
            objArr[NFC_STATE_VALUE_UNKNOWN] = plugin;
            objArr[NFC_STATE_VALUE_OFF] = Integer.valueOf(requestCode);
            objArr[NFC_STATE_VALUE_TURNING_ON] = permissions;
            method.invoke(cordovaInterface, objArr);
        } catch (NoSuchMethodException e) {
            throw new Exception("requestPermissions() method not found in CordovaInterface implementation of Cordova v6.2.1");
        }
    }

    private boolean shouldShowRequestPermissionRationale(Activity activity, String permission) throws Exception {
        try {
            Class[] clsArr = new Class[NFC_STATE_VALUE_TURNING_ON];
            clsArr[NFC_STATE_VALUE_UNKNOWN] = Activity.class;
            clsArr[NFC_STATE_VALUE_OFF] = String.class;
            Method method = ActivityCompat.class.getMethod("shouldShowRequestPermissionRationale", clsArr);
            Object[] objArr = new Object[NFC_STATE_VALUE_TURNING_ON];
            objArr[NFC_STATE_VALUE_UNKNOWN] = activity;
            objArr[NFC_STATE_VALUE_OFF] = permission;
            return ((Boolean) method.invoke(null, objArr)).booleanValue();
        } catch (NoSuchMethodException e) {
            throw new Exception("shouldShowRequestPermissionRationale() method not found in ActivityCompat class. Check you have Android Support Library v23+ installed");
        }
    }

    public void executeGlobalJavascript(String jsString) {
        this.cordova.getActivity().runOnUiThread(new C01771(jsString));
    }

    protected void _getExternalSdCardDetails() throws JSONException {
        String[] storageDirectories = getStorageDirectories();
        JSONArray details = new JSONArray();
        for (int i = NFC_STATE_VALUE_UNKNOWN; i < storageDirectories.length; i += NFC_STATE_VALUE_OFF) {
            String directory = storageDirectories[i];
            File f = new File(directory);
            JSONObject detail = new JSONObject();
            if (f.canRead()) {
                detail.put("path", directory);
                detail.put("filePath", "file://" + directory);
                detail.put("canWrite", f.canWrite());
                detail.put("freeSpace", getFreeSpaceInBytes(directory));
                if (directory.contains("Android")) {
                    detail.put(Globalization.TYPE, "application");
                } else {
                    detail.put(Globalization.TYPE, "root");
                }
                details.put(detail);
            }
        }
        this.currentContext.success(details);
    }

    private long getFreeSpaceInBytes(String path) {
        try {
            StatFs stat = new StatFs(path);
            return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public String[] getStorageDirectories() {
        int length;
        int i;
        int i2;
        List<String> results = new ArrayList();
        if (VERSION.SDK_INT >= 19) {
            File[] externalDirs = this.cordova.getActivity().getApplicationContext().getExternalFilesDirs(null);
            length = externalDirs.length;
            for (i = NFC_STATE_VALUE_UNKNOWN; i < length; i += NFC_STATE_VALUE_OFF) {
                boolean addPath;
                File file = externalDirs[i];
                String applicationPath = file.getPath();
                String rootPath = applicationPath.split("/Android")[NFC_STATE_VALUE_UNKNOWN];
                if (VERSION.SDK_INT >= 21) {
                    addPath = Environment.isExternalStorageRemovable(file);
                } else {
                    addPath = "mounted".equals(EnvironmentCompat.getStorageState(file));
                }
                if (addPath) {
                    results.add(rootPath);
                    results.add(applicationPath);
                }
            }
        }
        if (results.isEmpty()) {
            String output = BuildConfig.FLAVOR;
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(new String[NFC_STATE_VALUE_UNKNOWN]);
                String[] strArr = new String[NFC_STATE_VALUE_OFF];
                strArr[NFC_STATE_VALUE_UNKNOWN] = "mount | grep /dev/block/vold";
                Process process = processBuilder.command(strArr).redirectErrorStream(true).start();
                process.waitFor();
                InputStream is = process.getInputStream();
                byte[] buffer = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!output.trim().isEmpty()) {
                String[] devicePoints = output.split("\n");
                length = devicePoints.length;
                for (i = NFC_STATE_VALUE_UNKNOWN; i < length; i += NFC_STATE_VALUE_OFF) {
                    results.add(devicePoints[i].split(" ")[NFC_STATE_VALUE_TURNING_ON]);
                }
            }
        }
        int i3;
        if (VERSION.SDK_INT >= 23) {
            i2 = NFC_STATE_VALUE_UNKNOWN;
            while (i2 < results.size()) {
                if (!((String) results.get(i2)).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}.*")) {
                    Log.d(TAG, ((String) results.get(i2)) + " might not be extSDcard");
                    i3 = i2 - 1;
                    results.remove(i2);
                    i2 = i3;
                }
                i2 += NFC_STATE_VALUE_OFF;
            }
        } else {
            i2 = NFC_STATE_VALUE_UNKNOWN;
            while (i2 < results.size()) {
                if (!(((String) results.get(i2)).toLowerCase().contains("ext") || ((String) results.get(i2)).toLowerCase().contains("sdcard"))) {
                    Log.d(TAG, ((String) results.get(i2)) + " might not be extSDcard");
                    i3 = i2 - 1;
                    results.remove(i2);
                    i2 = i3;
                }
                i2 += NFC_STATE_VALUE_OFF;
            }
        }
        String[] storageDirectories = new String[results.size()];
        for (i2 = NFC_STATE_VALUE_UNKNOWN; i2 < results.size(); i2 += NFC_STATE_VALUE_OFF) {
            storageDirectories[i2] = (String) results.get(i2);
        }
        return storageDirectories;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        String sRequestId = String.valueOf(requestCode);
        Log.v(TAG, "Received result for permissions request id=" + sRequestId);
        try {
            CallbackContext context = getContextById(sRequestId);
            JSONObject statuses = (JSONObject) this.permissionStatuses.get(sRequestId);
            int len = permissions.length;
            for (int i = NFC_STATE_VALUE_UNKNOWN; i < len; i += NFC_STATE_VALUE_OFF) {
                String status;
                String androidPermission = permissions[i];
                String permission = (String) permissionsMap.get(androidPermission);
                if (grantResults[i] != -1) {
                    status = STATUS_GRANTED;
                } else if (shouldShowRequestPermissionRationale(this.cordova.getActivity(), androidPermission)) {
                    status = STATUS_DENIED;
                } else {
                    status = STATUS_NOT_REQUESTED_OR_DENIED_ALWAYS;
                }
                statuses.put(permission, status);
                Log.v(TAG, "Authorisation for " + permission + " is " + statuses.get(permission));
                clearRequest(requestCode);
            }
            if (requestCode == GET_EXTERNAL_SD_CARD_DETAILS_PERMISSION_REQUEST.intValue()) {
                _getExternalSdCardDetails();
            } else {
                context.success(statuses);
            }
        } catch (Exception e) {
            handleError("Exception occurred onRequestPermissionsResult: ".concat(e.getMessage()), requestCode);
        }
    }
}
