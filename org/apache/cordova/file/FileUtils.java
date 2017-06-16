package org.apache.cordova.file;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.file.Filesystem.ReadFileCallback;
import org.apache.cordova.file.PendingRequests.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileUtils extends CordovaPlugin {
    public static int ABORT_ERR = 0;
    public static final int ACTION_GET_DIRECTORY = 2;
    public static final int ACTION_GET_FILE = 0;
    public static final int ACTION_WRITE = 1;
    public static int ENCODING_ERR = 0;
    public static int INVALID_MODIFICATION_ERR = 0;
    public static int INVALID_STATE_ERR = 0;
    private static final String LOG_TAG = "FileUtils";
    public static int NOT_FOUND_ERR = 0;
    public static int NOT_READABLE_ERR = 0;
    public static int NO_MODIFICATION_ALLOWED_ERR = 0;
    public static int PATH_EXISTS_ERR = 0;
    public static int QUOTA_EXCEEDED_ERR = 0;
    public static final int READ = 4;
    public static int SECURITY_ERR = 0;
    public static int SYNTAX_ERR = 0;
    public static int TYPE_MISMATCH_ERR = 0;
    public static int UNKNOWN_ERR = 0;
    public static final int WRITE = 3;
    private static FileUtils filePlugin;
    private boolean configured;
    private ArrayList<Filesystem> filesystems;
    private PendingRequests pendingRequests;
    private String[] permissions;

    /* renamed from: org.apache.cordova.file.FileUtils.12 */
    class AnonymousClass12 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass12(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            try {
                this.val$callbackContext.success(FileUtils.this.requestAllPaths());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.25 */
    class AnonymousClass25 implements Runnable {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ FileOp val$f;
        final /* synthetic */ String val$rawArgs;

        AnonymousClass25(String str, FileOp fileOp, CallbackContext callbackContext) {
            this.val$rawArgs = str;
            this.val$f = fileOp;
            this.val$callbackContext = callbackContext;
        }

        public void run() {
            try {
                this.val$f.run(new JSONArray(this.val$rawArgs));
            } catch (Exception e) {
                if (e instanceof EncodingException) {
                    this.val$callbackContext.error(FileUtils.ENCODING_ERR);
                } else if (e instanceof FileNotFoundException) {
                    this.val$callbackContext.error(FileUtils.NOT_FOUND_ERR);
                } else if (e instanceof FileExistsException) {
                    this.val$callbackContext.error(FileUtils.PATH_EXISTS_ERR);
                } else if (e instanceof NoModificationAllowedException) {
                    this.val$callbackContext.error(FileUtils.NO_MODIFICATION_ALLOWED_ERR);
                } else if (e instanceof InvalidModificationException) {
                    this.val$callbackContext.error(FileUtils.INVALID_MODIFICATION_ERR);
                } else if (e instanceof MalformedURLException) {
                    this.val$callbackContext.error(FileUtils.ENCODING_ERR);
                } else if (e instanceof IOException) {
                    this.val$callbackContext.error(FileUtils.INVALID_MODIFICATION_ERR);
                } else if (e instanceof EncodingException) {
                    this.val$callbackContext.error(FileUtils.ENCODING_ERR);
                } else if (e instanceof TypeMismatchException) {
                    this.val$callbackContext.error(FileUtils.TYPE_MISMATCH_ERR);
                } else if (e instanceof JSONException) {
                    this.val$callbackContext.sendPluginResult(new PluginResult(Status.JSON_EXCEPTION));
                } else if (e instanceof SecurityException) {
                    this.val$callbackContext.error(FileUtils.SECURITY_ERR);
                } else {
                    e.printStackTrace();
                    this.val$callbackContext.error(FileUtils.UNKNOWN_ERR);
                }
            }
        }
    }

    private interface FileOp {
        void run(JSONArray jSONArray) throws Exception;
    }

    /* renamed from: org.apache.cordova.file.FileUtils.10 */
    class AnonymousClass10 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass10(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, FileNotFoundException, IOException, NoModificationAllowedException {
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, (float) FileUtils.this.truncateFile(args.getString(FileUtils.ACTION_GET_FILE), (long) args.getInt(FileUtils.ACTION_WRITE))));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.11 */
    class AnonymousClass11 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass11(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws IOException, JSONException {
            this.val$callbackContext.success(FileUtils.this.requestAllFileSystems());
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.13 */
    class AnonymousClass13 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass13(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException {
            FileUtils.this.requestFileSystem(args.getInt(FileUtils.ACTION_GET_FILE), args.optLong(FileUtils.ACTION_WRITE), this.val$callbackContext);
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.14 */
    class AnonymousClass14 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass14(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws IOException, JSONException {
            this.val$callbackContext.success(FileUtils.this.resolveLocalFileSystemURI(args.getString(FileUtils.ACTION_GET_FILE)));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.15 */
    class AnonymousClass15 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass15(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws FileNotFoundException, JSONException, MalformedURLException {
            this.val$callbackContext.success(FileUtils.this.getFileMetadata(args.getString(FileUtils.ACTION_GET_FILE)));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.16 */
    class AnonymousClass16 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass16(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, IOException {
            this.val$callbackContext.success(FileUtils.this.getParent(args.getString(FileUtils.ACTION_GET_FILE)));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.17 */
    class AnonymousClass17 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ String val$rawArgs;

        AnonymousClass17(String str, CallbackContext callbackContext) {
            this.val$rawArgs = str;
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
            boolean containsCreate = false;
            String dirname = args.getString(FileUtils.ACTION_GET_FILE);
            String path = args.getString(FileUtils.ACTION_WRITE);
            String nativeURL = FileUtils.this.resolveLocalFileSystemURI(dirname).getString("nativeURL");
            if (!args.isNull(FileUtils.ACTION_GET_DIRECTORY)) {
                containsCreate = args.getJSONObject(FileUtils.ACTION_GET_DIRECTORY).optBoolean("create", false);
            }
            if (containsCreate && FileUtils.this.needPermission(nativeURL, FileUtils.WRITE)) {
                FileUtils.this.getWritePermission(this.val$rawArgs, FileUtils.ACTION_GET_DIRECTORY, this.val$callbackContext);
            } else if (containsCreate || !FileUtils.this.needPermission(nativeURL, FileUtils.READ)) {
                this.val$callbackContext.success(FileUtils.this.getFile(dirname, path, args.optJSONObject(FileUtils.ACTION_GET_DIRECTORY), true));
            } else {
                FileUtils.this.getReadPermission(this.val$rawArgs, FileUtils.ACTION_GET_DIRECTORY, this.val$callbackContext);
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.18 */
    class AnonymousClass18 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ String val$rawArgs;

        AnonymousClass18(String str, CallbackContext callbackContext) {
            this.val$rawArgs = str;
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
            String dirname = args.getString(FileUtils.ACTION_GET_FILE);
            String path = args.getString(FileUtils.ACTION_WRITE);
            String nativeURL = FileUtils.this.resolveLocalFileSystemURI(dirname).getString("nativeURL");
            boolean containsCreate = args.isNull(FileUtils.ACTION_GET_DIRECTORY) ? false : args.getJSONObject(FileUtils.ACTION_GET_DIRECTORY).optBoolean("create", false);
            if (containsCreate && FileUtils.this.needPermission(nativeURL, FileUtils.WRITE)) {
                FileUtils.this.getWritePermission(this.val$rawArgs, FileUtils.ACTION_GET_FILE, this.val$callbackContext);
            } else if (containsCreate || !FileUtils.this.needPermission(nativeURL, FileUtils.READ)) {
                this.val$callbackContext.success(FileUtils.this.getFile(dirname, path, args.optJSONObject(FileUtils.ACTION_GET_DIRECTORY), false));
            } else {
                FileUtils.this.getReadPermission(this.val$rawArgs, FileUtils.ACTION_GET_FILE, this.val$callbackContext);
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.19 */
    class AnonymousClass19 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass19(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, NoModificationAllowedException, InvalidModificationException, MalformedURLException {
            if (FileUtils.this.remove(args.getString(FileUtils.ACTION_GET_FILE))) {
                this.val$callbackContext.success();
            } else {
                this.val$callbackContext.error(FileUtils.NO_MODIFICATION_ALLOWED_ERR);
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.1 */
    class C03291 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03291(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) {
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, DirectoryManager.testSaveLocationExists()));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.20 */
    class AnonymousClass20 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass20(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, FileExistsException, MalformedURLException, NoModificationAllowedException {
            if (FileUtils.this.removeRecursively(args.getString(FileUtils.ACTION_GET_FILE))) {
                this.val$callbackContext.success();
            } else {
                this.val$callbackContext.error(FileUtils.NO_MODIFICATION_ALLOWED_ERR);
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.21 */
    class AnonymousClass21 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass21(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, NoModificationAllowedException, IOException, InvalidModificationException, EncodingException, FileExistsException {
            this.val$callbackContext.success(FileUtils.this.transferTo(args.getString(FileUtils.ACTION_GET_FILE), args.getString(FileUtils.ACTION_WRITE), args.getString(FileUtils.ACTION_GET_DIRECTORY), true));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.22 */
    class AnonymousClass22 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass22(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, NoModificationAllowedException, IOException, InvalidModificationException, EncodingException, FileExistsException {
            this.val$callbackContext.success(FileUtils.this.transferTo(args.getString(FileUtils.ACTION_GET_FILE), args.getString(FileUtils.ACTION_WRITE), args.getString(FileUtils.ACTION_GET_DIRECTORY), false));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.23 */
    class AnonymousClass23 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass23(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws FileNotFoundException, JSONException, MalformedURLException {
            this.val$callbackContext.success(FileUtils.this.readEntries(args.getString(FileUtils.ACTION_GET_FILE)));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.24 */
    class AnonymousClass24 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        AnonymousClass24(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws FileNotFoundException, JSONException, MalformedURLException {
            this.val$callbackContext.success(FileUtils.this.filesystemPathForURL(args.getString(FileUtils.ACTION_GET_FILE)));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.26 */
    class AnonymousClass26 implements ReadFileCallback {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ String val$encoding;
        final /* synthetic */ int val$resultType;

        AnonymousClass26(int i, String str, CallbackContext callbackContext) {
            this.val$resultType = i;
            this.val$encoding = str;
            this.val$callbackContext = callbackContext;
        }

        public void handleData(InputStream inputStream, String contentType) {
            try {
                PluginResult result;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD];
                while (true) {
                    int bytesRead = inputStream.read(buffer, FileUtils.ACTION_GET_FILE, AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                    if (bytesRead <= 0) {
                        break;
                    }
                    os.write(buffer, FileUtils.ACTION_GET_FILE, bytesRead);
                }
                switch (this.val$resultType) {
                    case FileUtils.ACTION_WRITE /*1*/:
                        result = new PluginResult(Status.OK, os.toString(this.val$encoding));
                        break;
                    case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                        result = new PluginResult(Status.OK, os.toByteArray());
                        break;
                    case PluginResult.MESSAGE_TYPE_BINARYSTRING /*7*/:
                        result = new PluginResult(Status.OK, os.toByteArray(), true);
                        break;
                    default:
                        result = new PluginResult(Status.OK, "data:" + contentType + ";base64," + new String(Base64.encode(os.toByteArray(), FileUtils.ACTION_GET_DIRECTORY), "US-ASCII"));
                        break;
                }
                this.val$callbackContext.sendPluginResult(result);
            } catch (IOException e) {
                LOG.m4d(FileUtils.LOG_TAG, e.getLocalizedMessage());
                this.val$callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, FileUtils.NOT_READABLE_ERR));
            }
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.27 */
    class AnonymousClass27 implements FileOp {
        final /* synthetic */ Request val$req;

        AnonymousClass27(Request request) {
            this.val$req = request;
        }

        public void run(JSONArray args) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
            this.val$req.getCallbackContext().success(FileUtils.this.getFile(args.getString(FileUtils.ACTION_GET_FILE), args.getString(FileUtils.ACTION_WRITE), args.optJSONObject(FileUtils.ACTION_GET_DIRECTORY), false));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.28 */
    class AnonymousClass28 implements FileOp {
        final /* synthetic */ Request val$req;

        AnonymousClass28(Request request) {
            this.val$req = request;
        }

        public void run(JSONArray args) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
            this.val$req.getCallbackContext().success(FileUtils.this.getFile(args.getString(FileUtils.ACTION_GET_FILE), args.getString(FileUtils.ACTION_WRITE), args.optJSONObject(FileUtils.ACTION_GET_DIRECTORY), true));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.29 */
    class AnonymousClass29 implements FileOp {
        final /* synthetic */ Request val$req;

        AnonymousClass29(Request request) {
            this.val$req = request;
        }

        public void run(JSONArray args) throws JSONException, FileNotFoundException, IOException, NoModificationAllowedException {
            this.val$req.getCallbackContext().sendPluginResult(new PluginResult(Status.OK, (float) FileUtils.this.write(args.getString(FileUtils.ACTION_GET_FILE), args.getString(FileUtils.ACTION_WRITE), args.getInt(FileUtils.ACTION_GET_DIRECTORY), Boolean.valueOf(args.getBoolean(FileUtils.WRITE)).booleanValue())));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.2 */
    class C03302 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03302(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) {
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, (float) DirectoryManager.getFreeExternalStorageSpace()));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.3 */
    class C03313 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03313(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException {
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, DirectoryManager.testFileExists(args.getString(FileUtils.ACTION_GET_FILE))));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.4 */
    class C03324 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03324(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException {
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, DirectoryManager.testFileExists(args.getString(FileUtils.ACTION_GET_FILE))));
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.5 */
    class C03335 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03335(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, MalformedURLException {
            String encoding = args.getString(FileUtils.ACTION_WRITE);
            int start = args.getInt(FileUtils.ACTION_GET_DIRECTORY);
            int end = args.getInt(FileUtils.WRITE);
            FileUtils.this.readFileAs(args.getString(FileUtils.ACTION_GET_FILE), start, end, this.val$callbackContext, encoding, FileUtils.ACTION_WRITE);
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.6 */
    class C03346 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03346(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, MalformedURLException {
            int start = args.getInt(FileUtils.ACTION_WRITE);
            int end = args.getInt(FileUtils.ACTION_GET_DIRECTORY);
            FileUtils.this.readFileAs(args.getString(FileUtils.ACTION_GET_FILE), start, end, this.val$callbackContext, null, -1);
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.7 */
    class C03357 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03357(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, MalformedURLException {
            int start = args.getInt(FileUtils.ACTION_WRITE);
            int end = args.getInt(FileUtils.ACTION_GET_DIRECTORY);
            FileUtils.this.readFileAs(args.getString(FileUtils.ACTION_GET_FILE), start, end, this.val$callbackContext, null, 6);
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.8 */
    class C03368 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;

        C03368(CallbackContext callbackContext) {
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, MalformedURLException {
            int start = args.getInt(FileUtils.ACTION_WRITE);
            int end = args.getInt(FileUtils.ACTION_GET_DIRECTORY);
            FileUtils.this.readFileAs(args.getString(FileUtils.ACTION_GET_FILE), start, end, this.val$callbackContext, null, 7);
        }
    }

    /* renamed from: org.apache.cordova.file.FileUtils.9 */
    class C03379 implements FileOp {
        final /* synthetic */ CallbackContext val$callbackContext;
        final /* synthetic */ String val$rawArgs;

        C03379(String str, CallbackContext callbackContext) {
            this.val$rawArgs = str;
            this.val$callbackContext = callbackContext;
        }

        public void run(JSONArray args) throws JSONException, FileNotFoundException, IOException, NoModificationAllowedException {
            String fname = args.getString(FileUtils.ACTION_GET_FILE);
            String nativeURL = FileUtils.this.resolveLocalFileSystemURI(fname).getString("nativeURL");
            String data = args.getString(FileUtils.ACTION_WRITE);
            int offset = args.getInt(FileUtils.ACTION_GET_DIRECTORY);
            Boolean isBinary = Boolean.valueOf(args.getBoolean(FileUtils.WRITE));
            if (FileUtils.this.needPermission(nativeURL, FileUtils.WRITE)) {
                FileUtils.this.getWritePermission(this.val$rawArgs, FileUtils.ACTION_WRITE, this.val$callbackContext);
                return;
            }
            this.val$callbackContext.sendPluginResult(new PluginResult(Status.OK, (float) FileUtils.this.write(fname, data, offset, isBinary.booleanValue())));
        }
    }

    public FileUtils() {
        this.configured = false;
        String[] strArr = new String[ACTION_GET_DIRECTORY];
        strArr[ACTION_GET_FILE] = "android.permission.READ_EXTERNAL_STORAGE";
        strArr[ACTION_WRITE] = "android.permission.WRITE_EXTERNAL_STORAGE";
        this.permissions = strArr;
    }

    static {
        NOT_FOUND_ERR = ACTION_WRITE;
        SECURITY_ERR = ACTION_GET_DIRECTORY;
        ABORT_ERR = WRITE;
        NOT_READABLE_ERR = READ;
        ENCODING_ERR = 5;
        NO_MODIFICATION_ALLOWED_ERR = 6;
        INVALID_STATE_ERR = 7;
        SYNTAX_ERR = 8;
        INVALID_MODIFICATION_ERR = 9;
        QUOTA_EXCEEDED_ERR = 10;
        TYPE_MISMATCH_ERR = 11;
        PATH_EXISTS_ERR = 12;
        UNKNOWN_ERR = PointerIconCompat.TYPE_DEFAULT;
    }

    public void registerFilesystem(Filesystem fs) {
        if (fs != null && filesystemForName(fs.name) == null) {
            this.filesystems.add(fs);
        }
    }

    private Filesystem filesystemForName(String name) {
        Iterator it = this.filesystems.iterator();
        while (it.hasNext()) {
            Filesystem fs = (Filesystem) it.next();
            if (fs != null && fs.name != null && fs.name.equals(name)) {
                return fs;
            }
        }
        return null;
    }

    protected String[] getExtraFileSystemsPreference(Activity activity) {
        return this.preferences.getString("androidextrafilesystems", "files,files-external,documents,sdcard,cache,cache-external,assets,root").split(",");
    }

    protected void registerExtraFileSystems(String[] filesystems, HashMap<String, String> availableFileSystems) {
        HashSet<String> installedFileSystems = new HashSet();
        int length = filesystems.length;
        for (int i = ACTION_GET_FILE; i < length; i += ACTION_WRITE) {
            String fsName = filesystems[i];
            if (!installedFileSystems.contains(fsName)) {
                String fsRoot = (String) availableFileSystems.get(fsName);
                if (fsRoot != null) {
                    File newRoot = new File(fsRoot);
                    if (newRoot.mkdirs() || newRoot.isDirectory()) {
                        registerFilesystem(new LocalFilesystem(fsName, this.webView.getContext(), this.webView.getResourceApi(), newRoot));
                        installedFileSystems.add(fsName);
                    } else {
                        LOG.m4d(LOG_TAG, "Unable to create root dir for filesystem \"" + fsName + "\", skipping");
                    }
                } else {
                    LOG.m4d(LOG_TAG, "Unrecognized extra filesystem identifier: " + fsName);
                }
            }
        }
    }

    protected HashMap<String, String> getAvailableFileSystems(Activity activity) {
        Context context = activity.getApplicationContext();
        HashMap<String, String> availableFileSystems = new HashMap();
        availableFileSystems.put("files", context.getFilesDir().getAbsolutePath());
        availableFileSystems.put("documents", new File(context.getFilesDir(), "Documents").getAbsolutePath());
        availableFileSystems.put("cache", context.getCacheDir().getAbsolutePath());
        availableFileSystems.put("root", "/");
        if (Environment.getExternalStorageState().equals("mounted")) {
            try {
                availableFileSystems.put("files-external", context.getExternalFilesDir(null).getAbsolutePath());
                availableFileSystems.put("sdcard", Environment.getExternalStorageDirectory().getAbsolutePath());
                availableFileSystems.put("cache-external", context.getExternalCacheDir().getAbsolutePath());
            } catch (NullPointerException e) {
                LOG.m4d(LOG_TAG, "External storage unavailable, check to see if USB Mass Storage Mode is on");
            }
        }
        return availableFileSystems;
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.filesystems = new ArrayList();
        this.pendingRequests = new PendingRequests();
        String persistentRoot = null;
        Activity activity = cordova.getActivity();
        String packageName = activity.getPackageName();
        String location = this.preferences.getString("androidpersistentfilelocation", "internal");
        String tempRoot = activity.getCacheDir().getAbsolutePath();
        if ("internal".equalsIgnoreCase(location)) {
            persistentRoot = activity.getFilesDir().getAbsolutePath() + "/files/";
            this.configured = true;
        } else if ("compatibility".equalsIgnoreCase(location)) {
            if (Environment.getExternalStorageState().equals("mounted")) {
                persistentRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
                tempRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + packageName + "/cache/";
            } else {
                persistentRoot = "/data/data/" + packageName;
            }
            this.configured = true;
        }
        if (this.configured) {
            File tmpRootFile = new File(tempRoot);
            File persistentRootFile = new File(persistentRoot);
            tmpRootFile.mkdirs();
            persistentRootFile.mkdirs();
            registerFilesystem(new LocalFilesystem("temporary", webView.getContext(), webView.getResourceApi(), tmpRootFile));
            registerFilesystem(new LocalFilesystem("persistent", webView.getContext(), webView.getResourceApi(), persistentRootFile));
            registerFilesystem(new ContentFilesystem(webView.getContext(), webView.getResourceApi()));
            registerFilesystem(new AssetFilesystem(webView.getContext().getAssets(), webView.getResourceApi()));
            registerExtraFileSystems(getExtraFileSystemsPreference(activity), getAvailableFileSystems(activity));
            if (filePlugin == null) {
                filePlugin = this;
                return;
            }
            return;
        }
        LOG.m7e(LOG_TAG, "File plugin configuration error: Please set AndroidPersistentFileLocation in config.xml to one of \"internal\" (for new applications) or \"compatibility\" (for compatibility with previous versions)");
        activity.finish();
    }

    public static FileUtils getFilePlugin() {
        return filePlugin;
    }

    private Filesystem filesystemForURL(LocalFilesystemURL localURL) {
        if (localURL == null) {
            return null;
        }
        return filesystemForName(localURL.fsName);
    }

    public Uri remapUri(Uri uri) {
        Uri uri2 = null;
        if (LocalFilesystemURL.FILESYSTEM_PROTOCOL.equals(uri.getScheme())) {
            try {
                LocalFilesystemURL inputURL = LocalFilesystemURL.parse(uri);
                Filesystem fs = filesystemForURL(inputURL);
                if (!(fs == null || fs.filesystemPathForURL(inputURL) == null)) {
                    uri2 = Uri.parse("file://" + fs.filesystemPathForURL(inputURL));
                }
            } catch (IllegalArgumentException e) {
            }
        }
        return uri2;
    }

    public boolean execute(String action, String rawArgs, CallbackContext callbackContext) {
        if (!this.configured) {
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, "File plugin is not configured. Please see the README.md file for details on how to update config.xml"));
            return true;
        } else if (action.equals("testSaveLocationExists")) {
            threadhelper(new C03291(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("getFreeDiskSpace")) {
            threadhelper(new C03302(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("testFileExists")) {
            threadhelper(new C03313(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("testDirectoryExists")) {
            threadhelper(new C03324(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("readAsText")) {
            threadhelper(new C03335(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("readAsDataURL")) {
            threadhelper(new C03346(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("readAsArrayBuffer")) {
            threadhelper(new C03357(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("readAsBinaryString")) {
            threadhelper(new C03368(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("write")) {
            threadhelper(new C03379(rawArgs, callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("truncate")) {
            threadhelper(new AnonymousClass10(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("requestAllFileSystems")) {
            threadhelper(new AnonymousClass11(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("requestAllPaths")) {
            this.cordova.getThreadPool().execute(new AnonymousClass12(callbackContext));
            return true;
        } else if (action.equals("requestFileSystem")) {
            threadhelper(new AnonymousClass13(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("resolveLocalFileSystemURI")) {
            threadhelper(new AnonymousClass14(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("getFileMetadata")) {
            threadhelper(new AnonymousClass15(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("getParent")) {
            threadhelper(new AnonymousClass16(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("getDirectory")) {
            threadhelper(new AnonymousClass17(rawArgs, callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("getFile")) {
            threadhelper(new AnonymousClass18(rawArgs, callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("remove")) {
            threadhelper(new AnonymousClass19(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("removeRecursively")) {
            threadhelper(new AnonymousClass20(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("moveTo")) {
            threadhelper(new AnonymousClass21(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("copyTo")) {
            threadhelper(new AnonymousClass22(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (action.equals("readEntries")) {
            threadhelper(new AnonymousClass23(callbackContext), rawArgs, callbackContext);
            return true;
        } else if (!action.equals("_getLocalFilesystemPath")) {
            return false;
        } else {
            threadhelper(new AnonymousClass24(callbackContext), rawArgs, callbackContext);
            return true;
        }
    }

    private void getReadPermission(String rawArgs, int action, CallbackContext callbackContext) {
        PermissionHelper.requestPermission(this, this.pendingRequests.createRequest(rawArgs, action, callbackContext), "android.permission.READ_EXTERNAL_STORAGE");
    }

    private void getWritePermission(String rawArgs, int action, CallbackContext callbackContext) {
        PermissionHelper.requestPermission(this, this.pendingRequests.createRequest(rawArgs, action, callbackContext), "android.permission.WRITE_EXTERNAL_STORAGE");
    }

    private boolean hasReadPermission() {
        return PermissionHelper.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE");
    }

    private boolean hasWritePermission() {
        return PermissionHelper.hasPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
    }

    private boolean needPermission(String nativeURL, int permissionType) throws JSONException {
        JSONObject j = requestAllPaths();
        ArrayList<String> allowedStorageDirectories = new ArrayList();
        allowedStorageDirectories.add(j.getString("applicationStorageDirectory"));
        if (j.has("externalApplicationStorageDirectory")) {
            allowedStorageDirectories.add(j.getString("externalApplicationStorageDirectory"));
        }
        if (permissionType == READ && hasReadPermission()) {
            return false;
        }
        if (permissionType == WRITE && hasWritePermission()) {
            return false;
        }
        Iterator it = allowedStorageDirectories.iterator();
        while (it.hasNext()) {
            if (nativeURL.startsWith((String) it.next())) {
                return false;
            }
        }
        return true;
    }

    public LocalFilesystemURL resolveNativeUri(Uri nativeUri) {
        LocalFilesystemURL localURL = null;
        Iterator it = this.filesystems.iterator();
        while (it.hasNext()) {
            LocalFilesystemURL url = ((Filesystem) it.next()).toLocalUri(nativeUri);
            if (url != null && (localURL == null || url.uri.toString().length() < localURL.toString().length())) {
                localURL = url;
            }
        }
        return localURL;
    }

    public String filesystemPathForURL(String localURLstr) throws MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(localURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.filesystemPathForURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    public LocalFilesystemURL filesystemURLforLocalPath(String localPath) {
        LocalFilesystemURL localURL = null;
        int shortestFullPath = ACTION_GET_FILE;
        Iterator it = this.filesystems.iterator();
        while (it.hasNext()) {
            LocalFilesystemURL url = ((Filesystem) it.next()).URLforFilesystemPath(localPath);
            if (url != null && (localURL == null || url.path.length() < shortestFullPath)) {
                localURL = url;
                shortestFullPath = url.path.length();
            }
        }
        return localURL;
    }

    private void threadhelper(FileOp f, String rawArgs, CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new AnonymousClass25(rawArgs, f, callbackContext));
    }

    private JSONObject resolveLocalFileSystemURI(String uriString) throws IOException, JSONException {
        if (uriString == null) {
            throw new MalformedURLException("Unrecognized filesystem URL");
        }
        Uri uri = Uri.parse(uriString);
        boolean isNativeUri = false;
        LocalFilesystemURL inputURL = LocalFilesystemURL.parse(uri);
        if (inputURL == null) {
            inputURL = resolveNativeUri(uri);
            isNativeUri = true;
        }
        try {
            Filesystem fs = filesystemForURL(inputURL);
            if (fs == null) {
                throw new MalformedURLException("No installed handlers for this URL");
            } else if (fs.exists(inputURL)) {
                if (!isNativeUri) {
                    inputURL = fs.toLocalUri(fs.toNativeUri(inputURL));
                }
                return fs.getEntryForLocalURL(inputURL);
            } else {
                throw new FileNotFoundException();
            }
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private JSONArray readEntries(String baseURLstr) throws FileNotFoundException, JSONException, MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.readEntriesAtLocalURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private JSONObject transferTo(String srcURLstr, String destURLstr, String newName, boolean move) throws JSONException, NoModificationAllowedException, IOException, InvalidModificationException, EncodingException, FileExistsException {
        if (srcURLstr == null || destURLstr == null) {
            throw new FileNotFoundException();
        }
        LocalFilesystemURL srcURL = LocalFilesystemURL.parse(srcURLstr);
        LocalFilesystemURL destURL = LocalFilesystemURL.parse(destURLstr);
        Filesystem srcFs = filesystemForURL(srcURL);
        Filesystem destFs = filesystemForURL(destURL);
        if (newName == null || !newName.contains(":")) {
            return destFs.copyFileToURL(destURL, newName, srcFs, srcURL, move);
        }
        throw new EncodingException("Bad file name");
    }

    private boolean removeRecursively(String baseURLstr) throws FileExistsException, NoModificationAllowedException, MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            if (BuildConfig.FLAVOR.equals(inputURL.path) || "/".equals(inputURL.path)) {
                throw new NoModificationAllowedException("You can't delete the root directory");
            }
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.recursiveRemoveFileAtLocalURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private boolean remove(String baseURLstr) throws NoModificationAllowedException, InvalidModificationException, MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            if (BuildConfig.FLAVOR.equals(inputURL.path) || "/".equals(inputURL.path)) {
                throw new NoModificationAllowedException("You can't delete the root directory");
            }
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.removeFileAtLocalURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private JSONObject getFile(String baseURLstr, String path, JSONObject options, boolean directory) throws FileExistsException, IOException, TypeMismatchException, EncodingException, JSONException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.getFileForLocalURL(inputURL, path, options, directory);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private JSONObject getParent(String baseURLstr) throws JSONException, IOException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.getParentForLocalURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private JSONObject getFileMetadata(String baseURLstr) throws FileNotFoundException, JSONException, MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(baseURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.getFileMetadataForLocalURL(inputURL);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private void requestFileSystem(int type, long requiredSize, CallbackContext callbackContext) throws JSONException {
        Filesystem rootFs = null;
        try {
            rootFs = (Filesystem) this.filesystems.get(type);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        if (rootFs == null) {
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, NOT_FOUND_ERR));
            return;
        }
        long availableSize = 0;
        if (requiredSize > 0) {
            availableSize = rootFs.getFreeSpaceInBytes();
        }
        if (availableSize < requiredSize) {
            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, QUOTA_EXCEEDED_ERR));
            return;
        }
        JSONObject fs = new JSONObject();
        fs.put("name", rootFs.name);
        fs.put("root", rootFs.getRootEntry());
        callbackContext.success(fs);
    }

    private JSONArray requestAllFileSystems() throws IOException, JSONException {
        JSONArray ret = new JSONArray();
        Iterator it = this.filesystems.iterator();
        while (it.hasNext()) {
            ret.put(((Filesystem) it.next()).getRootEntry());
        }
        return ret;
    }

    private static String toDirUrl(File f) {
        return Uri.fromFile(f).toString() + '/';
    }

    private JSONObject requestAllPaths() throws JSONException {
        Context context = this.cordova.getActivity();
        JSONObject ret = new JSONObject();
        ret.put("applicationDirectory", "file:///android_asset/");
        ret.put("applicationStorageDirectory", toDirUrl(context.getFilesDir().getParentFile()));
        ret.put("dataDirectory", toDirUrl(context.getFilesDir()));
        ret.put("cacheDirectory", toDirUrl(context.getCacheDir()));
        if (Environment.getExternalStorageState().equals("mounted")) {
            try {
                ret.put("externalApplicationStorageDirectory", toDirUrl(context.getExternalFilesDir(null).getParentFile()));
                ret.put("externalDataDirectory", toDirUrl(context.getExternalFilesDir(null)));
                ret.put("externalCacheDirectory", toDirUrl(context.getExternalCacheDir()));
                ret.put("externalRootDirectory", toDirUrl(Environment.getExternalStorageDirectory()));
            } catch (NullPointerException e) {
                LOG.m4d(LOG_TAG, "Unable to access these paths, most liklely due to USB storage");
            }
        }
        return ret;
    }

    public JSONObject getEntryForFile(File file) throws JSONException {
        Iterator it = this.filesystems.iterator();
        while (it.hasNext()) {
            JSONObject entry = ((Filesystem) it.next()).makeEntryForFile(file);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }

    @Deprecated
    public static JSONObject getEntry(File file) throws JSONException {
        if (getFilePlugin() != null) {
            return getFilePlugin().getEntryForFile(file);
        }
        return null;
    }

    public void readFileAs(String srcURLstr, int start, int end, CallbackContext callbackContext, String encoding, int resultType) throws MalformedURLException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(srcURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs == null) {
                throw new MalformedURLException("No installed handlers for this URL");
            }
            fs.readFileAtURL(inputURL, (long) start, (long) end, new AnonymousClass26(resultType, encoding, callbackContext));
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        } catch (FileNotFoundException e2) {
            callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, NOT_FOUND_ERR));
        } catch (IOException e3) {
            LOG.m4d(LOG_TAG, e3.getLocalizedMessage());
            callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, NOT_READABLE_ERR));
        }
    }

    public long write(String srcURLstr, String data, int offset, boolean isBinary) throws FileNotFoundException, IOException, NoModificationAllowedException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(srcURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs == null) {
                throw new MalformedURLException("No installed handlers for this URL");
            }
            long x = fs.writeToFileAtURL(inputURL, data, offset, isBinary);
            LOG.m4d("TEST", srcURLstr + ": " + x);
            return x;
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    private long truncateFile(String srcURLstr, long size) throws FileNotFoundException, IOException, NoModificationAllowedException {
        try {
            LocalFilesystemURL inputURL = LocalFilesystemURL.parse(srcURLstr);
            Filesystem fs = filesystemForURL(inputURL);
            if (fs != null) {
                return fs.truncateFileAtURL(inputURL, size);
            }
            throw new MalformedURLException("No installed handlers for this URL");
        } catch (IllegalArgumentException e) {
            MalformedURLException mue = new MalformedURLException("Unrecognized filesystem URL");
            mue.initCause(e);
            throw mue;
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        Request req = this.pendingRequests.getAndRemove(requestCode);
        if (req != null) {
            int length = grantResults.length;
            for (int i = ACTION_GET_FILE; i < length; i += ACTION_WRITE) {
                if (grantResults[i] == -1) {
                    req.getCallbackContext().sendPluginResult(new PluginResult(Status.ERROR, SECURITY_ERR));
                    return;
                }
            }
            switch (req.getAction()) {
                case ACTION_GET_FILE /*0*/:
                    threadhelper(new AnonymousClass27(req), req.getRawArgs(), req.getCallbackContext());
                    return;
                case ACTION_WRITE /*1*/:
                    threadhelper(new AnonymousClass29(req), req.getRawArgs(), req.getCallbackContext());
                    return;
                case ACTION_GET_DIRECTORY /*2*/:
                    threadhelper(new AnonymousClass28(req), req.getRawArgs(), req.getCallbackContext());
                    return;
                default:
                    return;
            }
        }
        LOG.m4d(LOG_TAG, "Received permission callback for unknown request code");
    }
}
