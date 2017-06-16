package com.homerours.musiccontrols;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build.VERSION;
import android.os.IBinder;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class MusicControls extends CordovaPlugin {
    private AudioManager mAudioManager;
    private MusicControlsBroadcastReceiver mMessageReceiver;
    private boolean mediaButtonAccess;
    private PendingIntent mediaButtonPendingIntent;
    private MusicControlsNotification notification;
    private final int notificationID;

    /* renamed from: com.homerours.musiccontrols.MusicControls.1 */
    class C01741 implements ServiceConnection {
        final /* synthetic */ Activity val$activity;

        C01741(Activity activity) {
            this.val$activity = activity;
        }

        public void onServiceConnected(ComponentName className, IBinder binder) {
            ((KillBinder) binder).service.startService(new Intent(this.val$activity, MusicControlsNotificationKiller.class));
        }

        public void onServiceDisconnected(ComponentName className) {
        }
    }

    /* renamed from: com.homerours.musiccontrols.MusicControls.2 */
    class C01752 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ MusicControlsInfos val$infos;

        C01752(MusicControlsInfos musicControlsInfos, CallbackContext callbackContext) {
            this.val$infos = musicControlsInfos;
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            MusicControls.this.notification.updateNotification(this.val$infos);
            this.val$callbackContext.success("success");
        }
    }

    /* renamed from: com.homerours.musiccontrols.MusicControls.3 */
    class C01763 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        C01763(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            MusicControls.this.mMessageReceiver.setCallback(this.val$callbackContext);
        }
    }

    public MusicControls() {
        this.notificationID = 7824;
        this.mediaButtonAccess = true;
    }

    private void registerBroadcaster(MusicControlsBroadcastReceiver mMessageReceiver) {
        Context context = this.cordova.getActivity().getApplicationContext();
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-previous"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-pause"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-play"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-next"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-media-button"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("music-controls-destroy"));
        context.registerReceiver(mMessageReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
    }

    public void registerMediaButtonEvent() {
        if (this.mediaButtonAccess && VERSION.SDK_INT >= 18) {
            this.mAudioManager.registerMediaButtonEventReceiver(this.mediaButtonPendingIntent);
        }
    }

    public void unregisterMediaButtonEvent() {
        if (this.mediaButtonAccess && VERSION.SDK_INT >= 18) {
            this.mAudioManager.unregisterMediaButtonEventReceiver(this.mediaButtonPendingIntent);
        }
    }

    public void destroyPlayerNotification() {
        this.notification.destroy();
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Activity activity = this.cordova.getActivity();
        Context context = activity.getApplicationContext();
        getClass();
        this.notification = new MusicControlsNotification(activity, 7824);
        this.mMessageReceiver = new MusicControlsBroadcastReceiver(this);
        registerBroadcaster(this.mMessageReceiver);
        try {
            this.mAudioManager = (AudioManager) context.getSystemService("audio");
            this.mediaButtonPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("music-controls-media-button"), 134217728);
            registerMediaButtonEvent();
        } catch (Exception e) {
            this.mediaButtonAccess = false;
            e.printStackTrace();
        }
        ServiceConnection mConnection = new C01741(activity);
        Intent startServiceIntent = new Intent(activity, MusicControlsNotificationKiller.class);
        getClass();
        startServiceIntent.putExtra("notificationID", 7824);
        activity.bindService(startServiceIntent, mConnection, 1);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = this.cordova.getActivity().getApplicationContext();
        Activity activity = this.cordova.getActivity();
        if (action.equals("create")) {
            this.cordova.getThreadPool().execute(new C01752(new MusicControlsInfos(args), callbackContext));
        } else if (action.equals("updateIsPlaying")) {
            this.notification.updateIsPlaying(args.getJSONObject(0).getBoolean("isPlaying"));
            callbackContext.success("success");
        } else if (action.equals("destroy")) {
            this.notification.destroy();
            this.mMessageReceiver.stopListening();
            callbackContext.success("success");
        } else if (action.equals("watch")) {
            registerMediaButtonEvent();
            this.cordova.getThreadPool().execute(new C01763(callbackContext));
        }
        return true;
    }

    public void onDestroy() {
        this.notification.destroy();
        this.mMessageReceiver.stopListening();
        unregisterMediaButtonEvent();
        super.onDestroy();
    }

    public void onReset() {
        onDestroy();
        super.onReset();
    }
}
