package org.apache.cordova.file;

import android.net.Uri;

public class LocalFilesystemURL {
    public static final String FILESYSTEM_PROTOCOL = "cdvfile";
    public final String fsName;
    public final boolean isDirectory;
    public final String path;
    public final Uri uri;

    private LocalFilesystemURL(Uri uri, String fsName, String fsPath, boolean isDirectory) {
        this.uri = uri;
        this.fsName = fsName;
        this.path = fsPath;
        this.isDirectory = isDirectory;
    }

    public static LocalFilesystemURL parse(Uri uri) {
        boolean isDirectory = true;
        if (!FILESYSTEM_PROTOCOL.equals(uri.getScheme())) {
            return null;
        }
        String path = uri.getPath();
        if (path.length() < 1) {
            return null;
        }
        int firstSlashIdx = path.indexOf(47, 1);
        if (firstSlashIdx < 0) {
            return null;
        }
        String fsName = path.substring(1, firstSlashIdx);
        path = path.substring(firstSlashIdx);
        if (path.charAt(path.length() - 1) != '/') {
            isDirectory = false;
        }
        return new LocalFilesystemURL(uri, fsName, path, isDirectory);
    }

    public static LocalFilesystemURL parse(String uri) {
        return parse(Uri.parse(uri));
    }

    public String toString() {
        return this.uri.toString();
    }
}
