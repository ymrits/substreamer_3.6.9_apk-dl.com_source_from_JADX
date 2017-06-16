package org.apache.cordova.file;

import android.os.Environment;
import android.os.StatFs;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.File;
import org.apache.cordova.BuildConfig;

public class DirectoryManager {
    private static final String LOG_TAG = "DirectoryManager";

    public static boolean testFileExists(String name) {
        if (!testSaveLocationExists() || name.equals(BuildConfig.FLAVOR)) {
            return false;
        }
        return constructFilePaths(Environment.getExternalStorageDirectory().toString(), name).exists();
    }

    public static long getFreeExternalStorageSpace() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return getFreeSpaceInBytes(Environment.getExternalStorageDirectory().getPath()) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        }
        return -1;
    }

    public static long getFreeSpaceInBytes(String path) {
        try {
            StatFs stat = new StatFs(path);
            return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static boolean testSaveLocationExists() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return true;
        }
        return false;
    }

    private static File constructFilePaths(String file1, String file2) {
        if (file2.startsWith(file1)) {
            return new File(file2);
        }
        return new File(file1 + "/" + file2);
    }
}
