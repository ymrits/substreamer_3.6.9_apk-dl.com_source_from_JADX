package com.homerours.musiccontrols;

import android.app.Activity;
import android.app.Notification.Builder;
import android.app.Notification.MediaStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.cordova.BuildConfig;

public class MusicControlsNotification {
    private Bitmap bitmapCover;
    private Activity cordovaActivity;
    private MusicControlsInfos infos;
    private Builder notificationBuilder;
    private int notificationID;
    private NotificationManager notificationManager;

    public MusicControlsNotification(Activity cordovaActivity, int id) {
        this.notificationID = id;
        this.cordovaActivity = cordovaActivity;
        this.notificationManager = (NotificationManager) cordovaActivity.getSystemService("notification");
    }

    public void updateNotification(MusicControlsInfos newInfos) {
        if (!newInfos.cover.isEmpty() && (this.infos == null || !newInfos.cover.equals(this.infos.cover))) {
            getBitmapCover(newInfos.cover);
        }
        this.infos = newInfos;
        createBuilder();
        this.notificationManager.notify(this.notificationID, this.notificationBuilder.build());
    }

    public void updateIsPlaying(boolean isPlaying) {
        this.infos.isPlaying = isPlaying;
        createBuilder();
        this.notificationManager.notify(this.notificationID, this.notificationBuilder.build());
    }

    private void getBitmapCover(String coverURL) {
        try {
            if (coverURL.matches("^(https?|ftp)://.*$")) {
                this.bitmapCover = getBitmapFromURL(coverURL);
            } else {
                this.bitmapCover = getBitmapFromLocal(coverURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Bitmap getBitmapFromLocal(String localURL) {
        BufferedInputStream buf;
        Bitmap myBitmap;
        try {
            buf = new BufferedInputStream(new FileInputStream(new File(Uri.parse(localURL).getPath())));
            myBitmap = BitmapFactory.decodeStream(buf);
            buf.close();
            return myBitmap;
        } catch (Exception ex) {
            try {
                buf = new BufferedInputStream(this.cordovaActivity.getAssets().open("www/" + localURL));
                myBitmap = BitmapFactory.decodeStream(buf);
                buf.close();
                return myBitmap;
            } catch (Exception ex2) {
                ex.printStackTrace();
                ex2.printStackTrace();
                return null;
            }
        }
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(strURL).openConnection();
            connection.setDoInput(true);
            connection.connect();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void createBuilder() {
        Context context = this.cordovaActivity;
        Builder builder = new Builder(context);
        builder.setContentTitle(this.infos.track);
        if (!this.infos.artist.isEmpty()) {
            builder.setContentText(this.infos.artist);
        }
        builder.setWhen(0);
        if (this.infos.dismissable) {
            builder.setOngoing(false);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 1, new Intent("music-controls-destroy"), 0));
        } else {
            builder.setOngoing(true);
        }
        if (!this.infos.ticker.isEmpty()) {
            builder.setTicker(this.infos.ticker);
        }
        builder.setPriority(2);
        if (VERSION.SDK_INT >= 21) {
            builder.setVisibility(1);
        }
        if (this.infos.isPlaying) {
            builder.setSmallIcon(17301540);
        } else {
            builder.setSmallIcon(17301539);
        }
        if (!(this.infos.cover.isEmpty() || this.bitmapCover == null)) {
            builder.setLargeIcon(this.bitmapCover);
        }
        Intent intent = new Intent(context, this.cordovaActivity.getClass());
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
        int nbControls = 0;
        if (this.infos.hasPrev) {
            nbControls = 0 + 1;
            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("music-controls-previous"), 0);
            builder.addAction(17301542, BuildConfig.FLAVOR, previousPendingIntent);
        }
        if (this.infos.isPlaying) {
            nbControls++;
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("music-controls-pause"), 0);
            builder.addAction(17301539, BuildConfig.FLAVOR, pausePendingIntent);
        } else {
            nbControls++;
            PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("music-controls-play"), 0);
            builder.addAction(17301540, BuildConfig.FLAVOR, playPendingIntent);
        }
        if (this.infos.hasNext) {
            nbControls++;
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("music-controls-next"), 0);
            builder.addAction(17301537, BuildConfig.FLAVOR, nextPendingIntent);
        }
        if (this.infos.hasClose) {
            nbControls++;
            PendingIntent destroyPendingIntent = PendingIntent.getBroadcast(context, 1, new Intent("music-controls-destroy"), 0);
            builder.addAction(17301560, BuildConfig.FLAVOR, destroyPendingIntent);
        }
        if (VERSION.SDK_INT >= 21) {
            int[] args = new int[nbControls];
            for (int i = 0; i < nbControls; i++) {
                args[i] = i;
            }
            builder.setStyle(new MediaStyle().setShowActionsInCompactView(args));
        }
        this.notificationBuilder = builder;
    }

    public void destroy() {
        this.notificationManager.cancel(this.notificationID);
    }
}
