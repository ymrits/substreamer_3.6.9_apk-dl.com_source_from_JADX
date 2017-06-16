package org.apache.cordova;

import android.util.Base64;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class PluginResult {
    public static final int MESSAGE_TYPE_ARRAYBUFFER = 6;
    public static final int MESSAGE_TYPE_BINARYSTRING = 7;
    public static final int MESSAGE_TYPE_BOOLEAN = 4;
    public static final int MESSAGE_TYPE_JSON = 2;
    public static final int MESSAGE_TYPE_MULTIPART = 8;
    public static final int MESSAGE_TYPE_NULL = 5;
    public static final int MESSAGE_TYPE_NUMBER = 3;
    public static final int MESSAGE_TYPE_STRING = 1;
    public static String[] StatusMessages;
    private String encodedMessage;
    private boolean keepCallback;
    private final int messageType;
    private List<PluginResult> multipartMessages;
    private final int status;
    private String strMessage;

    public enum Status {
        NO_RESULT,
        OK,
        CLASS_NOT_FOUND_EXCEPTION,
        ILLEGAL_ACCESS_EXCEPTION,
        INSTANTIATION_EXCEPTION,
        MALFORMED_URL_EXCEPTION,
        IO_EXCEPTION,
        INVALID_ACTION,
        JSON_EXCEPTION,
        ERROR
    }

    public PluginResult(Status status) {
        this(status, StatusMessages[status.ordinal()]);
    }

    public PluginResult(Status status, String message) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = message == null ? MESSAGE_TYPE_NULL : MESSAGE_TYPE_STRING;
        this.strMessage = message;
    }

    public PluginResult(Status status, JSONArray message) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_JSON;
        this.encodedMessage = message.toString();
    }

    public PluginResult(Status status, JSONObject message) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_JSON;
        this.encodedMessage = message.toString();
    }

    public PluginResult(Status status, int i) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_NUMBER;
        this.encodedMessage = BuildConfig.FLAVOR + i;
    }

    public PluginResult(Status status, float f) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_NUMBER;
        this.encodedMessage = BuildConfig.FLAVOR + f;
    }

    public PluginResult(Status status, boolean b) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_BOOLEAN;
        this.encodedMessage = Boolean.toString(b);
    }

    public PluginResult(Status status, byte[] data) {
        this(status, data, false);
    }

    public PluginResult(Status status, byte[] data, boolean binaryString) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = binaryString ? MESSAGE_TYPE_BINARYSTRING : MESSAGE_TYPE_ARRAYBUFFER;
        this.encodedMessage = Base64.encodeToString(data, MESSAGE_TYPE_JSON);
    }

    public PluginResult(Status status, List<PluginResult> multipartMessages) {
        this.keepCallback = false;
        this.status = status.ordinal();
        this.messageType = MESSAGE_TYPE_MULTIPART;
        this.multipartMessages = multipartMessages;
    }

    public void setKeepCallback(boolean b) {
        this.keepCallback = b;
    }

    public int getStatus() {
        return this.status;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public String getMessage() {
        if (this.encodedMessage == null) {
            this.encodedMessage = JSONObject.quote(this.strMessage);
        }
        return this.encodedMessage;
    }

    public int getMultipartMessagesSize() {
        return this.multipartMessages.size();
    }

    public PluginResult getMultipartMessage(int index) {
        return (PluginResult) this.multipartMessages.get(index);
    }

    public String getStrMessage() {
        return this.strMessage;
    }

    public boolean getKeepCallback() {
        return this.keepCallback;
    }

    @Deprecated
    public String getJSONString() {
        return "{\"status\":" + this.status + ",\"message\":" + getMessage() + ",\"keepCallback\":" + this.keepCallback + "}";
    }

    @Deprecated
    public String toCallbackString(String callbackId) {
        if (this.status == Status.NO_RESULT.ordinal() && this.keepCallback) {
            return null;
        }
        if (this.status == Status.OK.ordinal() || this.status == Status.NO_RESULT.ordinal()) {
            return toSuccessCallbackString(callbackId);
        }
        return toErrorCallbackString(callbackId);
    }

    @Deprecated
    public String toSuccessCallbackString(String callbackId) {
        return "cordova.callbackSuccess('" + callbackId + "'," + getJSONString() + ");";
    }

    @Deprecated
    public String toErrorCallbackString(String callbackId) {
        return "cordova.callbackError('" + callbackId + "', " + getJSONString() + ");";
    }

    static {
        StatusMessages = new String[]{"No result", "OK", "Class not found", "Illegal access", "Instantiation error", "Malformed url", "IO error", "Invalid action", "JSON error", "Error"};
    }
}
