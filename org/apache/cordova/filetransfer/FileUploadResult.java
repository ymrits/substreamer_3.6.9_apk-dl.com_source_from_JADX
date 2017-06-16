package org.apache.cordova.filetransfer;

import org.json.JSONException;
import org.json.JSONObject;

public class FileUploadResult {
    private long bytesSent;
    private String objectId;
    private String response;
    private int responseCode;

    public FileUploadResult() {
        this.bytesSent = 0;
        this.responseCode = -1;
        this.response = null;
        this.objectId = null;
    }

    public long getBytesSent() {
        return this.bytesSent;
    }

    public void setBytesSent(long bytes) {
        this.bytesSent = bytes;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject("{bytesSent:" + this.bytesSent + ",responseCode:" + this.responseCode + ",response:" + JSONObject.quote(this.response) + ",objectId:" + JSONObject.quote(this.objectId) + "}");
    }
}
