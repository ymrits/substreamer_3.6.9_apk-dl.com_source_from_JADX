package org.apache.cordova.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.globalization.Globalization;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentFilesystem extends Filesystem {
    private final Context context;

    public ContentFilesystem(Context context, CordovaResourceApi resourceApi) {
        super(Uri.parse("content://"), "content", resourceApi);
        this.context = context;
    }

    public Uri toNativeUri(LocalFilesystemURL inputURL) {
        String authorityAndPath = inputURL.uri.getEncodedPath().substring(this.name.length() + 2);
        if (authorityAndPath.length() < 2) {
            return null;
        }
        String ret = "content://" + authorityAndPath;
        String query = inputURL.uri.getEncodedQuery();
        if (query != null) {
            ret = ret + '?' + query;
        }
        String frag = inputURL.uri.getEncodedFragment();
        if (frag != null) {
            ret = ret + '#' + frag;
        }
        return Uri.parse(ret);
    }

    public LocalFilesystemURL toLocalUri(Uri inputURL) {
        if (!"content".equals(inputURL.getScheme())) {
            return null;
        }
        String subPath = inputURL.getEncodedPath();
        if (subPath.length() > 0) {
            subPath = subPath.substring(1);
        }
        Builder b = new Builder().scheme(LocalFilesystemURL.FILESYSTEM_PROTOCOL).authority("localhost").path(this.name).appendPath(inputURL.getAuthority());
        if (subPath.length() > 0) {
            b.appendEncodedPath(subPath);
        }
        return LocalFilesystemURL.parse(b.encodedQuery(inputURL.getEncodedQuery()).encodedFragment(inputURL.getEncodedFragment()).build());
    }

    public JSONObject getFileForLocalURL(LocalFilesystemURL inputURL, String fileName, JSONObject options, boolean directory) throws IOException, TypeMismatchException, JSONException {
        throw new UnsupportedOperationException("getFile() not supported for content:. Use resolveLocalFileSystemURL instead.");
    }

    public boolean removeFileAtLocalURL(LocalFilesystemURL inputURL) throws NoModificationAllowedException {
        Uri contentUri = toNativeUri(inputURL);
        try {
            this.context.getContentResolver().delete(contentUri, null, null);
            return true;
        } catch (UnsupportedOperationException t) {
            NoModificationAllowedException nmae = new NoModificationAllowedException("Deleting not supported for content uri: " + contentUri);
            nmae.initCause(t);
            throw nmae;
        }
    }

    public boolean recursiveRemoveFileAtLocalURL(LocalFilesystemURL inputURL) throws NoModificationAllowedException {
        throw new NoModificationAllowedException("Cannot remove content url");
    }

    public LocalFilesystemURL[] listChildren(LocalFilesystemURL inputURL) throws FileNotFoundException {
        throw new UnsupportedOperationException("readEntriesAtLocalURL() not supported for content:. Use resolveLocalFileSystemURL instead.");
    }

    public JSONObject getFileMetadataForLocalURL(LocalFilesystemURL inputURL) throws FileNotFoundException {
        JSONObject metadata;
        long size = -1;
        long lastModified = 0;
        Uri nativeUri = toNativeUri(inputURL);
        String mimeType = this.resourceApi.getMimeType(nativeUri);
        Cursor cursor = openCursorForURL(nativeUri);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    Long sizeForCursor = resourceSizeForCursor(cursor);
                    if (sizeForCursor != null) {
                        size = sizeForCursor.longValue();
                    }
                    Long modified = lastModifiedDateForCursor(cursor);
                    if (modified != null) {
                        lastModified = modified.longValue();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    metadata = new JSONObject();
                    metadata.put("size", size);
                    metadata.put(Globalization.TYPE, mimeType);
                    metadata.put("name", this.name);
                    metadata.put("fullPath", inputURL.path);
                    metadata.put("lastModifiedDate", lastModified);
                    return metadata;
                }
            } catch (IOException e) {
                FileNotFoundException fnfe = new FileNotFoundException();
                fnfe.initCause(e);
                throw fnfe;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        size = this.resourceApi.openForRead(nativeUri).length;
        if (cursor != null) {
            cursor.close();
        }
        metadata = new JSONObject();
        try {
            metadata.put("size", size);
            metadata.put(Globalization.TYPE, mimeType);
            metadata.put("name", this.name);
            metadata.put("fullPath", inputURL.path);
            metadata.put("lastModifiedDate", lastModified);
            return metadata;
        } catch (JSONException e2) {
            return null;
        }
    }

    public long writeToFileAtURL(LocalFilesystemURL inputURL, String data, int offset, boolean isBinary) throws NoModificationAllowedException {
        throw new NoModificationAllowedException("Couldn't write to file given its content URI");
    }

    public long truncateFileAtURL(LocalFilesystemURL inputURL, long size) throws NoModificationAllowedException {
        throw new NoModificationAllowedException("Couldn't truncate file given its content URI");
    }

    protected Cursor openCursorForURL(Uri nativeUri) {
        try {
            return this.context.getContentResolver().query(nativeUri, null, null, null, null);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    private Long resourceSizeForCursor(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("_size");
        if (columnIndex != -1) {
            String sizeStr = cursor.getString(columnIndex);
            if (sizeStr != null) {
                return Long.valueOf(Long.parseLong(sizeStr));
            }
        }
        return null;
    }

    protected Long lastModifiedDateForCursor(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex("date_modified");
        if (columnIndex == -1) {
            columnIndex = cursor.getColumnIndex("last_modified");
        }
        if (columnIndex != -1) {
            String dateStr = cursor.getString(columnIndex);
            if (dateStr != null) {
                return Long.valueOf(Long.parseLong(dateStr));
            }
        }
        return null;
    }

    public String filesystemPathForURL(LocalFilesystemURL url) {
        File f = this.resourceApi.mapUriToFile(toNativeUri(url));
        return f == null ? null : f.getAbsolutePath();
    }

    public LocalFilesystemURL URLforFilesystemPath(String path) {
        return null;
    }

    public boolean canRemoveFileAtLocalURL(LocalFilesystemURL inputURL) {
        return true;
    }
}
