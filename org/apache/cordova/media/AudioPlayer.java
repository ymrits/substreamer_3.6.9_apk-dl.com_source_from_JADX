package org.apache.cordova.media;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.os.Build.VERSION;
import android.os.Environment;
import android.support.v4.app.NotificationCompat.WearableExtender;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import org.apache.cordova.LOG;
import org.apache.cordova.file.FileUtils;
import org.json.JSONObject;

public class AudioPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener {
    private static final String LOG_TAG = "AudioPlayer";
    private static int MEDIA_DURATION;
    private static int MEDIA_ERROR;
    private static int MEDIA_ERR_ABORTED;
    private static int MEDIA_ERR_NONE_ACTIVE;
    private static int MEDIA_POSITION;
    private static int MEDIA_STATE;
    private String audioFile;
    private float duration;
    private AudioHandler handler;
    private String id;
    private MODE mode;
    private MediaPlayer player;
    private boolean prepareOnly;
    private MediaRecorder recorder;
    private int seekOnPrepared;
    private STATE state;
    private String tempFile;
    private LinkedList<String> tempFiles;

    /* renamed from: org.apache.cordova.media.AudioPlayer.1 */
    static /* synthetic */ class C02531 {
        static final /* synthetic */ int[] $SwitchMap$org$apache$cordova$media$AudioPlayer$MODE;
        static final /* synthetic */ int[] $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE;

        static {
            $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE = new int[STATE.values().length];
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_LOADING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_STARTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_RUNNING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_PAUSED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[STATE.MEDIA_STOPPED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            $SwitchMap$org$apache$cordova$media$AudioPlayer$MODE = new int[MODE.values().length];
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$MODE[MODE.PLAY.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$MODE[MODE.NONE.ordinal()] = 2;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$org$apache$cordova$media$AudioPlayer$MODE[MODE.RECORD.ordinal()] = 3;
            } catch (NoSuchFieldError e9) {
            }
        }
    }

    public enum MODE {
        NONE,
        PLAY,
        RECORD
    }

    public enum STATE {
        MEDIA_NONE,
        MEDIA_STARTING,
        MEDIA_RUNNING,
        MEDIA_PAUSED,
        MEDIA_STOPPED,
        MEDIA_LOADING
    }

    static {
        MEDIA_STATE = 1;
        MEDIA_DURATION = 2;
        MEDIA_POSITION = 3;
        MEDIA_ERROR = 9;
        MEDIA_ERR_NONE_ACTIVE = 0;
        MEDIA_ERR_ABORTED = 1;
    }

    public AudioPlayer(AudioHandler handler, String id, String file) {
        this.mode = MODE.NONE;
        this.state = STATE.MEDIA_NONE;
        this.audioFile = null;
        this.duration = -1.0f;
        this.recorder = null;
        this.tempFiles = null;
        this.tempFile = null;
        this.player = null;
        this.prepareOnly = true;
        this.seekOnPrepared = 0;
        this.handler = handler;
        this.id = id;
        this.audioFile = file;
        this.tempFiles = new LinkedList();
    }

    private String generateTempFile() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmprecording-" + System.currentTimeMillis() + ".3gp";
        }
        return "/data/data/" + this.handler.cordova.getActivity().getPackageName() + "/cache/tmprecording-" + System.currentTimeMillis() + ".3gp";
    }

    public void destroy() {
        if (this.player != null) {
            if (this.state == STATE.MEDIA_RUNNING || this.state == STATE.MEDIA_PAUSED) {
                this.player.stop();
                setState(STATE.MEDIA_STOPPED);
            }
            this.player.release();
            this.player = null;
        }
        if (this.recorder != null) {
            stopRecording(true);
            this.recorder.release();
            this.recorder = null;
        }
    }

    public void startRecording(String file) {
        switch (C02531.$SwitchMap$org$apache$cordova$media$AudioPlayer$MODE[this.mode.ordinal()]) {
            case FileUtils.ACTION_WRITE /*1*/:
                LOG.m4d(LOG_TAG, "AudioPlayer Error: Can't record in play mode.");
                sendErrorStatus(MEDIA_ERR_ABORTED);
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                this.audioFile = file;
                this.recorder = new MediaRecorder();
                this.recorder.setAudioSource(1);
                this.recorder.setOutputFormat(6);
                this.recorder.setAudioEncoder(3);
                this.tempFile = generateTempFile();
                this.recorder.setOutputFile(this.tempFile);
                try {
                    this.recorder.prepare();
                    this.recorder.start();
                    setState(STATE.MEDIA_RUNNING);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    sendErrorStatus(MEDIA_ERR_ABORTED);
                } catch (IOException e2) {
                    e2.printStackTrace();
                    sendErrorStatus(MEDIA_ERR_ABORTED);
                }
            case FileUtils.WRITE /*3*/:
                LOG.m4d(LOG_TAG, "AudioPlayer Error: Already recording.");
                sendErrorStatus(MEDIA_ERR_ABORTED);
            default:
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void moveFile(java.lang.String r15) {
        /*
        r14 = this;
        r11 = "/";
        r11 = r15.startsWith(r11);
        if (r11 != 0) goto L_0x0033;
    L_0x0008:
        r11 = android.os.Environment.getExternalStorageState();
        r12 = "mounted";
        r11 = r11.equals(r12);
        if (r11 == 0) goto L_0x00a3;
    L_0x0014:
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = android.os.Environment.getExternalStorageDirectory();
        r12 = r12.getAbsolutePath();
        r11 = r11.append(r12);
        r12 = java.io.File.separator;
        r11 = r11.append(r12);
        r11 = r11.append(r15);
        r15 = r11.toString();
    L_0x0033:
        r11 = r14.tempFiles;
        r10 = r11.size();
        r11 = "AudioPlayer";
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "size = ";
        r12 = r12.append(r13);
        r12 = r12.append(r10);
        r12 = r12.toString();
        org.apache.cordova.LOG.m4d(r11, r12);
        r11 = 1;
        if (r10 != r11) goto L_0x00ce;
    L_0x0054:
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "renaming ";
        r11 = r11.append(r12);
        r12 = r14.tempFile;
        r11 = r11.append(r12);
        r12 = " to ";
        r11 = r11.append(r12);
        r11 = r11.append(r15);
        r7 = r11.toString();
        r11 = "AudioPlayer";
        org.apache.cordova.LOG.m4d(r11, r7);
        r1 = new java.io.File;
        r11 = r14.tempFile;
        r1.<init>(r11);
        r11 = new java.io.File;
        r11.<init>(r15);
        r11 = r1.renameTo(r11);
        if (r11 != 0) goto L_0x00a2;
    L_0x008a:
        r11 = "AudioPlayer";
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "FAILED ";
        r12 = r12.append(r13);
        r12 = r12.append(r7);
        r12 = r12.toString();
        org.apache.cordova.LOG.m7e(r11, r12);
    L_0x00a2:
        return;
    L_0x00a3:
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "/data/data/";
        r11 = r11.append(r12);
        r12 = r14.handler;
        r12 = r12.cordova;
        r12 = r12.getActivity();
        r12 = r12.getPackageName();
        r11 = r11.append(r12);
        r12 = "/cache/";
        r11 = r11.append(r12);
        r11 = r11.append(r15);
        r15 = r11.toString();
        goto L_0x0033;
    L_0x00ce:
        r8 = 0;
        r9 = new java.io.FileOutputStream;	 Catch:{ Exception -> 0x018b }
        r11 = new java.io.File;	 Catch:{ Exception -> 0x018b }
        r11.<init>(r15);	 Catch:{ Exception -> 0x018b }
        r9.<init>(r11);	 Catch:{ Exception -> 0x018b }
        r5 = 0;
        r3 = 0;
        r2 = 0;
        r4 = r3;
        r6 = r5;
    L_0x00de:
        if (r2 >= r10) goto L_0x016b;
    L_0x00e0:
        r3 = new java.io.File;	 Catch:{ Exception -> 0x012a, all -> 0x0153 }
        r11 = r14.tempFiles;	 Catch:{ Exception -> 0x012a, all -> 0x0153 }
        r11 = r11.get(r2);	 Catch:{ Exception -> 0x012a, all -> 0x0153 }
        r11 = (java.lang.String) r11;	 Catch:{ Exception -> 0x012a, all -> 0x0153 }
        r3.<init>(r11);	 Catch:{ Exception -> 0x012a, all -> 0x0153 }
        r5 = new java.io.FileInputStream;	 Catch:{ Exception -> 0x0192, all -> 0x018d }
        r5.<init>(r3);	 Catch:{ Exception -> 0x0192, all -> 0x018d }
        if (r2 <= 0) goto L_0x0106;
    L_0x00f4:
        r11 = 1;
    L_0x00f5:
        copy(r5, r9, r11);	 Catch:{ Exception -> 0x0195 }
        if (r5 == 0) goto L_0x0101;
    L_0x00fa:
        r5.close();	 Catch:{ Exception -> 0x0108, all -> 0x014b }
        r3.delete();	 Catch:{ Exception -> 0x0108, all -> 0x014b }
        r3 = 0;
    L_0x0101:
        r2 = r2 + 1;
        r4 = r3;
        r6 = r5;
        goto L_0x00de;
    L_0x0106:
        r11 = 0;
        goto L_0x00f5;
    L_0x0108:
        r0 = move-exception;
        r11 = "AudioPlayer";
        r12 = r0.getLocalizedMessage();	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        org.apache.cordova.LOG.m8e(r11, r12, r0);	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        goto L_0x0101;
    L_0x0113:
        r0 = move-exception;
        r8 = r9;
    L_0x0115:
        r0.printStackTrace();	 Catch:{ all -> 0x0189 }
        if (r8 == 0) goto L_0x00a2;
    L_0x011a:
        r8.close();	 Catch:{ Exception -> 0x011e }
        goto L_0x00a2;
    L_0x011e:
        r0 = move-exception;
        r11 = "AudioPlayer";
        r12 = r0.getLocalizedMessage();
        org.apache.cordova.LOG.m8e(r11, r12, r0);
        goto L_0x00a2;
    L_0x012a:
        r0 = move-exception;
        r3 = r4;
        r5 = r6;
    L_0x012d:
        r11 = "AudioPlayer";
        r12 = r0.getLocalizedMessage();	 Catch:{ all -> 0x0190 }
        org.apache.cordova.LOG.m8e(r11, r12, r0);	 Catch:{ all -> 0x0190 }
        if (r5 == 0) goto L_0x0101;
    L_0x0138:
        r5.close();	 Catch:{ Exception -> 0x0140, all -> 0x014b }
        r3.delete();	 Catch:{ Exception -> 0x0140, all -> 0x014b }
        r3 = 0;
        goto L_0x0101;
    L_0x0140:
        r0 = move-exception;
        r11 = "AudioPlayer";
        r12 = r0.getLocalizedMessage();	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        org.apache.cordova.LOG.m8e(r11, r12, r0);	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        goto L_0x0101;
    L_0x014b:
        r11 = move-exception;
        r8 = r9;
    L_0x014d:
        if (r8 == 0) goto L_0x0152;
    L_0x014f:
        r8.close();	 Catch:{ Exception -> 0x017e }
    L_0x0152:
        throw r11;
    L_0x0153:
        r11 = move-exception;
        r3 = r4;
        r5 = r6;
    L_0x0156:
        if (r5 == 0) goto L_0x015f;
    L_0x0158:
        r5.close();	 Catch:{ Exception -> 0x0160, all -> 0x014b }
        r3.delete();	 Catch:{ Exception -> 0x0160, all -> 0x014b }
        r3 = 0;
    L_0x015f:
        throw r11;	 Catch:{ Exception -> 0x0113, all -> 0x014b }
    L_0x0160:
        r0 = move-exception;
        r12 = "AudioPlayer";
        r13 = r0.getLocalizedMessage();	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        org.apache.cordova.LOG.m8e(r12, r13, r0);	 Catch:{ Exception -> 0x0113, all -> 0x014b }
        goto L_0x015f;
    L_0x016b:
        if (r9 == 0) goto L_0x00a2;
    L_0x016d:
        r9.close();	 Catch:{ Exception -> 0x0172 }
        goto L_0x00a2;
    L_0x0172:
        r0 = move-exception;
        r11 = "AudioPlayer";
        r12 = r0.getLocalizedMessage();
        org.apache.cordova.LOG.m8e(r11, r12, r0);
        goto L_0x00a2;
    L_0x017e:
        r0 = move-exception;
        r12 = "AudioPlayer";
        r13 = r0.getLocalizedMessage();
        org.apache.cordova.LOG.m8e(r12, r13, r0);
        goto L_0x0152;
    L_0x0189:
        r11 = move-exception;
        goto L_0x014d;
    L_0x018b:
        r0 = move-exception;
        goto L_0x0115;
    L_0x018d:
        r11 = move-exception;
        r5 = r6;
        goto L_0x0156;
    L_0x0190:
        r11 = move-exception;
        goto L_0x0156;
    L_0x0192:
        r0 = move-exception;
        r5 = r6;
        goto L_0x012d;
    L_0x0195:
        r0 = move-exception;
        goto L_0x012d;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.cordova.media.AudioPlayer.moveFile(java.lang.String):void");
    }

    private static long copy(InputStream from, OutputStream to, boolean skipHeader) throws IOException {
        byte[] buf = new byte[8096];
        long total = 0;
        if (skipHeader) {
            from.skip(6);
        }
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                return total;
            }
            to.write(buf, 0, r);
            total += (long) r;
        }
    }

    public void stopRecording(boolean stop) {
        if (this.recorder != null) {
            try {
                if (this.state == STATE.MEDIA_RUNNING) {
                    this.recorder.stop();
                }
                this.recorder.reset();
                if (!this.tempFiles.contains(this.tempFile)) {
                    this.tempFiles.add(this.tempFile);
                }
                if (stop) {
                    LOG.m4d(LOG_TAG, "stopping recording");
                    setState(STATE.MEDIA_STOPPED);
                    moveFile(this.audioFile);
                    return;
                }
                LOG.m4d(LOG_TAG, "pause recording");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resumeRecording() {
        startRecording(this.audioFile);
    }

    public void startPlaying(String file) {
        if (!readyPlayer(file) || this.player == null) {
            this.prepareOnly = false;
            return;
        }
        this.player.start();
        setState(STATE.MEDIA_RUNNING);
        this.seekOnPrepared = 0;
    }

    public void seekToPlaying(int milliseconds) {
        if (readyPlayer(this.audioFile)) {
            if (milliseconds > 0) {
                this.player.seekTo(milliseconds);
            }
            LOG.m4d(LOG_TAG, "Send a onStatus update for the new seek");
            sendStatusChange(MEDIA_POSITION, null, Float.valueOf(((float) milliseconds) / 1000.0f));
            return;
        }
        this.seekOnPrepared = milliseconds;
    }

    public void pausePlaying() {
        if (this.state != STATE.MEDIA_RUNNING || this.player == null) {
            LOG.m4d(LOG_TAG, "AudioPlayer Error: pausePlaying() called during invalid state: " + this.state.ordinal());
            sendErrorStatus(MEDIA_ERR_NONE_ACTIVE);
            return;
        }
        this.player.pause();
        setState(STATE.MEDIA_PAUSED);
    }

    public void stopPlaying() {
        if (this.state == STATE.MEDIA_RUNNING || this.state == STATE.MEDIA_PAUSED) {
            this.player.pause();
            this.player.seekTo(0);
            LOG.m4d(LOG_TAG, "stopPlaying is calling stopped");
            setState(STATE.MEDIA_STOPPED);
            return;
        }
        LOG.m4d(LOG_TAG, "AudioPlayer Error: stopPlaying() called during invalid state: " + this.state.ordinal());
        sendErrorStatus(MEDIA_ERR_NONE_ACTIVE);
    }

    public void resumePlaying() {
        startPlaying(this.audioFile);
    }

    public void onCompletion(MediaPlayer player) {
        LOG.m4d(LOG_TAG, "on completion is calling stopped");
        setState(STATE.MEDIA_STOPPED);
    }

    public long getCurrentPosition() {
        if (this.state != STATE.MEDIA_RUNNING && this.state != STATE.MEDIA_PAUSED) {
            return -1;
        }
        int curPos = this.player.getCurrentPosition();
        sendStatusChange(MEDIA_POSITION, null, Float.valueOf(((float) curPos) / 1000.0f));
        return (long) curPos;
    }

    public boolean isStreaming(String file) {
        if (file.contains("http://") || file.contains("https://") || file.contains("rtsp://")) {
            return true;
        }
        return false;
    }

    public float getDuration(String file) {
        if (this.recorder != null) {
            return -2.0f;
        }
        if (this.player != null) {
            return this.duration;
        }
        this.prepareOnly = true;
        startPlaying(file);
        return this.duration;
    }

    public void onPrepared(MediaPlayer player) {
        this.player.setOnCompletionListener(this);
        seekToPlaying(this.seekOnPrepared);
        if (this.prepareOnly) {
            setState(STATE.MEDIA_STARTING);
        } else {
            this.player.start();
            setState(STATE.MEDIA_RUNNING);
            this.seekOnPrepared = 0;
        }
        this.duration = getDurationInSeconds();
        this.prepareOnly = true;
        sendStatusChange(MEDIA_DURATION, null, Float.valueOf(this.duration));
    }

    private float getDurationInSeconds() {
        return ((float) this.player.getDuration()) / 1000.0f;
    }

    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        LOG.m4d(LOG_TAG, "AudioPlayer.onError(" + arg1 + ", " + arg2 + ")");
        this.state = STATE.MEDIA_STOPPED;
        destroy();
        sendErrorStatus(arg1);
        return false;
    }

    private void setState(STATE state) {
        if (this.state != state) {
            sendStatusChange(MEDIA_STATE, null, Float.valueOf((float) state.ordinal()));
        }
        this.state = state;
    }

    private void setMode(MODE mode) {
        if (this.mode != mode) {
            this.mode = mode;
        } else {
            this.mode = mode;
        }
    }

    public int getState() {
        return this.state.ordinal();
    }

    public void setVolume(float volume) {
        if (this.player != null) {
            this.player.setVolume(volume, volume);
            return;
        }
        LOG.m4d(LOG_TAG, "AudioPlayer Error: Cannot set volume until the audio file is initialized.");
        sendErrorStatus(MEDIA_ERR_NONE_ACTIVE);
    }

    public void setRate(float rate) {
        if (this.player == null) {
            LOG.m4d(LOG_TAG, "AudioPlayer Error: Cannot set volume until the audio file is initialized.");
            sendErrorStatus(MEDIA_ERR_NONE_ACTIVE);
        } else if (VERSION.SDK_INT >= 23) {
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(rate);
            this.player.setPlaybackParams(params);
        }
    }

    private boolean playMode() {
        switch (C02531.$SwitchMap$org$apache$cordova$media$AudioPlayer$MODE[this.mode.ordinal()]) {
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                setMode(MODE.PLAY);
                break;
            case FileUtils.WRITE /*3*/:
                LOG.m4d(LOG_TAG, "AudioPlayer Error: Can't play in record mode.");
                sendErrorStatus(MEDIA_ERR_ABORTED);
                return false;
        }
        return true;
    }

    private boolean readyPlayer(String file) {
        if (!playMode()) {
            return false;
        }
        switch (C02531.$SwitchMap$org$apache$cordova$media$AudioPlayer$STATE[this.state.ordinal()]) {
            case FileUtils.ACTION_WRITE /*1*/:
                if (this.player == null) {
                    this.player = new MediaPlayer();
                    this.player.setOnErrorListener(this);
                }
                try {
                    loadAudioFile(file);
                    return false;
                } catch (Exception e) {
                    sendErrorStatus(MEDIA_ERR_ABORTED);
                    return false;
                }
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                LOG.m4d(LOG_TAG, "AudioPlayer Loading: startPlaying() called during media preparation: " + STATE.MEDIA_STARTING.ordinal());
                this.prepareOnly = false;
                return false;
            case FileUtils.WRITE /*3*/:
            case FileUtils.READ /*4*/:
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                return true;
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                if (file == null || this.audioFile.compareTo(file) != 0) {
                    this.player.reset();
                    try {
                        loadAudioFile(file);
                        return false;
                    } catch (Exception e2) {
                        sendErrorStatus(MEDIA_ERR_ABORTED);
                        return false;
                    }
                } else if (this.player == null) {
                    this.player = new MediaPlayer();
                    this.player.setOnErrorListener(this);
                    this.prepareOnly = false;
                    try {
                        loadAudioFile(file);
                        return false;
                    } catch (Exception e3) {
                        sendErrorStatus(MEDIA_ERR_ABORTED);
                        return false;
                    }
                } else {
                    this.player.seekTo(0);
                    this.player.pause();
                    return true;
                }
            default:
                LOG.m4d(LOG_TAG, "AudioPlayer Error: startPlaying() called during invalid state: " + this.state);
                sendErrorStatus(MEDIA_ERR_ABORTED);
                return false;
        }
    }

    private void loadAudioFile(String file) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        if (isStreaming(file)) {
            this.player.setDataSource(file);
            this.player.setAudioStreamType(3);
            setMode(MODE.PLAY);
            setState(STATE.MEDIA_STARTING);
            this.player.setOnPreparedListener(this);
            this.player.prepareAsync();
            return;
        }
        if (file.startsWith("/android_asset/")) {
            AssetFileDescriptor fd = this.handler.cordova.getActivity().getAssets().openFd(file.substring(15));
            this.player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
        } else if (new File(file).exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            this.player.setDataSource(fileInputStream.getFD());
            fileInputStream.close();
        } else {
            this.player.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + file);
        }
        setState(STATE.MEDIA_STARTING);
        this.player.setOnPreparedListener(this);
        this.player.prepare();
        this.duration = getDurationInSeconds();
    }

    private void sendErrorStatus(int errorCode) {
        sendStatusChange(MEDIA_ERROR, Integer.valueOf(errorCode), null);
    }

    private void sendStatusChange(int messageType, Integer additionalCode, Float value) {
        if (additionalCode == null || value == null) {
            JSONObject statusDetails = new JSONObject();
            try {
                statusDetails.put("id", this.id);
                statusDetails.put("msgType", messageType);
                if (additionalCode != null) {
                    JSONObject code = new JSONObject();
                    code.put("code", additionalCode.intValue());
                    statusDetails.put("value", code);
                } else if (value != null) {
                    statusDetails.put("value", (double) value.floatValue());
                }
            } catch (Throwable e) {
                LOG.m8e(LOG_TAG, "Failed to create status details", e);
            }
            this.handler.sendEventMessage(NotificationCompatApi24.CATEGORY_STATUS, statusDetails);
            return;
        }
        throw new IllegalArgumentException("Only one of additionalCode or value can be specified, not both");
    }

    public float getCurrentAmplitude() {
        if (this.recorder != null) {
            try {
                if (this.state == STATE.MEDIA_RUNNING) {
                    return ((float) this.recorder.getMaxAmplitude()) / 32762.0f;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0.0f;
    }
}
