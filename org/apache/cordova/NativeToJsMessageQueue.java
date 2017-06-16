package org.apache.cordova;

import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.file.FileUtils;

public class NativeToJsMessageQueue {
    static final boolean DISABLE_EXEC_CHAINING = false;
    private static final boolean FORCE_ENCODE_USING_EVAL = false;
    private static final String LOG_TAG = "JsMessageQueue";
    private static int MAX_PAYLOAD_SIZE;
    private BridgeMode activeBridgeMode;
    private ArrayList<BridgeMode> bridgeModes;
    private boolean paused;
    private final LinkedList<JsMessage> queue;

    public static abstract class BridgeMode {
        public abstract void onNativeToJsMessageAvailable(NativeToJsMessageQueue nativeToJsMessageQueue);

        public void notifyOfFlush(NativeToJsMessageQueue queue, boolean fromOnlineEvent) {
        }

        public void reset() {
        }
    }

    private static class JsMessage {
        final String jsPayloadOrCallbackId;
        final PluginResult pluginResult;

        JsMessage(String js) {
            if (js == null) {
                throw new NullPointerException();
            }
            this.jsPayloadOrCallbackId = js;
            this.pluginResult = null;
        }

        JsMessage(PluginResult pluginResult, String callbackId) {
            if (callbackId == null || pluginResult == null) {
                throw new NullPointerException();
            }
            this.jsPayloadOrCallbackId = callbackId;
            this.pluginResult = pluginResult;
        }

        static int calculateEncodedLengthHelper(PluginResult pluginResult) {
            switch (pluginResult.getMessageType()) {
                case FileUtils.ACTION_WRITE /*1*/:
                    return pluginResult.getStrMessage().length() + 1;
                case FileUtils.WRITE /*3*/:
                    return pluginResult.getMessage().length() + 1;
                case FileUtils.READ /*4*/:
                case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                    return 1;
                case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                    return pluginResult.getMessage().length() + 1;
                case PluginResult.MESSAGE_TYPE_BINARYSTRING /*7*/:
                    return pluginResult.getMessage().length() + 1;
                case TransportMediator.FLAG_KEY_MEDIA_PLAY_PAUSE /*8*/:
                    int ret = 1;
                    for (int i = 0; i < pluginResult.getMultipartMessagesSize(); i++) {
                        int length = calculateEncodedLengthHelper(pluginResult.getMultipartMessage(i));
                        ret += (String.valueOf(length).length() + 1) + length;
                    }
                    return ret;
                default:
                    return pluginResult.getMessage().length();
            }
        }

        int calculateEncodedLength() {
            if (this.pluginResult == null) {
                return this.jsPayloadOrCallbackId.length() + 1;
            }
            return calculateEncodedLengthHelper(this.pluginResult) + ((((String.valueOf(this.pluginResult.getStatus()).length() + 2) + 1) + this.jsPayloadOrCallbackId.length()) + 1);
        }

        static void encodeAsMessageHelper(StringBuilder sb, PluginResult pluginResult) {
            switch (pluginResult.getMessageType()) {
                case FileUtils.ACTION_WRITE /*1*/:
                    sb.append('s');
                    sb.append(pluginResult.getStrMessage());
                case FileUtils.WRITE /*3*/:
                    sb.append('n').append(pluginResult.getMessage());
                case FileUtils.READ /*4*/:
                    sb.append(pluginResult.getMessage().charAt(0));
                case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                    sb.append('N');
                case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                    sb.append('A');
                    sb.append(pluginResult.getMessage());
                case PluginResult.MESSAGE_TYPE_BINARYSTRING /*7*/:
                    sb.append('S');
                    sb.append(pluginResult.getMessage());
                case TransportMediator.FLAG_KEY_MEDIA_PLAY_PAUSE /*8*/:
                    sb.append('M');
                    for (int i = 0; i < pluginResult.getMultipartMessagesSize(); i++) {
                        PluginResult multipartMessage = pluginResult.getMultipartMessage(i);
                        sb.append(String.valueOf(calculateEncodedLengthHelper(multipartMessage)));
                        sb.append(' ');
                        encodeAsMessageHelper(sb, multipartMessage);
                    }
                default:
                    sb.append(pluginResult.getMessage());
            }
        }

        void encodeAsMessage(StringBuilder sb) {
            if (this.pluginResult == null) {
                sb.append('J').append(this.jsPayloadOrCallbackId);
                return;
            }
            boolean noResult;
            boolean resultOk;
            int status = this.pluginResult.getStatus();
            if (status == Status.NO_RESULT.ordinal()) {
                noResult = true;
            } else {
                noResult = NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
            }
            if (status == Status.OK.ordinal()) {
                resultOk = true;
            } else {
                resultOk = NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
            }
            boolean keepCallback = this.pluginResult.getKeepCallback();
            char c = (noResult || resultOk) ? 'S' : 'F';
            sb.append(c).append(keepCallback ? '1' : '0').append(status).append(' ').append(this.jsPayloadOrCallbackId).append(' ');
            encodeAsMessageHelper(sb, this.pluginResult);
        }

        void buildJsMessage(StringBuilder sb) {
            switch (this.pluginResult.getMessageType()) {
                case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                    sb.append("cordova.require('cordova/base64').toArrayBuffer('").append(this.pluginResult.getMessage()).append("')");
                case PluginResult.MESSAGE_TYPE_BINARYSTRING /*7*/:
                    sb.append("atob('").append(this.pluginResult.getMessage()).append("')");
                case TransportMediator.FLAG_KEY_MEDIA_PLAY_PAUSE /*8*/:
                    int size = this.pluginResult.getMultipartMessagesSize();
                    for (int i = 0; i < size; i++) {
                        new JsMessage(this.pluginResult.getMultipartMessage(i), this.jsPayloadOrCallbackId).buildJsMessage(sb);
                        if (i < size - 1) {
                            sb.append(",");
                        }
                    }
                default:
                    sb.append(this.pluginResult.getMessage());
            }
        }

        void encodeAsJsMessage(StringBuilder sb) {
            if (this.pluginResult == null) {
                sb.append(this.jsPayloadOrCallbackId);
                return;
            }
            int status = this.pluginResult.getStatus();
            boolean success = (status == Status.OK.ordinal() || status == Status.NO_RESULT.ordinal()) ? true : NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
            sb.append("cordova.callbackFromNative('").append(this.jsPayloadOrCallbackId).append("',").append(success).append(",").append(status).append(",[");
            buildJsMessage(sb);
            sb.append("],").append(this.pluginResult.getKeepCallback()).append(");");
        }
    }

    public static class EvalBridgeMode extends BridgeMode {
        private final CordovaInterface cordova;
        private final CordovaWebViewEngine engine;

        /* renamed from: org.apache.cordova.NativeToJsMessageQueue.EvalBridgeMode.1 */
        class C02111 implements Runnable {
            final /* synthetic */ NativeToJsMessageQueue val$queue;

            C02111(NativeToJsMessageQueue nativeToJsMessageQueue) {
                this.val$queue = nativeToJsMessageQueue;
            }

            public void run() {
                String js = this.val$queue.popAndEncodeAsJs();
                if (js != null) {
                    EvalBridgeMode.this.engine.evaluateJavascript(js, null);
                }
            }
        }

        public EvalBridgeMode(CordovaWebViewEngine engine, CordovaInterface cordova) {
            this.engine = engine;
            this.cordova = cordova;
        }

        public void onNativeToJsMessageAvailable(NativeToJsMessageQueue queue) {
            this.cordova.getActivity().runOnUiThread(new C02111(queue));
        }
    }

    public static class LoadUrlBridgeMode extends BridgeMode {
        private final CordovaInterface cordova;
        private final CordovaWebViewEngine engine;

        /* renamed from: org.apache.cordova.NativeToJsMessageQueue.LoadUrlBridgeMode.1 */
        class C02121 implements Runnable {
            final /* synthetic */ NativeToJsMessageQueue val$queue;

            C02121(NativeToJsMessageQueue nativeToJsMessageQueue) {
                this.val$queue = nativeToJsMessageQueue;
            }

            public void run() {
                String js = this.val$queue.popAndEncodeAsJs();
                if (js != null) {
                    LoadUrlBridgeMode.this.engine.loadUrl("javascript:" + js, NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL);
                }
            }
        }

        public LoadUrlBridgeMode(CordovaWebViewEngine engine, CordovaInterface cordova) {
            this.engine = engine;
            this.cordova = cordova;
        }

        public void onNativeToJsMessageAvailable(NativeToJsMessageQueue queue) {
            this.cordova.getActivity().runOnUiThread(new C02121(queue));
        }
    }

    public static class NoOpBridgeMode extends BridgeMode {
        public void onNativeToJsMessageAvailable(NativeToJsMessageQueue queue) {
        }
    }

    public static class OnlineEventsBridgeMode extends BridgeMode {
        private final OnlineEventsBridgeModeDelegate delegate;
        private boolean ignoreNextFlush;
        private boolean online;

        /* renamed from: org.apache.cordova.NativeToJsMessageQueue.OnlineEventsBridgeMode.1 */
        class C02131 implements Runnable {
            C02131() {
            }

            public void run() {
                OnlineEventsBridgeMode.this.online = NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
                OnlineEventsBridgeMode.this.ignoreNextFlush = true;
                OnlineEventsBridgeMode.this.delegate.setNetworkAvailable(true);
            }
        }

        /* renamed from: org.apache.cordova.NativeToJsMessageQueue.OnlineEventsBridgeMode.2 */
        class C02142 implements Runnable {
            final /* synthetic */ NativeToJsMessageQueue val$queue;

            C02142(NativeToJsMessageQueue nativeToJsMessageQueue) {
                this.val$queue = nativeToJsMessageQueue;
            }

            public void run() {
                if (!this.val$queue.isEmpty()) {
                    OnlineEventsBridgeMode.this.ignoreNextFlush = NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
                    OnlineEventsBridgeMode.this.delegate.setNetworkAvailable(OnlineEventsBridgeMode.this.online);
                }
            }
        }

        public interface OnlineEventsBridgeModeDelegate {
            void runOnUiThread(Runnable runnable);

            void setNetworkAvailable(boolean z);
        }

        public OnlineEventsBridgeMode(OnlineEventsBridgeModeDelegate delegate) {
            this.delegate = delegate;
        }

        public void reset() {
            this.delegate.runOnUiThread(new C02131());
        }

        public void onNativeToJsMessageAvailable(NativeToJsMessageQueue queue) {
            this.delegate.runOnUiThread(new C02142(queue));
        }

        public void notifyOfFlush(NativeToJsMessageQueue queue, boolean fromOnlineEvent) {
            if (fromOnlineEvent && !this.ignoreNextFlush) {
                this.online = !this.online ? true : NativeToJsMessageQueue.FORCE_ENCODE_USING_EVAL;
            }
        }
    }

    public NativeToJsMessageQueue() {
        this.queue = new LinkedList();
        this.bridgeModes = new ArrayList();
    }

    static {
        MAX_PAYLOAD_SIZE = 524288000;
    }

    public void addBridgeMode(BridgeMode bridgeMode) {
        this.bridgeModes.add(bridgeMode);
    }

    public boolean isBridgeEnabled() {
        return this.activeBridgeMode != null ? true : FORCE_ENCODE_USING_EVAL;
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public void setBridgeMode(int value) {
        if (value < -1 || value >= this.bridgeModes.size()) {
            LOG.m4d(LOG_TAG, "Invalid NativeToJsBridgeMode: " + value);
            return;
        }
        BridgeMode newMode = value < 0 ? null : (BridgeMode) this.bridgeModes.get(value);
        if (newMode != this.activeBridgeMode) {
            LOG.m4d(LOG_TAG, "Set native->JS mode to " + (newMode == null ? "null" : newMode.getClass().getSimpleName()));
            synchronized (this) {
                this.activeBridgeMode = newMode;
                if (newMode != null) {
                    newMode.reset();
                    if (!(this.paused || this.queue.isEmpty())) {
                        newMode.onNativeToJsMessageAvailable(this);
                    }
                }
            }
        }
    }

    public void reset() {
        synchronized (this) {
            this.queue.clear();
            setBridgeMode(-1);
        }
    }

    private int calculatePackedMessageLength(JsMessage message) {
        int messageLen = message.calculateEncodedLength();
        return (String.valueOf(messageLen).length() + messageLen) + 1;
    }

    private void packMessage(JsMessage message, StringBuilder sb) {
        sb.append(message.calculateEncodedLength()).append(' ');
        message.encodeAsMessage(sb);
    }

    public String popAndEncode(boolean fromOnlineEvent) {
        String str = null;
        synchronized (this) {
            if (this.activeBridgeMode == null) {
            } else {
                this.activeBridgeMode.notifyOfFlush(this, fromOnlineEvent);
                if (this.queue.isEmpty()) {
                } else {
                    int totalPayloadLen = 0;
                    int numMessagesToSend = 0;
                    Iterator it = this.queue.iterator();
                    while (it.hasNext()) {
                        int messageSize = calculatePackedMessageLength((JsMessage) it.next());
                        if (numMessagesToSend > 0 && totalPayloadLen + messageSize > MAX_PAYLOAD_SIZE && MAX_PAYLOAD_SIZE > 0) {
                            break;
                        }
                        totalPayloadLen += messageSize;
                        numMessagesToSend++;
                    }
                    StringBuilder sb = new StringBuilder(totalPayloadLen);
                    for (int i = 0; i < numMessagesToSend; i++) {
                        packMessage((JsMessage) this.queue.removeFirst(), sb);
                    }
                    if (!this.queue.isEmpty()) {
                        sb.append('*');
                    }
                    str = sb.toString();
                }
            }
        }
        return str;
    }

    public String popAndEncodeAsJs() {
        String str;
        synchronized (this) {
            if (this.queue.size() == 0) {
                str = null;
            } else {
                boolean willSendAllMessages;
                int totalPayloadLen = 0;
                int numMessagesToSend = 0;
                Iterator it = this.queue.iterator();
                while (it.hasNext()) {
                    int messageSize = ((JsMessage) it.next()).calculateEncodedLength() + 50;
                    if (numMessagesToSend > 0 && totalPayloadLen + messageSize > MAX_PAYLOAD_SIZE && MAX_PAYLOAD_SIZE > 0) {
                        break;
                    }
                    totalPayloadLen += messageSize;
                    numMessagesToSend++;
                }
                if (numMessagesToSend == this.queue.size()) {
                    willSendAllMessages = true;
                } else {
                    willSendAllMessages = FORCE_ENCODE_USING_EVAL;
                }
                StringBuilder sb = new StringBuilder((willSendAllMessages ? 0 : 100) + totalPayloadLen);
                int i = 0;
                while (i < numMessagesToSend) {
                    JsMessage message = (JsMessage) this.queue.removeFirst();
                    if (willSendAllMessages && i + 1 == numMessagesToSend) {
                        message.encodeAsJsMessage(sb);
                    } else {
                        sb.append("try{");
                        message.encodeAsJsMessage(sb);
                        sb.append("}finally{");
                    }
                    i++;
                }
                if (!willSendAllMessages) {
                    sb.append("window.setTimeout(function(){cordova.require('cordova/plugin/android/polling').pollOnce();},0);");
                }
                i = willSendAllMessages ? 1 : 0;
                while (i < numMessagesToSend) {
                    sb.append('}');
                    i++;
                }
                str = sb.toString();
            }
        }
        return str;
    }

    public void addJavaScript(String statement) {
        enqueueMessage(new JsMessage(statement));
    }

    public void addPluginResult(PluginResult result, String callbackId) {
        if (callbackId == null) {
            LOG.m8e(LOG_TAG, "Got plugin result with no callbackId", new Throwable());
            return;
        }
        boolean noResult = result.getStatus() == Status.NO_RESULT.ordinal() ? true : FORCE_ENCODE_USING_EVAL;
        boolean keepCallback = result.getKeepCallback();
        if (!noResult || !keepCallback) {
            enqueueMessage(new JsMessage(result, callbackId));
        }
    }

    private void enqueueMessage(JsMessage message) {
        synchronized (this) {
            if (this.activeBridgeMode == null) {
                LOG.m4d(LOG_TAG, "Dropping Native->JS message due to disabled bridge");
                return;
            }
            this.queue.add(message);
            if (!this.paused) {
                this.activeBridgeMode.onNativeToJsMessageAvailable(this);
            }
        }
    }

    public void setPaused(boolean value) {
        if (this.paused && value) {
            LOG.m8e(LOG_TAG, "nested call to setPaused detected.", new Throwable());
        }
        this.paused = value;
        if (!value) {
            synchronized (this) {
                if (!(this.queue.isEmpty() || this.activeBridgeMode == null)) {
                    this.activeBridgeMode.onNativeToJsMessageAvailable(this);
                }
            }
        }
    }
}
