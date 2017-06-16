package org.apache.cordova;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Locale;

public class CordovaResourceApi {
    private static final String[] LOCAL_FILE_PROJECTION;
    private static final String LOG_TAG = "CordovaResourceApi";
    public static final String PLUGIN_URI_SCHEME = "cdvplugin";
    public static final int URI_TYPE_ASSET = 1;
    public static final int URI_TYPE_CONTENT = 2;
    public static final int URI_TYPE_DATA = 4;
    public static final int URI_TYPE_FILE = 0;
    public static final int URI_TYPE_HTTP = 5;
    public static final int URI_TYPE_HTTPS = 6;
    public static final int URI_TYPE_PLUGIN = 7;
    public static final int URI_TYPE_RESOURCE = 3;
    public static final int URI_TYPE_UNKNOWN = -1;
    public static Thread jsThread;
    private final AssetManager assetManager;
    private final ContentResolver contentResolver;
    private final PluginManager pluginManager;
    private boolean threadCheckingEnabled;

    public static final class OpenForReadResult {
        public final AssetFileDescriptor assetFd;
        public final InputStream inputStream;
        public final long length;
        public final String mimeType;
        public final Uri uri;

        public OpenForReadResult(Uri uri, InputStream inputStream, String mimeType, long length, AssetFileDescriptor assetFd) {
            this.uri = uri;
            this.inputStream = inputStream;
            this.mimeType = mimeType;
            this.length = length;
            this.assetFd = assetFd;
        }
    }

    static {
        String[] strArr = new String[URI_TYPE_ASSET];
        strArr[URI_TYPE_FILE] = "_data";
        LOCAL_FILE_PROJECTION = strArr;
    }

    public CordovaResourceApi(Context context, PluginManager pluginManager) {
        this.threadCheckingEnabled = true;
        this.contentResolver = context.getContentResolver();
        this.assetManager = context.getAssets();
        this.pluginManager = pluginManager;
    }

    public void setThreadCheckingEnabled(boolean value) {
        this.threadCheckingEnabled = value;
    }

    public boolean isThreadCheckingEnabled() {
        return this.threadCheckingEnabled;
    }

    public static int getUriType(Uri uri) {
        assertNonRelative(uri);
        String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            return URI_TYPE_CONTENT;
        }
        if ("android.resource".equalsIgnoreCase(scheme)) {
            return URI_TYPE_RESOURCE;
        }
        if ("file".equalsIgnoreCase(scheme)) {
            if (uri.getPath().startsWith("/android_asset/")) {
                return URI_TYPE_ASSET;
            }
            return URI_TYPE_FILE;
        } else if ("data".equalsIgnoreCase(scheme)) {
            return URI_TYPE_DATA;
        } else {
            if ("http".equalsIgnoreCase(scheme)) {
                return URI_TYPE_HTTP;
            }
            if ("https".equalsIgnoreCase(scheme)) {
                return URI_TYPE_HTTPS;
            }
            if (PLUGIN_URI_SCHEME.equalsIgnoreCase(scheme)) {
                return URI_TYPE_PLUGIN;
            }
            return URI_TYPE_UNKNOWN;
        }
    }

    public Uri remapUri(Uri uri) {
        assertNonRelative(uri);
        Uri pluginUri = this.pluginManager.remapUri(uri);
        return pluginUri != null ? pluginUri : uri;
    }

    public String remapPath(String path) {
        return remapUri(Uri.fromFile(new File(path))).getPath();
    }

    public File mapUriToFile(Uri uri) {
        File file = null;
        assertBackgroundThread();
        switch (getUriType(uri)) {
            case URI_TYPE_FILE /*0*/:
                return new File(uri.getPath());
            case URI_TYPE_CONTENT /*2*/:
                Cursor cursor = this.contentResolver.query(uri, LOCAL_FILE_PROJECTION, null, null, null);
                if (cursor == null) {
                    return null;
                }
                try {
                    int columnIndex = cursor.getColumnIndex(LOCAL_FILE_PROJECTION[URI_TYPE_FILE]);
                    if (columnIndex != URI_TYPE_UNKNOWN && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        String realPath = cursor.getString(columnIndex);
                        if (realPath != null) {
                            file = new File(realPath);
                            return file;
                        }
                    }
                    cursor.close();
                    return null;
                } finally {
                    cursor.close();
                }
            default:
                return null;
        }
    }

    public String getMimeType(Uri uri) {
        switch (getUriType(uri)) {
            case URI_TYPE_FILE /*0*/:
            case URI_TYPE_ASSET /*1*/:
                return getMimeTypeFromPath(uri.getPath());
            case URI_TYPE_CONTENT /*2*/:
            case URI_TYPE_RESOURCE /*3*/:
                return this.contentResolver.getType(uri);
            case URI_TYPE_DATA /*4*/:
                return getDataUriMimeType(uri);
            case URI_TYPE_HTTP /*5*/:
            case URI_TYPE_HTTPS /*6*/:
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
                    conn.setDoInput(false);
                    conn.setRequestMethod("HEAD");
                    String mimeType = conn.getHeaderField("Content-Type");
                    if (mimeType != null) {
                        return mimeType.split(";")[URI_TYPE_FILE];
                    }
                    return mimeType;
                } catch (IOException e) {
                    break;
                }
        }
        return null;
    }

    private String getMimeTypeFromPath(String path) {
        String extension = path;
        int lastDot = extension.lastIndexOf(46);
        if (lastDot != URI_TYPE_UNKNOWN) {
            extension = extension.substring(lastDot + URI_TYPE_ASSET);
        }
        extension = extension.toLowerCase(Locale.getDefault());
        if (extension.equals("3ga")) {
            return "audio/3gpp";
        }
        if (extension.equals("js")) {
            return "text/javascript";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public OpenForReadResult openForRead(Uri uri) throws IOException {
        return openForRead(uri, false);
    }

    public OpenForReadResult openForRead(Uri uri, boolean skipThreadCheck) throws IOException {
        if (!skipThreadCheck) {
            assertBackgroundThread();
        }
        AssetFileDescriptor assetFd;
        String mimeType;
        switch (getUriType(uri)) {
            case URI_TYPE_FILE /*0*/:
                FileInputStream inputStream = new FileInputStream(uri.getPath());
                return new OpenForReadResult(uri, inputStream, getMimeTypeFromPath(uri.getPath()), inputStream.getChannel().size(), null);
            case URI_TYPE_ASSET /*1*/:
                InputStream inputStream2;
                String assetPath = uri.getPath().substring(15);
                assetFd = null;
                long length = -1;
                try {
                    assetFd = this.assetManager.openFd(assetPath);
                    inputStream2 = assetFd.createInputStream();
                    length = assetFd.getLength();
                } catch (FileNotFoundException e) {
                    inputStream2 = this.assetManager.open(assetPath);
                }
                return new OpenForReadResult(uri, inputStream2, getMimeTypeFromPath(assetPath), length, assetFd);
            case URI_TYPE_CONTENT /*2*/:
            case URI_TYPE_RESOURCE /*3*/:
                mimeType = this.contentResolver.getType(uri);
                assetFd = this.contentResolver.openAssetFileDescriptor(uri, "r");
                return new OpenForReadResult(uri, assetFd.createInputStream(), mimeType, assetFd.getLength(), assetFd);
            case URI_TYPE_DATA /*4*/:
                OpenForReadResult ret = readDataUri(uri);
                if (ret != null) {
                    return ret;
                }
                break;
            case URI_TYPE_HTTP /*5*/:
            case URI_TYPE_HTTPS /*6*/:
                HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
                conn.setDoInput(true);
                mimeType = conn.getHeaderField("Content-Type");
                if (mimeType != null) {
                    mimeType = mimeType.split(";")[URI_TYPE_FILE];
                }
                int length2 = conn.getContentLength();
                return new OpenForReadResult(uri, conn.getInputStream(), mimeType, (long) length2, null);
            case URI_TYPE_PLUGIN /*7*/:
                CordovaPlugin plugin = this.pluginManager.getPlugin(uri.getHost());
                if (plugin != null) {
                    return plugin.handleOpenForRead(uri);
                }
                throw new FileNotFoundException("Invalid plugin ID in URI: " + uri);
        }
        throw new FileNotFoundException("URI not supported by CordovaResourceApi: " + uri);
    }

    public OutputStream openOutputStream(Uri uri) throws IOException {
        return openOutputStream(uri, false);
    }

    public OutputStream openOutputStream(Uri uri, boolean append) throws IOException {
        assertBackgroundThread();
        switch (getUriType(uri)) {
            case URI_TYPE_FILE /*0*/:
                File localFile = new File(uri.getPath());
                File parent = localFile.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                return new FileOutputStream(localFile, append);
            case URI_TYPE_CONTENT /*2*/:
            case URI_TYPE_RESOURCE /*3*/:
                return this.contentResolver.openAssetFileDescriptor(uri, append ? "wa" : "w").createOutputStream();
            default:
                throw new FileNotFoundException("URI not supported by CordovaResourceApi: " + uri);
        }
    }

    public HttpURLConnection createHttpConnection(Uri uri) throws IOException {
        assertBackgroundThread();
        return (HttpURLConnection) new URL(uri.toString()).openConnection();
    }

    public void copyResource(OpenForReadResult input, OutputStream outputStream) throws IOException {
        assertBackgroundThread();
        try {
            InputStream inputStream = input.inputStream;
            if (!(inputStream instanceof FileInputStream) || !(outputStream instanceof FileOutputStream)) {
                byte[] buffer = new byte[AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD];
                while (true) {
                    int bytesRead = inputStream.read(buffer, URI_TYPE_FILE, AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                    if (bytesRead <= 0) {
                        break;
                    }
                    outputStream.write(buffer, URI_TYPE_FILE, bytesRead);
                }
            } else {
                FileChannel inChannel = ((FileInputStream) input.inputStream).getChannel();
                FileChannel outChannel = ((FileOutputStream) outputStream).getChannel();
                long offset = 0;
                long length = input.length;
                if (input.assetFd != null) {
                    offset = input.assetFd.getStartOffset();
                }
                inChannel.position(offset);
                outChannel.transferFrom(inChannel, 0, length);
            }
            input.inputStream.close();
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Throwable th) {
            input.inputStream.close();
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    public void copyResource(Uri sourceUri, OutputStream outputStream) throws IOException {
        copyResource(openForRead(sourceUri), outputStream);
    }

    public void copyResource(Uri sourceUri, Uri dstUri) throws IOException {
        copyResource(openForRead(sourceUri), openOutputStream(dstUri));
    }

    private void assertBackgroundThread() {
        if (this.threadCheckingEnabled) {
            Thread curThread = Thread.currentThread();
            if (curThread == Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("Do not perform IO operations on the UI thread. Use CordovaInterface.getThreadPool() instead.");
            } else if (curThread == jsThread) {
                throw new IllegalStateException("Tried to perform an IO operation on the WebCore thread. Use CordovaInterface.getThreadPool() instead.");
            }
        }
    }

    private String getDataUriMimeType(Uri uri) {
        String uriAsString = uri.getSchemeSpecificPart();
        int commaPos = uriAsString.indexOf(44);
        if (commaPos == URI_TYPE_UNKNOWN) {
            return null;
        }
        String[] mimeParts = uriAsString.substring(URI_TYPE_FILE, commaPos).split(";");
        if (mimeParts.length > 0) {
            return mimeParts[URI_TYPE_FILE];
        }
        return null;
    }

    private OpenForReadResult readDataUri(Uri uri) {
        String uriAsString = uri.getSchemeSpecificPart();
        int commaPos = uriAsString.indexOf(44);
        if (commaPos == URI_TYPE_UNKNOWN) {
            return null;
        }
        byte[] data;
        String[] mimeParts = uriAsString.substring(URI_TYPE_FILE, commaPos).split(";");
        String contentType = null;
        boolean base64 = false;
        if (mimeParts.length > 0) {
            contentType = mimeParts[URI_TYPE_FILE];
        }
        for (int i = URI_TYPE_ASSET; i < mimeParts.length; i += URI_TYPE_ASSET) {
            if ("base64".equalsIgnoreCase(mimeParts[i])) {
                base64 = true;
            }
        }
        String dataPartAsString = uriAsString.substring(commaPos + URI_TYPE_ASSET);
        if (base64) {
            data = Base64.decode(dataPartAsString, URI_TYPE_FILE);
        } else {
            try {
                data = dataPartAsString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                data = dataPartAsString.getBytes();
            }
        }
        return new OpenForReadResult(uri, new ByteArrayInputStream(data), contentType, (long) data.length, null);
    }

    private static void assertNonRelative(Uri uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("Relative URIs are not supported.");
        }
    }
}
