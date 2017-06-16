package com.homerours.musiccontrols;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.file.FileUtils;

public class MusicControlsBroadcastReceiver extends BroadcastReceiver {
    private CallbackContext cb;
    private MusicControls musicControls;

    public MusicControlsBroadcastReceiver(MusicControls musicControls) {
        this.musicControls = musicControls;
    }

    public void setCallback(CallbackContext cb) {
        this.cb = cb;
    }

    public void stopListening() {
        if (this.cb != null) {
            this.cb.success("music-controls-stop-listening");
            this.cb = null;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (this.cb != null) {
            String message = intent.getAction();
            if (message.equals("android.intent.action.HEADSET_PLUG")) {
                switch (intent.getIntExtra("state", -1)) {
                    case FileUtils.ACTION_GET_FILE /*0*/:
                        this.cb.success("music-controls-headset-unplugged");
                        this.cb = null;
                        this.musicControls.unregisterMediaButtonEvent();
                    case FileUtils.ACTION_WRITE /*1*/:
                        this.cb.success("music-controls-headset-plugged");
                        this.cb = null;
                        this.musicControls.registerMediaButtonEvent();
                    default:
                }
            } else if (message.equals("music-controls-media-button")) {
                if (((KeyEvent) intent.getParcelableExtra("android.intent.extra.KEY_EVENT")).getAction() == 0) {
                    this.cb.success(message);
                    this.cb = null;
                }
            } else if (message.equals("music-controls-destroy")) {
                this.cb.success("music-controls-destroy");
                this.cb = null;
                this.musicControls.destroyPlayerNotification();
            } else {
                this.cb.success(message);
                this.cb = null;
            }
        }
    }
}
