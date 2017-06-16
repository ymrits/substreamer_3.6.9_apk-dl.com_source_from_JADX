package org.apache.cordova.file;

import android.content.res.AssetManager;
import android.net.Uri;
import android.net.Uri.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.LOG;
import org.apache.cordova.globalization.Globalization;
import org.json.JSONException;
import org.json.JSONObject;

public class AssetFilesystem extends Filesystem {
    private static final String LOG_TAG = "AssetFilesystem";
    private static Map<String, Long> lengthCache;
    private static Map<String, String[]> listCache;
    private static boolean listCacheFromFile;
    private static Object listCacheLock;
    private final AssetManager assetManager;

    static {
        listCacheLock = new Object();
    }

    private void lazyInitCaches() {
        ClassNotFoundException e;
        Throwable th;
        synchronized (listCacheLock) {
            if (listCache == null) {
                ObjectInputStream ois = null;
                try {
                    ObjectInputStream ois2 = new ObjectInputStream(this.assetManager.open("cdvasset.manifest"));
                    try {
                        listCache = (Map) ois2.readObject();
                        lengthCache = (Map) ois2.readObject();
                        listCacheFromFile = true;
                        if (ois2 != null) {
                            try {
                                ois2.close();
                                ois = ois2;
                            } catch (IOException e2) {
                                LOG.m4d(LOG_TAG, e2.getLocalizedMessage());
                                ois = ois2;
                            }
                        }
                    } catch (ClassNotFoundException e3) {
                        e = e3;
                        ois = ois2;
                        try {
                            e.printStackTrace();
                            if (ois != null) {
                                try {
                                    ois.close();
                                } catch (IOException e22) {
                                    LOG.m4d(LOG_TAG, e22.getLocalizedMessage());
                                }
                            }
                            if (listCache == null) {
                                LOG.m16w(LOG_TAG, "Asset manifest not found. Recursive copies and directory listing will be slow.");
                                listCache = new HashMap();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (ois != null) {
                                try {
                                    ois.close();
                                } catch (IOException e222) {
                                    LOG.m4d(LOG_TAG, e222.getLocalizedMessage());
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        ois = ois2;
                        if (ois != null) {
                            try {
                                ois.close();
                            } catch (IOException e2222) {
                                LOG.m4d(LOG_TAG, e2222.getLocalizedMessage());
                            }
                        }
                        if (listCache == null) {
                            LOG.m16w(LOG_TAG, "Asset manifest not found. Recursive copies and directory listing will be slow.");
                            listCache = new HashMap();
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        ois = ois2;
                        if (ois != null) {
                            ois.close();
                        }
                        throw th;
                    }
                } catch (ClassNotFoundException e5) {
                    e = e5;
                    e.printStackTrace();
                    if (ois != null) {
                        ois.close();
                    }
                    if (listCache == null) {
                        LOG.m16w(LOG_TAG, "Asset manifest not found. Recursive copies and directory listing will be slow.");
                        listCache = new HashMap();
                    }
                } catch (IOException e6) {
                    if (ois != null) {
                        ois.close();
                    }
                    if (listCache == null) {
                        LOG.m16w(LOG_TAG, "Asset manifest not found. Recursive copies and directory listing will be slow.");
                        listCache = new HashMap();
                    }
                }
                if (listCache == null) {
                    LOG.m16w(LOG_TAG, "Asset manifest not found. Recursive copies and directory listing will be slow.");
                    listCache = new HashMap();
                }
            }
        }
    }

    private String[] listAssets(String assetPath) throws IOException {
        if (assetPath.startsWith("/")) {
            assetPath = assetPath.substring(1);
        }
        if (assetPath.endsWith("/")) {
            assetPath = assetPath.substring(0, assetPath.length() - 1);
        }
        lazyInitCaches();
        String[] ret = (String[]) listCache.get(assetPath);
        if (ret != null) {
            return ret;
        }
        if (listCacheFromFile) {
            return new String[0];
        }
        ret = this.assetManager.list(assetPath);
        listCache.put(assetPath, ret);
        return ret;
    }

    private long getAssetSize(String assetPath) throws FileNotFoundException {
        if (assetPath.startsWith("/")) {
            assetPath = assetPath.substring(1);
        }
        lazyInitCaches();
        if (lengthCache != null) {
            Long ret = (Long) lengthCache.get(assetPath);
            if (ret != null) {
                return ret.longValue();
            }
            throw new FileNotFoundException("Asset not found: " + assetPath);
        }
        try {
            OpenForReadResult offr = this.resourceApi.openForRead(nativeUriForFullPath(assetPath));
            long length = offr.length;
            if (length < 0) {
                length = (long) offr.inputStream.available();
            }
            if (offr == null) {
                return length;
            }
            try {
                offr.inputStream.close();
                return length;
            } catch (IOException e) {
                LOG.m4d(LOG_TAG, e.getLocalizedMessage());
                return length;
            }
        } catch (IOException e2) {
            FileNotFoundException fnfe = new FileNotFoundException("File not found: " + assetPath);
            fnfe.initCause(e2);
            throw fnfe;
        } catch (Throwable th) {
            if (null != null) {
                try {
                    null.inputStream.close();
                } catch (IOException e22) {
                    LOG.m4d(LOG_TAG, e22.getLocalizedMessage());
                }
            }
        }
    }

    public AssetFilesystem(AssetManager assetManager, CordovaResourceApi resourceApi) {
        super(Uri.parse("file:///android_asset/"), "assets", resourceApi);
        this.assetManager = assetManager;
    }

    public Uri toNativeUri(LocalFilesystemURL inputURL) {
        return nativeUriForFullPath(inputURL.path);
    }

    public LocalFilesystemURL toLocalUri(Uri inputURL) {
        if (!"file".equals(inputURL.getScheme())) {
            return null;
        }
        Uri resolvedUri = Uri.fromFile(new File(inputURL.getPath()));
        String rootUriNoTrailingSlash = this.rootUri.getEncodedPath();
        rootUriNoTrailingSlash = rootUriNoTrailingSlash.substring(0, rootUriNoTrailingSlash.length() - 1);
        if (!resolvedUri.getEncodedPath().startsWith(rootUriNoTrailingSlash)) {
            return null;
        }
        String subPath = resolvedUri.getEncodedPath().substring(rootUriNoTrailingSlash.length());
        if (!subPath.isEmpty()) {
            subPath = subPath.substring(1);
        }
        Builder b = new Builder().scheme(LocalFilesystemURL.FILESYSTEM_PROTOCOL).authority("localhost").path(this.name);
        if (!subPath.isEmpty()) {
            b.appendEncodedPath(subPath);
        }
        if (isDirectory(subPath) || inputURL.getPath().endsWith("/")) {
            b.appendEncodedPath(BuildConfig.FLAVOR);
        }
        return LocalFilesystemURL.parse(b.build());
    }

    private boolean isDirectory(String assetPath) {
        try {
            return listAssets(assetPath).length != 0;
        } catch (IOException e) {
            return false;
        }
    }

    public LocalFilesystemURL[] listChildren(LocalFilesystemURL inputURL) throws FileNotFoundException {
        String pathNoSlashes = inputURL.path.substring(1);
        if (pathNoSlashes.endsWith("/")) {
            pathNoSlashes = pathNoSlashes.substring(0, pathNoSlashes.length() - 1);
        }
        try {
            String[] files = listAssets(pathNoSlashes);
            LocalFilesystemURL[] entries = new LocalFilesystemURL[files.length];
            for (int i = 0; i < files.length; i++) {
                entries[i] = localUrlforFullPath(new File(inputURL.path, files[i]).getPath());
            }
            return entries;
        } catch (IOException e) {
            FileNotFoundException fnfe = new FileNotFoundException();
            fnfe.initCause(e);
            throw fnfe;
        }
    }

    public JSONObject getFileForLocalURL(LocalFilesystemURL inputURL, String path, JSONObject options, boolean directory) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
        if (options == null || !options.optBoolean("create")) {
            LocalFilesystemURL requestedURL;
            if (directory && !path.endsWith("/")) {
                path = path + "/";
            }
            if (path.startsWith("/")) {
                requestedURL = localUrlforFullPath(Filesystem.normalizePath(path));
            } else {
                requestedURL = localUrlforFullPath(Filesystem.normalizePath(inputURL.path + "/" + path));
            }
            getFileMetadataForLocalURL(requestedURL);
            boolean isDir = isDirectory(requestedURL.path);
            if (directory && !isDir) {
                throw new TypeMismatchException("path doesn't exist or is file");
            } else if (directory || !isDir) {
                return makeEntryForURL(requestedURL);
            } else {
                throw new TypeMismatchException("path doesn't exist or is directory");
            }
        }
        throw new UnsupportedOperationException("Assets are read-only");
    }

    public JSONObject getFileMetadataForLocalURL(LocalFilesystemURL inputURL) throws FileNotFoundException {
        JSONObject metadata = new JSONObject();
        try {
            metadata.put("size", inputURL.isDirectory ? 0 : getAssetSize(inputURL.path));
            metadata.put(Globalization.TYPE, inputURL.isDirectory ? "text/directory" : this.resourceApi.getMimeType(toNativeUri(inputURL)));
            metadata.put("name", new File(inputURL.path).getName());
            metadata.put("fullPath", inputURL.path);
            metadata.put("lastModifiedDate", 0);
            return metadata;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean canRemoveFileAtLocalURL(LocalFilesystemURL inputURL) {
        return false;
    }

    long writeToFileAtURL(LocalFilesystemURL inputURL, String data, int offset, boolean isBinary) throws NoModificationAllowedException, IOException {
        throw new NoModificationAllowedException("Assets are read-only");
    }

    long truncateFileAtURL(LocalFilesystemURL inputURL, long size) throws IOException, NoModificationAllowedException {
        throw new NoModificationAllowedException("Assets are read-only");
    }

    String filesystemPathForURL(LocalFilesystemURL url) {
        return new File(this.rootUri.getPath(), url.path).toString();
    }

    LocalFilesystemURL URLforFilesystemPath(String path) {
        return null;
    }

    boolean removeFileAtLocalURL(LocalFilesystemURL inputURL) throws InvalidModificationException, NoModificationAllowedException {
        throw new NoModificationAllowedException("Assets are read-only");
    }

    boolean recursiveRemoveFileAtLocalURL(LocalFilesystemURL inputURL) throws NoModificationAllowedException {
        throw new NoModificationAllowedException("Assets are read-only");
    }
}
