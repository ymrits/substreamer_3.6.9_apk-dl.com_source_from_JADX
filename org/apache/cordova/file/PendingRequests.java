package org.apache.cordova.file;

import android.util.SparseArray;
import org.apache.cordova.CallbackContext;

class PendingRequests {
    private int currentReqId;
    private SparseArray<Request> requests;

    public class Request {
        private int action;
        private CallbackContext callbackContext;
        private String rawArgs;
        private int requestCode;

        private Request(String rawArgs, int action, CallbackContext callbackContext) {
            this.rawArgs = rawArgs;
            this.action = action;
            this.callbackContext = callbackContext;
            this.requestCode = PendingRequests.this.currentReqId = PendingRequests.this.currentReqId + 1;
        }

        public int getAction() {
            return this.action;
        }

        public String getRawArgs() {
            return this.rawArgs;
        }

        public CallbackContext getCallbackContext() {
            return this.callbackContext;
        }
    }

    PendingRequests() {
        this.currentReqId = 0;
        this.requests = new SparseArray();
    }

    public synchronized int createRequest(String rawArgs, int action, CallbackContext callbackContext) {
        Request req;
        req = new Request(rawArgs, action, callbackContext, null);
        this.requests.put(req.requestCode, req);
        return req.requestCode;
    }

    public synchronized Request getAndRemove(int requestCode) {
        Request result;
        result = (Request) this.requests.get(requestCode);
        this.requests.remove(requestCode);
        return result;
    }
}
