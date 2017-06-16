package com.homerours.musiccontrols;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicControlsNotificationKiller extends Service {
    private static int NOTIFICATION_ID;
    private final IBinder mBinder;
    private NotificationManager mNM;

    public MusicControlsNotificationKiller() {
        this.mBinder = new KillBinder(this);
    }

    public IBinder onBind(Intent intent) {
        NOTIFICATION_ID = intent.getIntExtra("notificationID", 1);
        return this.mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public void onCreate() {
        this.mNM = (NotificationManager) getSystemService("notification");
        this.mNM.cancel(NOTIFICATION_ID);
    }
}
