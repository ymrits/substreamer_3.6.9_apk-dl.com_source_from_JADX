package org.apache.cordova.media;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.support.v7.widget.ListPopupWindow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.media.AudioPlayer.STATE;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AudioHandler extends CordovaPlugin {
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static int RECORD_AUDIO;
    public static String TAG;
    public static int WRITE_EXTERNAL_STORAGE;
    public static String[] permissions;
    private String fileUriStr;
    private OnAudioFocusChangeListener focusChangeListener;
    private CallbackContext messageChannel;
    private int origVolumeStream;
    ArrayList<AudioPlayer> pausedForFocus;
    ArrayList<AudioPlayer> pausedForPhone;
    HashMap<String, AudioPlayer> players;
    private String recordId;

    /* renamed from: org.apache.cordova.media.AudioHandler.1 */
    class C02521 implements OnAudioFocusChangeListener {
        C02521() {
        }

        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case -3:
                case ListPopupWindow.WRAP_CONTENT /*-2*/:
                case ListPopupWindow.MATCH_PARENT /*-1*/:
                    AudioHandler.this.pauseAllLostFocus();
                case FileUtils.ACTION_WRITE /*1*/:
                    AudioHandler.this.resumeAllGainedFocus();
                default:
            }
        }
    }

    static {
        TAG = "AudioHandler";
        permissions = new String[]{"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
        RECORD_AUDIO = 0;
        WRITE_EXTERNAL_STORAGE = 1;
    }

    public AudioHandler() {
        this.origVolumeStream = -1;
        this.focusChangeListener = new C02521();
        this.players = new HashMap();
        this.pausedForPhone = new ArrayList();
        this.pausedForFocus = new ArrayList();
    }

    protected void getWritePermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, permissions[WRITE_EXTERNAL_STORAGE]);
    }

    protected void getMicPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, permissions[RECORD_AUDIO]);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        CordovaResourceApi resourceApi = this.webView.getResourceApi();
        Status status = Status.OK;
        String result = BuildConfig.FLAVOR;
        String target;
        if (action.equals("startRecordingAudio")) {
            this.recordId = args.getString(0);
            target = args.getString(1);
            try {
                this.fileUriStr = resourceApi.remapUri(Uri.parse(target)).toString();
            } catch (IllegalArgumentException e) {
                this.fileUriStr = target;
            }
            promptForRecord();
        } else if (action.equals("stopRecordingAudio")) {
            stopRecordingAudio(args.getString(0), true);
        } else if (action.equals("pauseRecordingAudio")) {
            stopRecordingAudio(args.getString(0), false);
        } else if (action.equals("resumeRecordingAudio")) {
            resumeRecordingAudio(args.getString(0));
        } else if (action.equals("startPlayingAudio")) {
            String fileUriStr;
            target = args.getString(1);
            try {
                fileUriStr = resourceApi.remapUri(Uri.parse(target)).toString();
            } catch (IllegalArgumentException e2) {
                fileUriStr = target;
            }
            startPlayingAudio(args.getString(0), FileHelper.stripFileProtocol(fileUriStr));
        } else if (action.equals("seekToAudio")) {
            seekToAudio(args.getString(0), args.getInt(1));
        } else if (action.equals("pausePlayingAudio")) {
            pausePlayingAudio(args.getString(0));
        } else if (action.equals("stopPlayingAudio")) {
            stopPlayingAudio(args.getString(0));
        } else if (action.equals("setVolume")) {
            try {
                setVolume(args.getString(0), Float.parseFloat(args.getString(1)));
            } catch (NumberFormatException e3) {
            }
        } else if (action.equals("setRate")) {
            try {
                setRate(args.getString(0), Float.parseFloat(args.getString(1)));
            } catch (NumberFormatException e4) {
            }
        } else if (action.equals("getCurrentPositionAudio")) {
            callbackContext.sendPluginResult(new PluginResult(status, getCurrentPositionAudio(args.getString(0))));
            return true;
        } else if (action.equals("getDurationAudio")) {
            callbackContext.sendPluginResult(new PluginResult(status, getDurationAudio(args.getString(0), args.getString(1))));
            return true;
        } else if (action.equals("create")) {
            getOrCreatePlayer(args.getString(0), FileHelper.stripFileProtocol(args.getString(1)));
        } else if (action.equals(BuildConfig.BUILD_TYPE)) {
            callbackContext.sendPluginResult(new PluginResult(status, release(args.getString(0))));
            return true;
        } else if (action.equals("messageChannel")) {
            this.messageChannel = callbackContext;
            return true;
        } else if (!action.equals("getCurrentAmplitudeAudio")) {
            return false;
        } else {
            callbackContext.sendPluginResult(new PluginResult(status, getCurrentAmplitudeAudio(args.getString(0))));
            return true;
        }
        callbackContext.sendPluginResult(new PluginResult(status, result));
        return true;
    }

    public void onDestroy() {
        if (!this.players.isEmpty()) {
            onLastPlayerReleased();
        }
        for (AudioPlayer audio : this.players.values()) {
            audio.destroy();
        }
        this.players.clear();
    }

    public void onReset() {
        onDestroy();
    }

    public Object onMessage(String id, Object data) {
        if (id.equals("telephone")) {
            Iterator it;
            if ("ringing".equals(data) || "offhook".equals(data)) {
                for (AudioPlayer audio : this.players.values()) {
                    if (audio.getState() == STATE.MEDIA_RUNNING.ordinal()) {
                        this.pausedForPhone.add(audio);
                        audio.pausePlaying();
                    }
                }
            } else if ("idle".equals(data)) {
                it = this.pausedForPhone.iterator();
                while (it.hasNext()) {
                    ((AudioPlayer) it.next()).startPlaying(null);
                }
                this.pausedForPhone.clear();
            }
        }
        return null;
    }

    private AudioPlayer getOrCreatePlayer(String id, String file) {
        AudioPlayer ret = (AudioPlayer) this.players.get(id);
        if (ret != null) {
            return ret;
        }
        if (this.players.isEmpty()) {
            onFirstPlayerCreated();
        }
        ret = new AudioPlayer(this, id, file);
        this.players.put(id, ret);
        return ret;
    }

    private boolean release(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.remove(id);
        if (audio == null) {
            return false;
        }
        if (this.players.isEmpty()) {
            onLastPlayerReleased();
        }
        audio.destroy();
        return true;
    }

    public void startRecordingAudio(String id, String file) {
        getOrCreatePlayer(id, file).startRecording(file);
    }

    public void stopRecordingAudio(String id, boolean stop) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.stopRecording(stop);
        }
    }

    public void resumeRecordingAudio(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.resumeRecording();
        }
    }

    public void startPlayingAudio(String id, String file) {
        getOrCreatePlayer(id, file).startPlaying(file);
        getAudioFocus();
    }

    public void seekToAudio(String id, int milliseconds) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.seekToPlaying(milliseconds);
        }
    }

    public void pausePlayingAudio(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.pausePlaying();
        }
    }

    public void stopPlayingAudio(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.stopPlaying();
        }
    }

    public float getCurrentPositionAudio(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            return ((float) audio.getCurrentPosition()) / 1000.0f;
        }
        return -1.0f;
    }

    public float getDurationAudio(String id, String file) {
        return getOrCreatePlayer(id, file).getDuration(file);
    }

    public void setAudioOutputDevice(int output) {
        String TAG1 = "AudioHandler.setAudioOutputDevice(): Error : ";
        AudioManager audiMgr = (AudioManager) this.cordova.getActivity().getSystemService("audio");
        if (output == 2) {
            audiMgr.setRouting(0, 2, -1);
        } else if (output == 1) {
            audiMgr.setRouting(0, 1, -1);
        } else {
            LOG.m7e(TAG1, " Unknown output device");
        }
    }

    public void pauseAllLostFocus() {
        for (AudioPlayer audio : this.players.values()) {
            if (audio.getState() == STATE.MEDIA_RUNNING.ordinal()) {
                this.pausedForFocus.add(audio);
                audio.pausePlaying();
            }
        }
    }

    public void resumeAllGainedFocus() {
        Iterator it = this.pausedForFocus.iterator();
        while (it.hasNext()) {
            ((AudioPlayer) it.next()).resumePlaying();
        }
        this.pausedForFocus.clear();
    }

    public void getAudioFocus() {
        String TAG2 = "AudioHandler.getAudioFocus(): Error : ";
        int result = ((AudioManager) this.cordova.getActivity().getSystemService("audio")).requestAudioFocus(this.focusChangeListener, 3, 1);
        if (result != 1) {
            LOG.m7e(TAG2, result + " instead of " + 1);
        }
    }

    public int getAudioOutputDevice() {
        AudioManager audiMgr = (AudioManager) this.cordova.getActivity().getSystemService("audio");
        if (audiMgr.getRouting(0) == 1) {
            return 1;
        }
        if (audiMgr.getRouting(0) == 2) {
            return 2;
        }
        return -1;
    }

    public void setVolume(String id, float volume) {
        String TAG3 = "AudioHandler.setVolume(): Error : ";
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.setVolume(volume);
        } else {
            LOG.m7e(TAG3, "Unknown Audio Player " + id);
        }
    }

    public void setRate(String id, float volume) {
        String TAG3 = "AudioHandler.setVolume(): Error : ";
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            audio.setRate(volume);
        } else {
            LOG.m7e(TAG3, "Unknown Audio Player " + id);
        }
    }

    private void onFirstPlayerCreated() {
        this.origVolumeStream = this.cordova.getActivity().getVolumeControlStream();
        this.cordova.getActivity().setVolumeControlStream(3);
    }

    private void onLastPlayerReleased() {
        if (this.origVolumeStream != -1) {
            this.cordova.getActivity().setVolumeControlStream(this.origVolumeStream);
            this.origVolumeStream = -1;
        }
    }

    void sendEventMessage(String action, JSONObject actionData) {
        JSONObject message = new JSONObject();
        try {
            message.put("action", action);
            if (actionData != null) {
                message.put(action, actionData);
            }
        } catch (Throwable e) {
            LOG.m8e(TAG, "Failed to create event message", e);
        }
        PluginResult pluginResult = new PluginResult(Status.OK, message);
        pluginResult.setKeepCallback(true);
        if (this.messageChannel != null) {
            this.messageChannel.sendPluginResult(pluginResult);
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == -1) {
                this.messageChannel.sendPluginResult(new PluginResult(Status.ERROR, (int) PERMISSION_DENIED_ERROR));
                return;
            }
        }
        promptForRecord();
    }

    private void promptForRecord() {
        if (PermissionHelper.hasPermission(this, permissions[WRITE_EXTERNAL_STORAGE]) && PermissionHelper.hasPermission(this, permissions[RECORD_AUDIO])) {
            startRecordingAudio(this.recordId, FileHelper.stripFileProtocol(this.fileUriStr));
        } else if (PermissionHelper.hasPermission(this, permissions[RECORD_AUDIO])) {
            getWritePermission(WRITE_EXTERNAL_STORAGE);
        } else {
            getMicPermission(RECORD_AUDIO);
        }
    }

    public float getCurrentAmplitudeAudio(String id) {
        AudioPlayer audio = (AudioPlayer) this.players.get(id);
        if (audio != null) {
            return audio.getCurrentAmplitude();
        }
        return 0.0f;
    }
}
