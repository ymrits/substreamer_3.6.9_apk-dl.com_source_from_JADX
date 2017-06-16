package org.apache.cordova.filetransfer;

import org.json.JSONException;
import org.json.JSONObject;

public class FileProgressResult {
    private boolean lengthComputable;
    private long loaded;
    private long total;

    public FileProgressResult() {
        this.lengthComputable = false;
        this.loaded = 0;
        this.total = 0;
    }

    public boolean getLengthComputable() {
        return this.lengthComputable;
    }

    public void setLengthComputable(boolean computable) {
        this.lengthComputable = computable;
    }

    public long getLoaded() {
        return this.loaded;
    }

    public void setLoaded(long bytes) {
        this.loaded = bytes;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long bytes) {
        this.total = bytes;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject("{loaded:" + this.loaded + ",total:" + this.total + ",lengthComputable:" + (this.lengthComputable ? "true" : "false") + "}");
    }
}
