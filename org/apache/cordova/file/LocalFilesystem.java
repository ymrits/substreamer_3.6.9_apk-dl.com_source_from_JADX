package org.apache.cordova.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build.VERSION;
import android.os.Environment;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.globalization.Globalization;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalFilesystem extends Filesystem {
    private final Context context;

    public LocalFilesystem(String name, Context context, CordovaResourceApi resourceApi, File fsRoot) {
        super(Uri.fromFile(fsRoot).buildUpon().appendEncodedPath(BuildConfig.FLAVOR).build(), name, resourceApi);
        this.context = context;
    }

    public String filesystemPathForFullPath(String fullPath) {
        return new File(this.rootUri.getPath(), fullPath).toString();
    }

    public String filesystemPathForURL(LocalFilesystemURL url) {
        return filesystemPathForFullPath(url.path);
    }

    private String fullPathForFilesystemPath(String absolutePath) {
        if (absolutePath == null || !absolutePath.startsWith(this.rootUri.getPath())) {
            return null;
        }
        return absolutePath.substring(this.rootUri.getPath().length() - 1);
    }

    public Uri toNativeUri(LocalFilesystemURL inputURL) {
        return nativeUriForFullPath(inputURL.path);
    }

    public LocalFilesystemURL toLocalUri(Uri inputURL) {
        if (!"file".equals(inputURL.getScheme())) {
            return null;
        }
        File f = new File(inputURL.getPath());
        Uri resolvedUri = Uri.fromFile(f);
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
        if (f.isDirectory()) {
            b.appendEncodedPath(BuildConfig.FLAVOR);
        }
        return LocalFilesystemURL.parse(b.build());
    }

    public LocalFilesystemURL URLforFilesystemPath(String path) {
        return localUrlforFullPath(fullPathForFilesystemPath(path));
    }

    public JSONObject getFileForLocalURL(LocalFilesystemURL inputURL, String path, JSONObject options, boolean directory) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
        boolean create = false;
        boolean exclusive = false;
        if (options != null) {
            create = options.optBoolean("create");
            if (create) {
                exclusive = options.optBoolean("exclusive");
            }
        }
        if (path.contains(":")) {
            throw new EncodingException("This path has an invalid \":\" in it.");
        }
        LocalFilesystemURL requestedURL;
        if (directory && !path.endsWith("/")) {
            path = path + "/";
        }
        if (path.startsWith("/")) {
            requestedURL = localUrlforFullPath(Filesystem.normalizePath(path));
        } else {
            requestedURL = localUrlforFullPath(Filesystem.normalizePath(inputURL.path + "/" + path));
        }
        File fp = new File(filesystemPathForURL(requestedURL));
        if (create) {
            if (exclusive && fp.exists()) {
                throw new FileExistsException("create/exclusive fails");
            }
            if (directory) {
                fp.mkdir();
            } else {
                fp.createNewFile();
            }
            if (!fp.exists()) {
                throw new FileExistsException("create fails");
            }
        } else if (!fp.exists()) {
            throw new FileNotFoundException("path does not exist");
        } else if (directory) {
            if (fp.isFile()) {
                throw new TypeMismatchException("path doesn't exist or is file");
            }
        } else if (fp.isDirectory()) {
            throw new TypeMismatchException("path doesn't exist or is directory");
        }
        return makeEntryForURL(requestedURL);
    }

    public boolean removeFileAtLocalURL(LocalFilesystemURL inputURL) throws InvalidModificationException {
        File fp = new File(filesystemPathForURL(inputURL));
        if (!fp.isDirectory() || fp.list().length <= 0) {
            return fp.delete();
        }
        throw new InvalidModificationException("You can't delete a directory that is not empty.");
    }

    public boolean exists(LocalFilesystemURL inputURL) {
        return new File(filesystemPathForURL(inputURL)).exists();
    }

    public long getFreeSpaceInBytes() {
        return DirectoryManager.getFreeSpaceInBytes(this.rootUri.getPath());
    }

    public boolean recursiveRemoveFileAtLocalURL(LocalFilesystemURL inputURL) throws FileExistsException {
        return removeDirRecursively(new File(filesystemPathForURL(inputURL)));
    }

    protected boolean removeDirRecursively(File directory) throws FileExistsException {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                removeDirRecursively(file);
            }
        }
        if (directory.delete()) {
            return true;
        }
        throw new FileExistsException("could not delete: " + directory.getName());
    }

    public LocalFilesystemURL[] listChildren(LocalFilesystemURL inputURL) throws FileNotFoundException {
        File fp = new File(filesystemPathForURL(inputURL));
        if (fp.exists()) {
            File[] files = fp.listFiles();
            if (files == null) {
                return null;
            }
            LocalFilesystemURL[] entries = new LocalFilesystemURL[files.length];
            for (int i = 0; i < files.length; i++) {
                entries[i] = URLforFilesystemPath(files[i].getPath());
            }
            return entries;
        }
        throw new FileNotFoundException();
    }

    public JSONObject getFileMetadataForLocalURL(LocalFilesystemURL inputURL) throws FileNotFoundException {
        File file = new File(filesystemPathForURL(inputURL));
        if (file.exists()) {
            JSONObject metadata = new JSONObject();
            try {
                metadata.put("size", file.isDirectory() ? 0 : file.length());
                metadata.put(Globalization.TYPE, this.resourceApi.getMimeType(Uri.fromFile(file)));
                metadata.put("name", file.getName());
                metadata.put("fullPath", inputURL.path);
                metadata.put("lastModifiedDate", file.lastModified());
                return metadata;
            } catch (JSONException e) {
                return null;
            }
        }
        throw new FileNotFoundException("File at " + inputURL.uri + " does not exist.");
    }

    private void copyFile(Filesystem srcFs, LocalFilesystemURL srcURL, File destFile, boolean move) throws IOException, InvalidModificationException, NoModificationAllowedException {
        if (move) {
            String realSrcPath = srcFs.filesystemPathForURL(srcURL);
            if (realSrcPath != null && new File(realSrcPath).renameTo(destFile)) {
                return;
            }
        }
        copyResource(this.resourceApi.openForRead(srcFs.toNativeUri(srcURL)), new FileOutputStream(destFile));
        if (move) {
            srcFs.removeFileAtLocalURL(srcURL);
        }
    }

    private void copyDirectory(Filesystem srcFs, LocalFilesystemURL srcURL, File dstDir, boolean move) throws IOException, NoModificationAllowedException, InvalidModificationException, FileExistsException {
        if (move) {
            String realSrcPath = srcFs.filesystemPathForURL(srcURL);
            if (realSrcPath != null) {
                File srcDir = new File(realSrcPath);
                if (dstDir.exists()) {
                    if (dstDir.list().length > 0) {
                        throw new InvalidModificationException("directory is not empty");
                    }
                    dstDir.delete();
                }
                if (srcDir.renameTo(dstDir)) {
                    return;
                }
            }
        }
        if (dstDir.exists()) {
            if (dstDir.list().length > 0) {
                throw new InvalidModificationException("directory is not empty");
            }
        } else if (!dstDir.mkdir()) {
            throw new NoModificationAllowedException("Couldn't create the destination directory");
        }
        for (LocalFilesystemURL childLocalUrl : srcFs.listChildren(srcURL)) {
            File target = new File(dstDir, new File(childLocalUrl.path).getName());
            if (childLocalUrl.isDirectory) {
                copyDirectory(srcFs, childLocalUrl, target, false);
            } else {
                copyFile(srcFs, childLocalUrl, target, false);
            }
        }
        if (move) {
            srcFs.recursiveRemoveFileAtLocalURL(srcURL);
        }
    }

    public JSONObject copyFileToURL(LocalFilesystemURL destURL, String newName, Filesystem srcFs, LocalFilesystemURL srcURL, boolean move) throws IOException, InvalidModificationException, JSONException, NoModificationAllowedException, FileExistsException {
        if (new File(filesystemPathForURL(destURL)).exists()) {
            LocalFilesystemURL destinationURL = makeDestinationURL(newName, srcURL, destURL, srcURL.isDirectory);
            Uri dstNativeUri = toNativeUri(destinationURL);
            Uri srcNativeUri = srcFs.toNativeUri(srcURL);
            if (dstNativeUri.equals(srcNativeUri)) {
                throw new InvalidModificationException("Can't copy onto itself");
            } else if (!move || srcFs.canRemoveFileAtLocalURL(srcURL)) {
                File destFile = new File(dstNativeUri.getPath());
                if (destFile.exists()) {
                    if (!srcURL.isDirectory && destFile.isDirectory()) {
                        throw new InvalidModificationException("Can't copy/move a file to an existing directory");
                    } else if (srcURL.isDirectory && destFile.isFile()) {
                        throw new InvalidModificationException("Can't copy/move a directory to an existing file");
                    }
                }
                if (!srcURL.isDirectory) {
                    copyFile(srcFs, srcURL, destFile, move);
                } else if (dstNativeUri.toString().startsWith(srcNativeUri.toString() + '/')) {
                    throw new InvalidModificationException("Can't copy directory into itself");
                } else {
                    copyDirectory(srcFs, srcURL, destFile, move);
                }
                return makeEntryForURL(destinationURL);
            } else {
                throw new InvalidModificationException("Source URL is read-only (cannot move)");
            }
        }
        throw new FileNotFoundException("The source does not exist");
    }

    public long writeToFileAtURL(LocalFilesystemURL inputURL, String data, int offset, boolean isBinary) throws IOException, NoModificationAllowedException {
        byte[] rawData;
        FileOutputStream out;
        boolean append = false;
        if (offset > 0) {
            truncateFileAtURL(inputURL, (long) offset);
            append = true;
        }
        if (isBinary) {
            rawData = Base64.decode(data, 0);
        } else {
            rawData = data.getBytes(Charset.defaultCharset());
        }
        ByteArrayInputStream in = new ByteArrayInputStream(rawData);
        try {
            byte[] buff = new byte[rawData.length];
            String absolutePath = filesystemPathForURL(inputURL);
            out = new FileOutputStream(absolutePath, append);
            in.read(buff, 0, buff.length);
            out.write(buff, 0, rawData.length);
            out.flush();
            out.close();
            if (isPublicDirectory(absolutePath)) {
                broadcastNewFile(Uri.fromFile(new File(absolutePath)));
            }
            return (long) rawData.length;
        } catch (NullPointerException e) {
            NoModificationAllowedException realException = new NoModificationAllowedException(inputURL.toString());
            realException.initCause(e);
            throw realException;
        } catch (Throwable th) {
            out.close();
        }
    }

    private boolean isPublicDirectory(String absolutePath) {
        if (VERSION.SDK_INT >= 21) {
            for (File f : this.context.getExternalMediaDirs()) {
                if (f != null && absolutePath.startsWith(f.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return absolutePath.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void broadcastNewFile(Uri nativeUri) {
        this.context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", nativeUri));
    }

    public long truncateFileAtURL(LocalFilesystemURL inputURL, long size) throws IOException {
        if (new File(filesystemPathForURL(inputURL)).exists()) {
            RandomAccessFile raf = new RandomAccessFile(filesystemPathForURL(inputURL), "rw");
            try {
                if (raf.length() >= size) {
                    raf.getChannel().truncate(size);
                } else {
                    size = raf.length();
                    raf.close();
                }
                return size;
            } finally {
                raf.close();
            }
        } else {
            throw new FileNotFoundException("File at " + inputURL.uri + " does not exist.");
        }
    }

    public boolean canRemoveFileAtLocalURL(LocalFilesystemURL inputURL) {
        return new File(filesystemPathForURL(inputURL)).exists();
    }

    private static void copyResource(OpenForReadResult input, OutputStream outputStream) throws IOException {
        try {
            InputStream inputStream = input.inputStream;
            if (!(inputStream instanceof FileInputStream) || !(outputStream instanceof FileOutputStream)) {
                byte[] buffer = new byte[AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD];
                while (true) {
                    int bytesRead = inputStream.read(buffer, 0, AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                    if (bytesRead <= 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, bytesRead);
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
}
