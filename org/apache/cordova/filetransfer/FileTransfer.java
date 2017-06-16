package org.apache.cordova.filetransfer;

import android.net.Uri;
import android.webkit.CookieManager;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.cordova.BuildConfig;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.cordova.Whitelist;
import org.apache.cordova.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileTransfer extends CordovaPlugin {
    public static int ABORTED_ERR = 0;
    private static final String BOUNDARY = "+++++";
    public static int CONNECTION_ERR = 0;
    private static final HostnameVerifier DO_NOT_VERIFY;
    public static int FILE_NOT_FOUND_ERR = 0;
    public static int INVALID_URL_ERR = 0;
    private static final String LINE_END = "\r\n";
    private static final String LINE_START = "--";
    private static final String LOG_TAG = "FileTransfer";
    private static final int MAX_BUFFER_SIZE = 16384;
    public static int NOT_MODIFIED_ERR;
    private static HashMap<String, RequestContext> activeRequests;
    private static final TrustManager[] trustAllCerts;

    /* renamed from: org.apache.cordova.filetransfer.FileTransfer.1 */
    class C02351 implements Runnable {
        final /* synthetic */ boolean val$chunkedMode;
        final /* synthetic */ RequestContext val$context;
        final /* synthetic */ String val$fileKey;
        final /* synthetic */ String val$fileName;
        final /* synthetic */ JSONObject val$headers;
        final /* synthetic */ String val$httpMethod;
        final /* synthetic */ String val$mimeType;
        final /* synthetic */ String val$objectId;
        final /* synthetic */ JSONObject val$params;
        final /* synthetic */ CordovaResourceApi val$resourceApi;
        final /* synthetic */ String val$source;
        final /* synthetic */ String val$target;
        final /* synthetic */ Uri val$targetUri;
        final /* synthetic */ boolean val$trustEveryone;
        final /* synthetic */ boolean val$useHttps;

        C02351(RequestContext requestContext, String str, CordovaResourceApi cordovaResourceApi, Uri uri, boolean z, boolean z2, String str2, JSONObject jSONObject, String str3, JSONObject jSONObject2, String str4, String str5, String str6, boolean z3, String str7) {
            this.val$context = requestContext;
            this.val$source = str;
            this.val$resourceApi = cordovaResourceApi;
            this.val$targetUri = uri;
            this.val$useHttps = z;
            this.val$trustEveryone = z2;
            this.val$httpMethod = str2;
            this.val$headers = jSONObject;
            this.val$target = str3;
            this.val$params = jSONObject2;
            this.val$fileKey = str4;
            this.val$fileName = str5;
            this.val$mimeType = str6;
            this.val$chunkedMode = z3;
            this.val$objectId = str7;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
            r44 = this;
            r0 = r44;
            r0 = r0.val$context;
            r39 = r0;
            r0 = r39;
            r0 = r0.aborted;
            r39 = r0;
            if (r39 == 0) goto L_0x000f;
        L_0x000e:
            return;
        L_0x000f:
            r0 = r44;
            r0 = r0.val$source;
            r39 = r0;
            r36 = android.net.Uri.parse(r39);
            r0 = r44;
            r0 = r0.val$resourceApi;
            r39 = r0;
            r40 = r36.getScheme();
            if (r40 == 0) goto L_0x02d1;
        L_0x0025:
            r0 = r39;
            r1 = r36;
            r32 = r0.remapUri(r1);
            r10 = 0;
            r20 = 0;
            r21 = 0;
            r37 = 0;
            r14 = -1;
            r30 = new org.apache.cordova.filetransfer.FileUploadResult;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r30.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r23 = new org.apache.cordova.filetransfer.FileProgressResult;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r23.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$resourceApi;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r44;
            r0 = r0.val$targetUri;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r10 = r39.createHttpConnection(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$useHttps;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 == 0) goto L_0x0074;
        L_0x0057:
            r0 = r44;
            r0 = r0.val$trustEveryone;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 == 0) goto L_0x0074;
        L_0x005f:
            r0 = r10;
            r0 = (javax.net.ssl.HttpsURLConnection) r0;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r15 = r0;
            r21 = org.apache.cordova.filetransfer.FileTransfer.trustAllHosts(r15);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r20 = r15.getHostnameVerifier();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = org.apache.cordova.filetransfer.FileTransfer.DO_NOT_VERIFY;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r39;
            r15.setHostnameVerifier(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0074:
            r39 = 1;
            r0 = r39;
            r10.setDoInput(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = 1;
            r0 = r39;
            r10.setDoOutput(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = 0;
            r0 = r39;
            r10.setUseCaches(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$httpMethod;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r39;
            r10.setRequestMethod(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$headers;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 == 0) goto L_0x00aa;
        L_0x009c:
            r0 = r44;
            r0 = r0.val$headers;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r40 = "Content-Type";
            r39 = r39.has(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            if (r39 != 0) goto L_0x02e2;
        L_0x00aa:
            r19 = 1;
        L_0x00ac:
            if (r19 == 0) goto L_0x00b9;
        L_0x00ae:
            r39 = "Content-Type";
            r40 = "multipart/form-data; boundary=+++++";
            r0 = r39;
            r1 = r40;
            r10.setRequestProperty(r0, r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x00b9:
            r0 = r44;
            r0 = org.apache.cordova.filetransfer.FileTransfer.this;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r44;
            r0 = r0.val$target;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r11 = r39.getCookies(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            if (r11 == 0) goto L_0x00d2;
        L_0x00cb:
            r39 = "Cookie";
            r0 = r39;
            r10.setRequestProperty(r0, r11);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x00d2:
            r0 = r44;
            r0 = r0.val$headers;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 == 0) goto L_0x00e5;
        L_0x00da:
            r0 = r44;
            r0 = r0.val$headers;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r39;
            org.apache.cordova.filetransfer.FileTransfer.addHeadersToRequest(r10, r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x00e5:
            r4 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r4.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$params;	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = r0;
            r17 = r39.keys();	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
        L_0x00f4:
            r39 = r17.hasNext();	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            if (r39 == 0) goto L_0x0168;
        L_0x00fa:
            r18 = r17.next();	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = java.lang.String.valueOf(r18);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = "headers";
            r39 = r39.equals(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            if (r39 != 0) goto L_0x00f4;
        L_0x010a:
            r39 = "--";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = "+++++";
            r39 = r39.append(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39.append(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = "Content-Disposition: form-data; name=\"";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = r18.toString();	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = r39.append(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = 34;
            r39.append(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = "\r\n";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39.append(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$params;	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = r0;
            r40 = r18.toString();	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = r39.getString(r40);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r0 = r39;
            r4.append(r0);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            r39 = "\r\n";
            r0 = r39;
            r4.append(r0);	 Catch:{ JSONException -> 0x015a, FileNotFoundException -> 0x0302, IOException -> 0x0473, Throwable -> 0x0678 }
            goto L_0x00f4;
        L_0x015a:
            r12 = move-exception;
            r39 = "FileTransfer";
            r40 = r12.getMessage();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r39;
            r1 = r40;
            org.apache.cordova.LOG.m8e(r0, r1, r12);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0168:
            r39 = "--";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "+++++";
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "Content-Disposition: form-data; name=\"";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$fileKey;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "\";";
            r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = " filename=\"";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$fileName;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = 34;
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "Content-Type: ";
            r0 = r39;
            r39 = r4.append(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$mimeType;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39 = r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "\r\n";
            r39.append(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r4.toString();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = "UTF-8";
            r5 = r39.getBytes(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "\r\n--+++++--\r\n";
            r40 = "UTF-8";
            r35 = r39.getBytes(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$resourceApi;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r39;
            r1 = r32;
            r27 = r0.openForRead(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r5.length;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r0 = r35;
            r0 = r0.length;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r33 = r39 + r40;
            r0 = r27;
            r0 = r0.length;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r42 = 0;
            r39 = (r40 > r42 ? 1 : (r40 == r42 ? 0 : -1));
            if (r39 < 0) goto L_0x0222;
        L_0x0202:
            r0 = r27;
            r0 = r0.length;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r0 = r40;
            r14 = (int) r0;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            if (r19 == 0) goto L_0x020f;
        L_0x020d:
            r14 = r14 + r33;
        L_0x020f:
            r39 = 1;
            r0 = r23;
            r1 = r39;
            r0.setLengthComputable(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = (long) r14;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            r0 = r23;
            r1 = r40;
            r0.setTotal(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0222:
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = "Content Length: ";
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r40;
            r40 = r0.append(r14);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r40.toString();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$chunkedMode;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 != 0) goto L_0x024e;
        L_0x0244:
            r39 = android.os.Build.VERSION.SDK_INT;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = 8;
            r0 = r39;
            r1 = r40;
            if (r0 >= r1) goto L_0x02e6;
        L_0x024e:
            r38 = 1;
        L_0x0250:
            if (r38 != 0) goto L_0x0258;
        L_0x0252:
            r39 = -1;
            r0 = r39;
            if (r14 != r0) goto L_0x02ea;
        L_0x0258:
            r38 = 1;
        L_0x025a:
            if (r38 == 0) goto L_0x02ee;
        L_0x025c:
            r39 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
            r0 = r39;
            r10.setChunkedStreamingMode(r0);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "Transfer-Encoding";
            r40 = "chunked";
            r0 = r39;
            r1 = r40;
            r10.setRequestProperty(r0, r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x026e:
            r10.connect();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r31 = 0;
            r31 = r10.getOutputStream();	 Catch:{ all -> 0x0465 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0465 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ all -> 0x0465 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0513 }
            r39 = r0;
            r0 = r39;
            r0 = r0.aborted;	 Catch:{ all -> 0x0513 }
            r39 = r0;
            if (r39 == 0) goto L_0x0377;
        L_0x028c:
            monitor-exit(r40);	 Catch:{ all -> 0x0513 }
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r39);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r31);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x0374 }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x0374 }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x0374 }
            monitor-exit(r40);	 Catch:{ all -> 0x0374 }
            if (r10 == 0) goto L_0x000e;
        L_0x02b2:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x02ba:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x02c2:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x02d1:
            r40 = new java.io.File;
            r0 = r44;
            r0 = r0.val$source;
            r41 = r0;
            r40.<init>(r41);
            r36 = android.net.Uri.fromFile(r40);
            goto L_0x0025;
        L_0x02e2:
            r19 = 0;
            goto L_0x00ac;
        L_0x02e6:
            r38 = 0;
            goto L_0x0250;
        L_0x02ea:
            r38 = 0;
            goto L_0x025a;
        L_0x02ee:
            r10.setFixedLengthStreamingMode(r14);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$useHttps;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            if (r39 == 0) goto L_0x026e;
        L_0x02f9:
            r39 = "FileTransfer";
            r40 = "setFixedLengthStreamingMode could cause OutOfMemoryException - switch to chunkedMode=true to avoid it if this is an issue.";
            org.apache.cordova.LOG.m16w(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            goto L_0x026e;
        L_0x0302:
            r12 = move-exception;
            r39 = org.apache.cordova.filetransfer.FileTransfer.FILE_NOT_FOUND_ERR;	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$source;	 Catch:{ all -> 0x0743 }
            r40 = r0;
            r0 = r44;
            r0 = r0.val$target;	 Catch:{ all -> 0x0743 }
            r41 = r0;
            r0 = r39;
            r1 = r40;
            r2 = r41;
            r13 = org.apache.cordova.filetransfer.FileTransfer.createFileTransferError(r0, r1, r2, r10, r12);	 Catch:{ all -> 0x0743 }
            r39 = "FileTransfer";
            r40 = r13.toString();	 Catch:{ all -> 0x0743 }
            r0 = r39;
            r1 = r40;
            org.apache.cordova.LOG.m8e(r0, r1, r12);	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0743 }
            r39 = r0;
            r40 = new org.apache.cordova.PluginResult;	 Catch:{ all -> 0x0743 }
            r41 = org.apache.cordova.PluginResult.Status.IO_EXCEPTION;	 Catch:{ all -> 0x0743 }
            r0 = r40;
            r1 = r41;
            r0.<init>(r1, r13);	 Catch:{ all -> 0x0743 }
            r39.sendPluginResult(r40);	 Catch:{ all -> 0x0743 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x0822 }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x0822 }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x0822 }
            monitor-exit(r40);	 Catch:{ all -> 0x0822 }
            if (r10 == 0) goto L_0x000e;
        L_0x0355:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x035d:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x0365:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x0374:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0374 }
            throw r39;
        L_0x0377:
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0513 }
            r39 = r0;
            r0 = r39;
            r0.connection = r10;	 Catch:{ all -> 0x0513 }
            monitor-exit(r40);	 Catch:{ all -> 0x0513 }
            if (r19 == 0) goto L_0x038e;
        L_0x0384:
            r0 = r31;
            r0.write(r5);	 Catch:{ all -> 0x0465 }
            r0 = r5.length;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r37 = r37 + r39;
        L_0x038e:
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r8 = r39.available();	 Catch:{ all -> 0x0465 }
            r39 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
            r0 = r39;
            r7 = java.lang.Math.min(r8, r0);	 Catch:{ all -> 0x0465 }
            r6 = new byte[r7];	 Catch:{ all -> 0x0465 }
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r40 = 0;
            r0 = r39;
            r1 = r40;
            r9 = r0.read(r6, r1, r7);	 Catch:{ all -> 0x0465 }
            r24 = 0;
        L_0x03b4:
            if (r9 <= 0) goto L_0x0516;
        L_0x03b6:
            r37 = r37 + r9;
            r0 = r37;
            r0 = (long) r0;	 Catch:{ all -> 0x0465 }
            r40 = r0;
            r0 = r30;
            r1 = r40;
            r0.setBytesSent(r1);	 Catch:{ all -> 0x0465 }
            r39 = 0;
            r0 = r31;
            r1 = r39;
            r0.write(r6, r1, r9);	 Catch:{ all -> 0x0465 }
            r0 = r37;
            r0 = (long) r0;	 Catch:{ all -> 0x0465 }
            r40 = r0;
            r42 = 102400; // 0x19000 float:1.43493E-40 double:5.05923E-319;
            r42 = r42 + r24;
            r39 = (r40 > r42 ? 1 : (r40 == r42 ? 0 : -1));
            if (r39 <= 0) goto L_0x040e;
        L_0x03db:
            r0 = r37;
            r0 = (long) r0;	 Catch:{ all -> 0x0465 }
            r24 = r0;
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0465 }
            r40.<init>();	 Catch:{ all -> 0x0465 }
            r41 = "Uploaded ";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0465 }
            r0 = r40;
            r1 = r37;
            r40 = r0.append(r1);	 Catch:{ all -> 0x0465 }
            r41 = " of ";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0465 }
            r0 = r40;
            r40 = r0.append(r14);	 Catch:{ all -> 0x0465 }
            r41 = " bytes";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0465 }
            r40 = r40.toString();	 Catch:{ all -> 0x0465 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ all -> 0x0465 }
        L_0x040e:
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r8 = r39.available();	 Catch:{ all -> 0x0465 }
            r39 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
            r0 = r39;
            r7 = java.lang.Math.min(r8, r0);	 Catch:{ all -> 0x0465 }
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r40 = 0;
            r0 = r39;
            r1 = r40;
            r9 = r0.read(r6, r1, r7);	 Catch:{ all -> 0x0465 }
            r0 = r37;
            r0 = (long) r0;	 Catch:{ all -> 0x0465 }
            r40 = r0;
            r0 = r23;
            r1 = r40;
            r0.setLoaded(r1);	 Catch:{ all -> 0x0465 }
            r26 = new org.apache.cordova.PluginResult;	 Catch:{ all -> 0x0465 }
            r39 = org.apache.cordova.PluginResult.Status.OK;	 Catch:{ all -> 0x0465 }
            r40 = r23.toJSONObject();	 Catch:{ all -> 0x0465 }
            r0 = r26;
            r1 = r39;
            r2 = r40;
            r0.<init>(r1, r2);	 Catch:{ all -> 0x0465 }
            r39 = 1;
            r0 = r26;
            r1 = r39;
            r0.setKeepCallback(r1);	 Catch:{ all -> 0x0465 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r0 = r39;
            r1 = r26;
            r0.sendPluginResult(r1);	 Catch:{ all -> 0x0465 }
            goto L_0x03b4;
        L_0x0465:
            r39 = move-exception;
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r31);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0473:
            r12 = move-exception;
            r39 = org.apache.cordova.filetransfer.FileTransfer.CONNECTION_ERR;	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$source;	 Catch:{ all -> 0x0743 }
            r40 = r0;
            r0 = r44;
            r0 = r0.val$target;	 Catch:{ all -> 0x0743 }
            r41 = r0;
            r0 = r39;
            r1 = r40;
            r2 = r41;
            r13 = org.apache.cordova.filetransfer.FileTransfer.createFileTransferError(r0, r1, r2, r10, r12);	 Catch:{ all -> 0x0743 }
            r39 = "FileTransfer";
            r40 = r13.toString();	 Catch:{ all -> 0x0743 }
            r0 = r39;
            r1 = r40;
            org.apache.cordova.LOG.m8e(r0, r1, r12);	 Catch:{ all -> 0x0743 }
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0743 }
            r40.<init>();	 Catch:{ all -> 0x0743 }
            r41 = "Failed after uploading ";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0743 }
            r0 = r40;
            r1 = r37;
            r40 = r0.append(r1);	 Catch:{ all -> 0x0743 }
            r41 = " of ";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0743 }
            r0 = r40;
            r40 = r0.append(r14);	 Catch:{ all -> 0x0743 }
            r41 = " bytes.";
            r40 = r40.append(r41);	 Catch:{ all -> 0x0743 }
            r40 = r40.toString();	 Catch:{ all -> 0x0743 }
            org.apache.cordova.LOG.m7e(r39, r40);	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0743 }
            r39 = r0;
            r40 = new org.apache.cordova.PluginResult;	 Catch:{ all -> 0x0743 }
            r41 = org.apache.cordova.PluginResult.Status.IO_EXCEPTION;	 Catch:{ all -> 0x0743 }
            r0 = r40;
            r1 = r41;
            r0.<init>(r1, r13);	 Catch:{ all -> 0x0743 }
            r39.sendPluginResult(r40);	 Catch:{ all -> 0x0743 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x0825 }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x0825 }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x0825 }
            monitor-exit(r40);	 Catch:{ all -> 0x0825 }
            if (r10 == 0) goto L_0x000e;
        L_0x04f4:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x04fc:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x0504:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x0513:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0513 }
            throw r39;	 Catch:{ all -> 0x0465 }
        L_0x0516:
            if (r19 == 0) goto L_0x0526;
        L_0x0518:
            r0 = r31;
            r1 = r35;
            r0.write(r1);	 Catch:{ all -> 0x0465 }
            r0 = r35;
            r0 = r0.length;	 Catch:{ all -> 0x0465 }
            r39 = r0;
            r37 = r37 + r39;
        L_0x0526:
            r31.flush();	 Catch:{ all -> 0x0465 }
            r0 = r27;
            r0 = r0.inputStream;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r39);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r31);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x061c }
            r39 = r0;
            r41 = 0;
            r0 = r41;
            r1 = r39;
            r1.connection = r0;	 Catch:{ all -> 0x061c }
            monitor-exit(r40);	 Catch:{ all -> 0x061c }
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = "Sent ";
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r40;
            r1 = r37;
            r40 = r0.append(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = " of ";
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r40;
            r40 = r0.append(r14);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r40.toString();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r28 = r10.getResponseCode();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = "response code: ";
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r40;
            r1 = r28;
            r40 = r0.append(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r40.toString();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "FileTransfer";
            r40 = new java.lang.StringBuilder;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40.<init>();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = "response headers: ";
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = r10.getHeaderFields();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r40.append(r41);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r40.toString();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r16 = 0;
            r16 = org.apache.cordova.filetransfer.FileTransfer.getInputStream(r10);	 Catch:{ all -> 0x0728 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0728 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ all -> 0x0728 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0777 }
            r39 = r0;
            r0 = r39;
            r0 = r0.aborted;	 Catch:{ all -> 0x0777 }
            r39 = r0;
            if (r39 == 0) goto L_0x06f1;
        L_0x05ca:
            monitor-exit(r40);	 Catch:{ all -> 0x0777 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0675 }
            r39 = r0;
            r41 = 0;
            r0 = r41;
            r1 = r39;
            r1.connection = r0;	 Catch:{ all -> 0x0675 }
            monitor-exit(r40);	 Catch:{ all -> 0x0675 }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r16);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x06ee }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x06ee }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x06ee }
            monitor-exit(r40);	 Catch:{ all -> 0x06ee }
            if (r10 == 0) goto L_0x000e;
        L_0x05fd:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x0605:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x060d:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x061c:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x061c }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x061f:
            r12 = move-exception;
            r39 = "FileTransfer";
            r40 = r12.getMessage();	 Catch:{ all -> 0x0743 }
            r0 = r39;
            r1 = r40;
            org.apache.cordova.LOG.m8e(r0, r1, r12);	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0743 }
            r39 = r0;
            r40 = new org.apache.cordova.PluginResult;	 Catch:{ all -> 0x0743 }
            r41 = org.apache.cordova.PluginResult.Status.JSON_EXCEPTION;	 Catch:{ all -> 0x0743 }
            r40.<init>(r41);	 Catch:{ all -> 0x0743 }
            r39.sendPluginResult(r40);	 Catch:{ all -> 0x0743 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x0828 }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x0828 }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x0828 }
            monitor-exit(r40);	 Catch:{ all -> 0x0828 }
            if (r10 == 0) goto L_0x000e;
        L_0x0656:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x065e:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x0666:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x0675:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0675 }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0678:
            r34 = move-exception;
            r39 = org.apache.cordova.filetransfer.FileTransfer.CONNECTION_ERR;	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$source;	 Catch:{ all -> 0x0743 }
            r40 = r0;
            r0 = r44;
            r0 = r0.val$target;	 Catch:{ all -> 0x0743 }
            r41 = r0;
            r0 = r39;
            r1 = r40;
            r2 = r41;
            r3 = r34;
            r13 = org.apache.cordova.filetransfer.FileTransfer.createFileTransferError(r0, r1, r2, r10, r3);	 Catch:{ all -> 0x0743 }
            r39 = "FileTransfer";
            r40 = r13.toString();	 Catch:{ all -> 0x0743 }
            r0 = r39;
            r1 = r40;
            r2 = r34;
            org.apache.cordova.LOG.m8e(r0, r1, r2);	 Catch:{ all -> 0x0743 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0743 }
            r39 = r0;
            r40 = new org.apache.cordova.PluginResult;	 Catch:{ all -> 0x0743 }
            r41 = org.apache.cordova.PluginResult.Status.IO_EXCEPTION;	 Catch:{ all -> 0x0743 }
            r0 = r40;
            r1 = r41;
            r0.<init>(r1, r13);	 Catch:{ all -> 0x0743 }
            r39.sendPluginResult(r40);	 Catch:{ all -> 0x0743 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x082b }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x082b }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x082b }
            monitor-exit(r40);	 Catch:{ all -> 0x082b }
            if (r10 == 0) goto L_0x000e;
        L_0x06cf:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x06d7:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x06df:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x06ee:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x06ee }
            throw r39;
        L_0x06f1:
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0777 }
            r39 = r0;
            r0 = r39;
            r0.connection = r10;	 Catch:{ all -> 0x0777 }
            monitor-exit(r40);	 Catch:{ all -> 0x0777 }
            r22 = new java.io.ByteArrayOutputStream;	 Catch:{ all -> 0x0728 }
            r39 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
            r40 = r10.getContentLength();	 Catch:{ all -> 0x0728 }
            r39 = java.lang.Math.max(r39, r40);	 Catch:{ all -> 0x0728 }
            r0 = r22;
            r1 = r39;
            r0.<init>(r1);	 Catch:{ all -> 0x0728 }
            r39 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
            r0 = r39;
            r6 = new byte[r0];	 Catch:{ all -> 0x0728 }
            r9 = 0;
        L_0x0716:
            r0 = r16;
            r9 = r0.read(r6);	 Catch:{ all -> 0x0728 }
            if (r9 <= 0) goto L_0x077a;
        L_0x071e:
            r39 = 0;
            r0 = r22;
            r1 = r39;
            r0.write(r6, r1, r9);	 Catch:{ all -> 0x0728 }
            goto L_0x0716;
        L_0x0728:
            r39 = move-exception;
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x081c }
            r41 = r0;
            r42 = 0;
            r0 = r42;
            r1 = r41;
            r1.connection = r0;	 Catch:{ all -> 0x081c }
            monitor-exit(r40);	 Catch:{ all -> 0x081c }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r16);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x0743:
            r39 = move-exception;
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r41 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x082e }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x082e }
            r42 = r0;
            r41.remove(r42);	 Catch:{ all -> 0x082e }
            monitor-exit(r40);	 Catch:{ all -> 0x082e }
            if (r10 == 0) goto L_0x0776;
        L_0x0759:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r40 = r0;
            if (r40 == 0) goto L_0x0776;
        L_0x0761:
            r0 = r44;
            r0 = r0.val$useHttps;
            r40 = r0;
            if (r40 == 0) goto L_0x0776;
        L_0x0769:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
        L_0x0776:
            throw r39;
        L_0x0777:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0777 }
            throw r39;	 Catch:{ all -> 0x0728 }
        L_0x077a:
            r39 = "UTF-8";
            r0 = r22;
            r1 = r39;
            r29 = r0.toString(r1);	 Catch:{ all -> 0x0728 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = r0;
            monitor-enter(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ all -> 0x0819 }
            r39 = r0;
            r41 = 0;
            r0 = r41;
            r1 = r39;
            r1.connection = r0;	 Catch:{ all -> 0x0819 }
            monitor-exit(r40);	 Catch:{ all -> 0x0819 }
            org.apache.cordova.filetransfer.FileTransfer.safeClose(r16);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "FileTransfer";
            r40 = "got response from server";
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = "FileTransfer";
            r40 = 0;
            r41 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
            r42 = r29.length();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = java.lang.Math.min(r41, r42);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r29;
            r1 = r40;
            r2 = r41;
            r40 = r0.substring(r1, r2);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            org.apache.cordova.LOG.m4d(r39, r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r30;
            r1 = r28;
            r0.setResponseCode(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r30;
            r1 = r29;
            r0.setResponse(r1);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r0 = r44;
            r0 = r0.val$context;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39 = r0;
            r40 = new org.apache.cordova.PluginResult;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r41 = org.apache.cordova.PluginResult.Status.OK;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r42 = r30.toJSONObject();	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40.<init>(r41, r42);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r39.sendPluginResult(r40);	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
            r40 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;
            monitor-enter(r40);
            r39 = org.apache.cordova.filetransfer.FileTransfer.activeRequests;	 Catch:{ all -> 0x081f }
            r0 = r44;
            r0 = r0.val$objectId;	 Catch:{ all -> 0x081f }
            r41 = r0;
            r0 = r39;
            r1 = r41;
            r0.remove(r1);	 Catch:{ all -> 0x081f }
            monitor-exit(r40);	 Catch:{ all -> 0x081f }
            if (r10 == 0) goto L_0x000e;
        L_0x07fa:
            r0 = r44;
            r0 = r0.val$trustEveryone;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x0802:
            r0 = r44;
            r0 = r0.val$useHttps;
            r39 = r0;
            if (r39 == 0) goto L_0x000e;
        L_0x080a:
            r15 = r10;
            r15 = (javax.net.ssl.HttpsURLConnection) r15;
            r0 = r20;
            r15.setHostnameVerifier(r0);
            r0 = r21;
            r15.setSSLSocketFactory(r0);
            goto L_0x000e;
        L_0x0819:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0819 }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x081c:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x081c }
            throw r39;	 Catch:{ FileNotFoundException -> 0x0302, IOException -> 0x0473, JSONException -> 0x061f, Throwable -> 0x0678 }
        L_0x081f:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x081f }
            throw r39;
        L_0x0822:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0822 }
            throw r39;
        L_0x0825:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0825 }
            throw r39;
        L_0x0828:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x0828 }
            throw r39;
        L_0x082b:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x082b }
            throw r39;
        L_0x082e:
            r39 = move-exception;
            monitor-exit(r40);	 Catch:{ all -> 0x082e }
            throw r39;
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.cordova.filetransfer.FileTransfer.1.run():void");
        }
    }

    /* renamed from: org.apache.cordova.filetransfer.FileTransfer.2 */
    static class C02362 implements HostnameVerifier {
        C02362() {
        }

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /* renamed from: org.apache.cordova.filetransfer.FileTransfer.3 */
    static class C02373 implements X509TrustManager {
        C02373() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    /* renamed from: org.apache.cordova.filetransfer.FileTransfer.4 */
    class C02384 implements Runnable {
        final /* synthetic */ RequestContext val$context;
        final /* synthetic */ JSONObject val$headers;
        final /* synthetic */ boolean val$isLocalTransfer;
        final /* synthetic */ String val$objectId;
        final /* synthetic */ CordovaResourceApi val$resourceApi;
        final /* synthetic */ String val$source;
        final /* synthetic */ Uri val$sourceUri;
        final /* synthetic */ String val$target;
        final /* synthetic */ boolean val$trustEveryone;
        final /* synthetic */ boolean val$useHttps;

        C02384(RequestContext requestContext, String str, CordovaResourceApi cordovaResourceApi, Uri uri, boolean z, boolean z2, boolean z3, JSONObject jSONObject, String str2, String str3) {
            this.val$context = requestContext;
            this.val$target = str;
            this.val$resourceApi = cordovaResourceApi;
            this.val$sourceUri = uri;
            this.val$isLocalTransfer = z;
            this.val$useHttps = z2;
            this.val$trustEveryone = z3;
            this.val$headers = jSONObject;
            this.val$source = str2;
            this.val$objectId = str3;
        }

        public void run() {
            HttpsURLConnection https;
            PluginResult pluginResult;
            Throwable e;
            Throwable th;
            if (!this.val$context.aborted) {
                Uri tmpTarget = Uri.parse(this.val$target);
                CordovaResourceApi cordovaResourceApi = this.val$resourceApi;
                if (tmpTarget.getScheme() == null) {
                    tmpTarget = Uri.fromFile(new File(this.val$target));
                }
                Uri targetUri = cordovaResourceApi.remapUri(tmpTarget);
                HttpURLConnection connection = null;
                HostnameVerifier oldHostnameVerifier = null;
                SSLSocketFactory oldSocketFactory = null;
                File file = null;
                PluginResult result = null;
                TrackingInputStream inputStream = null;
                boolean cached = false;
                PluginResult result2;
                JSONObject error;
                try {
                    file = this.val$resourceApi.mapUriToFile(targetUri);
                    this.val$context.targetFile = file;
                    LOG.m4d(FileTransfer.LOG_TAG, "Download file:" + this.val$sourceUri);
                    FileProgressResult progress = new FileProgressResult();
                    if (this.val$isLocalTransfer) {
                        OpenForReadResult readResult = this.val$resourceApi.openForRead(this.val$sourceUri);
                        if (readResult.length != -1) {
                            progress.setLengthComputable(true);
                            progress.setTotal(readResult.length);
                        }
                        inputStream = new SimpleTrackingInputStream(readResult.inputStream);
                        result2 = null;
                    } else {
                        connection = this.val$resourceApi.createHttpConnection(this.val$sourceUri);
                        if (this.val$useHttps && this.val$trustEveryone) {
                            https = (HttpsURLConnection) connection;
                            oldSocketFactory = FileTransfer.trustAllHosts(https);
                            oldHostnameVerifier = https.getHostnameVerifier();
                            https.setHostnameVerifier(FileTransfer.DO_NOT_VERIFY);
                        }
                        connection.setRequestMethod("GET");
                        String cookie = FileTransfer.this.getCookies(this.val$sourceUri.toString());
                        if (cookie != null) {
                            connection.setRequestProperty("cookie", cookie);
                        }
                        connection.setRequestProperty("Accept-Encoding", "gzip");
                        if (this.val$headers != null) {
                            FileTransfer.addHeadersToRequest(connection, this.val$headers);
                        }
                        connection.connect();
                        if (connection.getResponseCode() == 304) {
                            cached = true;
                            connection.disconnect();
                            LOG.m4d(FileTransfer.LOG_TAG, "Resource not modified: " + this.val$source);
                            error = FileTransfer.createFileTransferError(FileTransfer.NOT_MODIFIED_ERR, this.val$source, this.val$target, connection, null);
                            pluginResult = new PluginResult(Status.ERROR, error);
                        } else {
                            if ((connection.getContentEncoding() == null || connection.getContentEncoding().equalsIgnoreCase("gzip")) && connection.getContentLength() != -1) {
                                progress.setLengthComputable(true);
                                progress.setTotal((long) connection.getContentLength());
                            }
                            inputStream = FileTransfer.getInputStream(connection);
                            result2 = null;
                        }
                    }
                    if (cached) {
                        result = result2;
                    } else {
                        try {
                            synchronized (this.val$context) {
                                if (this.val$context.aborted) {
                                    synchronized (this.val$context) {
                                        this.val$context.connection = null;
                                    }
                                    FileTransfer.safeClose(inputStream);
                                    FileTransfer.safeClose(null);
                                    synchronized (FileTransfer.activeRequests) {
                                        FileTransfer.activeRequests.remove(this.val$objectId);
                                    }
                                    if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                                        https = (HttpsURLConnection) connection;
                                        https.setHostnameVerifier(oldHostnameVerifier);
                                        https.setSSLSocketFactory(oldSocketFactory);
                                    }
                                    if (result2 == null) {
                                        pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                                    } else {
                                        result = result2;
                                    }
                                    if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                                        file.delete();
                                    }
                                    this.val$context.sendPluginResult(result);
                                    return;
                                }
                                this.val$context.connection = connection;
                                byte[] buffer = new byte[FileTransfer.MAX_BUFFER_SIZE];
                                OutputStream outputStream = this.val$resourceApi.openOutputStream(targetUri);
                                while (true) {
                                    int bytesRead = inputStream.read(buffer);
                                    if (bytesRead <= 0) {
                                        break;
                                    }
                                    outputStream.write(buffer, 0, bytesRead);
                                    progress.setLoaded(inputStream.getTotalRawBytesRead());
                                    pluginResult = new PluginResult(Status.OK, progress.toJSONObject());
                                    pluginResult.setKeepCallback(true);
                                    this.val$context.sendPluginResult(pluginResult);
                                }
                                synchronized (this.val$context) {
                                    this.val$context.connection = null;
                                }
                                FileTransfer.safeClose(inputStream);
                                FileTransfer.safeClose(outputStream);
                                LOG.m4d(FileTransfer.LOG_TAG, "Saved file: " + this.val$target);
                                Class webViewClass = FileTransfer.this.webView.getClass();
                                PluginManager pm = null;
                                try {
                                    pm = (PluginManager) webViewClass.getMethod("getPluginManager", new Class[0]).invoke(FileTransfer.this.webView, new Object[0]);
                                } catch (NoSuchMethodException e2) {
                                } catch (IllegalAccessException e3) {
                                } catch (InvocationTargetException e4) {
                                }
                                if (pm == null) {
                                    try {
                                        pm = (PluginManager) webViewClass.getField("pluginManager").get(FileTransfer.this.webView);
                                    } catch (NoSuchFieldException e5) {
                                    } catch (IllegalAccessException e6) {
                                    }
                                }
                                file = this.val$resourceApi.mapUriToFile(targetUri);
                                this.val$context.targetFile = file;
                                FileUtils filePlugin = (FileUtils) pm.getPlugin("File");
                                if (filePlugin != null) {
                                    JSONObject fileEntry = filePlugin.getEntryForFile(file);
                                    if (fileEntry != null) {
                                        pluginResult = new PluginResult(Status.OK, fileEntry);
                                    } else {
                                        error = FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null);
                                        LOG.m7e(FileTransfer.LOG_TAG, "File plugin cannot represent download path");
                                        pluginResult = new PluginResult(Status.IO_EXCEPTION, error);
                                    }
                                } else {
                                    LOG.m7e(FileTransfer.LOG_TAG, "File plugin not found; cannot save downloaded file");
                                    pluginResult = new PluginResult(Status.ERROR, "File plugin not found; cannot save downloaded file");
                                }
                            }
                        } catch (FileNotFoundException e7) {
                            e = e7;
                        } catch (IOException e8) {
                            e = e8;
                            error = FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, e);
                            LOG.m8e(FileTransfer.LOG_TAG, error.toString(), e);
                            pluginResult = new PluginResult(Status.IO_EXCEPTION, error);
                            synchronized (FileTransfer.activeRequests) {
                                FileTransfer.activeRequests.remove(this.val$objectId);
                            }
                            if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                                https = (HttpsURLConnection) connection;
                                https.setHostnameVerifier(oldHostnameVerifier);
                                https.setSSLSocketFactory(oldSocketFactory);
                            }
                            if (pluginResult == null) {
                                pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                            }
                            if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                                file.delete();
                            }
                            this.val$context.sendPluginResult(result);
                        } catch (JSONException e9) {
                            e = e9;
                            LOG.m8e(FileTransfer.LOG_TAG, e.getMessage(), e);
                            pluginResult = new PluginResult(Status.JSON_EXCEPTION);
                            synchronized (FileTransfer.activeRequests) {
                                FileTransfer.activeRequests.remove(this.val$objectId);
                            }
                            if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                                https = (HttpsURLConnection) connection;
                                https.setHostnameVerifier(oldHostnameVerifier);
                                https.setSSLSocketFactory(oldSocketFactory);
                            }
                            if (pluginResult == null) {
                                pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                            }
                            if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                                file.delete();
                            }
                            this.val$context.sendPluginResult(result);
                        } catch (Throwable th2) {
                            e = th2;
                            error = FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, e);
                            LOG.m8e(FileTransfer.LOG_TAG, error.toString(), e);
                            pluginResult = new PluginResult(Status.IO_EXCEPTION, error);
                            synchronized (FileTransfer.activeRequests) {
                                FileTransfer.activeRequests.remove(this.val$objectId);
                            }
                            if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                                https = (HttpsURLConnection) connection;
                                https.setHostnameVerifier(oldHostnameVerifier);
                                https.setSSLSocketFactory(oldSocketFactory);
                            }
                            if (pluginResult == null) {
                                pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                            }
                            if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                                file.delete();
                            }
                            this.val$context.sendPluginResult(result);
                        }
                    }
                    synchronized (FileTransfer.activeRequests) {
                        FileTransfer.activeRequests.remove(this.val$objectId);
                    }
                    if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                        https = (HttpsURLConnection) connection;
                        https.setHostnameVerifier(oldHostnameVerifier);
                        https.setSSLSocketFactory(oldSocketFactory);
                    }
                    if (result == null) {
                        pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                    }
                    if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                        file.delete();
                    }
                    this.val$context.sendPluginResult(result);
                } catch (FileNotFoundException e10) {
                    e = e10;
                    result2 = null;
                    try {
                        error = FileTransfer.createFileTransferError(FileTransfer.FILE_NOT_FOUND_ERR, this.val$source, this.val$target, connection, e);
                        LOG.m8e(FileTransfer.LOG_TAG, error.toString(), e);
                        pluginResult = new PluginResult(Status.IO_EXCEPTION, error);
                        synchronized (FileTransfer.activeRequests) {
                            FileTransfer.activeRequests.remove(this.val$objectId);
                        }
                        if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                            https = (HttpsURLConnection) connection;
                            https.setHostnameVerifier(oldHostnameVerifier);
                            https.setSSLSocketFactory(oldSocketFactory);
                        }
                        if (pluginResult == null) {
                            pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                        }
                        if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                            file.delete();
                        }
                        this.val$context.sendPluginResult(result);
                    } catch (Throwable th3) {
                        th = th3;
                        result = result2;
                        synchronized (FileTransfer.activeRequests) {
                            FileTransfer.activeRequests.remove(this.val$objectId);
                        }
                        if (connection != null && this.val$trustEveryone && this.val$useHttps) {
                            https = (HttpsURLConnection) connection;
                            https.setHostnameVerifier(oldHostnameVerifier);
                            https.setSSLSocketFactory(oldSocketFactory);
                        }
                        if (result == null) {
                            pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                        }
                        if (!(cached || result.getStatus() == Status.OK.ordinal() || file == null)) {
                            file.delete();
                        }
                        this.val$context.sendPluginResult(result);
                        throw th;
                    }
                } catch (IOException e11) {
                    e = e11;
                    result2 = null;
                    error = FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, e);
                    LOG.m8e(FileTransfer.LOG_TAG, error.toString(), e);
                    pluginResult = new PluginResult(Status.IO_EXCEPTION, error);
                    synchronized (FileTransfer.activeRequests) {
                        FileTransfer.activeRequests.remove(this.val$objectId);
                    }
                    https = (HttpsURLConnection) connection;
                    https.setHostnameVerifier(oldHostnameVerifier);
                    https.setSSLSocketFactory(oldSocketFactory);
                    if (pluginResult == null) {
                        pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                    }
                    file.delete();
                    this.val$context.sendPluginResult(result);
                } catch (JSONException e12) {
                    e = e12;
                    result2 = null;
                    LOG.m8e(FileTransfer.LOG_TAG, e.getMessage(), e);
                    pluginResult = new PluginResult(Status.JSON_EXCEPTION);
                    synchronized (FileTransfer.activeRequests) {
                        FileTransfer.activeRequests.remove(this.val$objectId);
                    }
                    https = (HttpsURLConnection) connection;
                    https.setHostnameVerifier(oldHostnameVerifier);
                    https.setSSLSocketFactory(oldSocketFactory);
                    if (pluginResult == null) {
                        pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                    }
                    file.delete();
                    this.val$context.sendPluginResult(result);
                } catch (Throwable th4) {
                    th = th4;
                    synchronized (FileTransfer.activeRequests) {
                        FileTransfer.activeRequests.remove(this.val$objectId);
                    }
                    https = (HttpsURLConnection) connection;
                    https.setHostnameVerifier(oldHostnameVerifier);
                    https.setSSLSocketFactory(oldSocketFactory);
                    if (result == null) {
                        pluginResult = new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.CONNECTION_ERR, this.val$source, this.val$target, connection, null));
                    }
                    file.delete();
                    this.val$context.sendPluginResult(result);
                    throw th;
                }
            }
        }
    }

    /* renamed from: org.apache.cordova.filetransfer.FileTransfer.5 */
    class C02395 implements Runnable {
        final /* synthetic */ RequestContext val$context;

        C02395(RequestContext requestContext) {
            this.val$context = requestContext;
        }

        public void run() {
            synchronized (this.val$context) {
                File file = this.val$context.targetFile;
                if (file != null) {
                    file.delete();
                }
                this.val$context.sendPluginResult(new PluginResult(Status.ERROR, FileTransfer.createFileTransferError(FileTransfer.ABORTED_ERR, this.val$context.source, this.val$context.target, null, Integer.valueOf(-1), null)));
                this.val$context.aborted = true;
                if (this.val$context.connection != null) {
                    try {
                        this.val$context.connection.disconnect();
                    } catch (Throwable e) {
                        LOG.m8e(FileTransfer.LOG_TAG, "CB-8431 Catch workaround for fatal exception", e);
                    }
                }
            }
        }
    }

    private static class ExposedGZIPInputStream extends GZIPInputStream {
        public ExposedGZIPInputStream(InputStream in) throws IOException {
            super(in);
        }

        public Inflater getInflater() {
            return this.inf;
        }
    }

    private static final class RequestContext {
        boolean aborted;
        CallbackContext callbackContext;
        HttpURLConnection connection;
        String source;
        String target;
        File targetFile;

        RequestContext(String source, String target, CallbackContext callbackContext) {
            this.source = source;
            this.target = target;
            this.callbackContext = callbackContext;
        }

        void sendPluginResult(PluginResult pluginResult) {
            synchronized (this) {
                if (!this.aborted) {
                    this.callbackContext.sendPluginResult(pluginResult);
                }
            }
        }
    }

    private static abstract class TrackingInputStream extends FilterInputStream {
        public abstract long getTotalRawBytesRead();

        public TrackingInputStream(InputStream in) {
            super(in);
        }
    }

    private static class SimpleTrackingInputStream extends TrackingInputStream {
        private long bytesRead;

        public SimpleTrackingInputStream(InputStream stream) {
            super(stream);
            this.bytesRead = 0;
        }

        private int updateBytesRead(int newBytesRead) {
            if (newBytesRead != -1) {
                this.bytesRead += (long) newBytesRead;
            }
            return newBytesRead;
        }

        public int read() throws IOException {
            return updateBytesRead(super.read());
        }

        public int read(byte[] bytes, int offset, int count) throws IOException {
            return updateBytesRead(super.read(bytes, offset, count));
        }

        public long getTotalRawBytesRead() {
            return this.bytesRead;
        }
    }

    private static class TrackingGZIPInputStream extends TrackingInputStream {
        private ExposedGZIPInputStream gzin;

        public TrackingGZIPInputStream(ExposedGZIPInputStream gzin) throws IOException {
            super(gzin);
            this.gzin = gzin;
        }

        public long getTotalRawBytesRead() {
            return this.gzin.getInflater().getBytesRead();
        }
    }

    static {
        FILE_NOT_FOUND_ERR = 1;
        INVALID_URL_ERR = 2;
        CONNECTION_ERR = 3;
        ABORTED_ERR = 4;
        NOT_MODIFIED_ERR = 5;
        activeRequests = new HashMap();
        DO_NOT_VERIFY = new C02362();
        trustAllCerts = new TrustManager[]{new C02373()};
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("upload") || action.equals("download")) {
            String source = args.getString(0);
            String target = args.getString(1);
            if (action.equals("upload")) {
                upload(source, target, args, callbackContext);
                return true;
            }
            download(source, target, args, callbackContext);
            return true;
        } else if (!action.equals("abort")) {
            return false;
        } else {
            abort(args.getString(0));
            callbackContext.success();
            return true;
        }
    }

    private static void addHeadersToRequest(URLConnection connection, JSONObject headers) {
        try {
            Iterator<?> iter = headers.keys();
            while (iter.hasNext()) {
                String headerKey = iter.next().toString();
                String cleanHeaderKey = headerKey.replaceAll("\\n", BuildConfig.FLAVOR).replaceAll("\\s+", BuildConfig.FLAVOR).replaceAll(":", BuildConfig.FLAVOR).replaceAll("[^\\x20-\\x7E]+", BuildConfig.FLAVOR);
                JSONArray headerValues = headers.optJSONArray(headerKey);
                if (headerValues == null) {
                    headerValues = new JSONArray();
                    headerValues.put(headers.getString(headerKey).replaceAll("\\s+", " ").replaceAll("\\n", " ").replaceAll("[^\\x20-\\x7E]+", " "));
                }
                connection.setRequestProperty(cleanHeaderKey, headerValues.getString(0));
                for (int i = 1; i < headerValues.length(); i++) {
                    connection.addRequestProperty(headerKey, headerValues.getString(i));
                }
            }
        } catch (JSONException e) {
        }
    }

    private String getCookies(String target) {
        boolean gotCookie = false;
        String cookie = null;
        try {
            Method gcmMethod = this.webView.getClass().getMethod("getCookieManager", new Class[0]);
            Class iccmClass = gcmMethod.getReturnType();
            cookie = (String) iccmClass.getMethod("getCookie", new Class[]{String.class}).invoke(iccmClass.cast(gcmMethod.invoke(this.webView, new Object[0])), new Object[]{target});
            gotCookie = true;
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e2) {
        } catch (InvocationTargetException e3) {
        } catch (ClassCastException e4) {
        }
        if (gotCookie || CookieManager.getInstance() == null) {
            return cookie;
        }
        return CookieManager.getInstance().getCookie(target);
    }

    private void upload(String source, String target, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject headers;
        LOG.m4d(LOG_TAG, "upload " + source + " to " + target);
        String fileKey = getArgument(args, 2, "file");
        String fileName = getArgument(args, 3, "image.jpg");
        String mimeType = getArgument(args, 4, "image/jpeg");
        JSONObject params = args.optJSONObject(5) == null ? new JSONObject() : args.optJSONObject(5);
        boolean trustEveryone = args.optBoolean(6);
        boolean chunkedMode = args.optBoolean(7) || args.isNull(7);
        if (args.optJSONObject(8) == null) {
            headers = params.optJSONObject("headers");
        } else {
            headers = args.optJSONObject(8);
        }
        String objectId = args.getString(9);
        String httpMethod = getArgument(args, 10, "POST");
        CordovaResourceApi resourceApi = this.webView.getResourceApi();
        LOG.m4d(LOG_TAG, "fileKey: " + fileKey);
        LOG.m4d(LOG_TAG, "fileName: " + fileName);
        LOG.m4d(LOG_TAG, "mimeType: " + mimeType);
        LOG.m4d(LOG_TAG, "params: " + params);
        LOG.m4d(LOG_TAG, "trustEveryone: " + trustEveryone);
        LOG.m4d(LOG_TAG, "chunkedMode: " + chunkedMode);
        LOG.m4d(LOG_TAG, "headers: " + headers);
        LOG.m4d(LOG_TAG, "objectId: " + objectId);
        LOG.m4d(LOG_TAG, "httpMethod: " + httpMethod);
        Uri targetUri = resourceApi.remapUri(Uri.parse(target));
        int uriType = CordovaResourceApi.getUriType(targetUri);
        boolean useHttps = uriType == 6;
        if (uriType == 5 || useHttps) {
            RequestContext context = new RequestContext(source, target, callbackContext);
            synchronized (activeRequests) {
                activeRequests.put(objectId, context);
            }
            this.cordova.getThreadPool().execute(new C02351(context, source, resourceApi, targetUri, useHttps, trustEveryone, httpMethod, headers, target, params, fileKey, fileName, mimeType, chunkedMode, objectId));
            return;
        }
        JSONObject error = createFileTransferError(INVALID_URL_ERR, source, target, null, Integer.valueOf(0), null);
        LOG.m7e(LOG_TAG, "Unsupported URI: " + targetUri);
        callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, error));
    }

    private static void safeClose(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    private static TrackingInputStream getInputStream(URLConnection conn) throws IOException {
        String encoding = conn.getContentEncoding();
        if (encoding == null || !encoding.equalsIgnoreCase("gzip")) {
            return new SimpleTrackingInputStream(conn.getInputStream());
        }
        return new TrackingGZIPInputStream(new ExposedGZIPInputStream(conn.getInputStream()));
    }

    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Throwable e) {
            LOG.m8e(LOG_TAG, e.getMessage(), e);
        }
        return oldFactory;
    }

    private static JSONObject createFileTransferError(int errorCode, String source, String target, URLConnection connection, Throwable throwable) {
        int httpStatus = 0;
        StringBuilder bodyBuilder = new StringBuilder();
        String body = null;
        if (connection != null) {
            BufferedReader reader;
            try {
                if (connection instanceof HttpURLConnection) {
                    httpStatus = ((HttpURLConnection) connection).getResponseCode();
                    InputStream err = ((HttpURLConnection) connection).getErrorStream();
                    if (err != null) {
                        reader = new BufferedReader(new InputStreamReader(err, "UTF-8"));
                        String line = reader.readLine();
                        while (line != null) {
                            bodyBuilder.append(line);
                            line = reader.readLine();
                            if (line != null) {
                                bodyBuilder.append('\n');
                            }
                        }
                        body = bodyBuilder.toString();
                        reader.close();
                    }
                }
            } catch (Throwable e) {
                LOG.m17w(LOG_TAG, "Error getting HTTP status code from connection.", e);
            }
        }
        return createFileTransferError(errorCode, source, target, body, Integer.valueOf(httpStatus), throwable);
    }

    private static JSONObject createFileTransferError(int errorCode, String source, String target, String body, Integer httpStatus, Throwable throwable) {
        Throwable e;
        JSONObject error = null;
        try {
            JSONObject error2 = new JSONObject();
            try {
                error2.put("code", errorCode);
                error2.put("source", source);
                error2.put("target", target);
                if (body != null) {
                    error2.put("body", body);
                }
                if (httpStatus != null) {
                    error2.put("http_status", httpStatus);
                }
                if (throwable != null) {
                    String msg = throwable.getMessage();
                    if (msg == null || BuildConfig.FLAVOR.equals(msg)) {
                        msg = throwable.toString();
                    }
                    error2.put("exception", msg);
                }
                return error2;
            } catch (JSONException e2) {
                e = e2;
                error = error2;
                LOG.m8e(LOG_TAG, e.getMessage(), e);
                return error;
            }
        } catch (JSONException e3) {
            e = e3;
            LOG.m8e(LOG_TAG, e.getMessage(), e);
            return error;
        }
    }

    private static String getArgument(JSONArray args, int position, String defaultString) {
        String arg = defaultString;
        if (args.length() <= position) {
            return arg;
        }
        arg = args.optString(position);
        if (arg == null || "null".equals(arg)) {
            return defaultString;
        }
        return arg;
    }

    private void download(String source, String target, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.m4d(LOG_TAG, "download " + source + " to " + target);
        CordovaResourceApi resourceApi = this.webView.getResourceApi();
        boolean trustEveryone = args.optBoolean(2);
        String objectId = args.getString(3);
        JSONObject headers = args.optJSONObject(4);
        Uri sourceUri = resourceApi.remapUri(Uri.parse(source));
        int uriType = CordovaResourceApi.getUriType(sourceUri);
        boolean useHttps = uriType == 6;
        boolean isLocalTransfer = (useHttps || uriType == 5) ? false : true;
        if (uriType == -1) {
            JSONObject error = createFileTransferError(INVALID_URL_ERR, source, target, null, Integer.valueOf(0), null);
            LOG.m7e(LOG_TAG, "Unsupported URI: " + sourceUri);
            callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, error));
            return;
        }
        Boolean shouldAllowRequest = null;
        if (isLocalTransfer) {
            shouldAllowRequest = Boolean.valueOf(true);
        }
        if (shouldAllowRequest == null) {
            try {
                shouldAllowRequest = Boolean.valueOf(((Whitelist) this.webView.getClass().getMethod("getWhitelist", new Class[0]).invoke(this.webView, new Object[0])).isUrlWhiteListed(source));
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e2) {
            } catch (InvocationTargetException e3) {
            }
        }
        if (shouldAllowRequest == null) {
            try {
                PluginManager pm = (PluginManager) this.webView.getClass().getMethod("getPluginManager", new Class[0]).invoke(this.webView, new Object[0]);
                shouldAllowRequest = (Boolean) pm.getClass().getMethod("shouldAllowRequest", new Class[]{String.class}).invoke(pm, new Object[]{source});
            } catch (NoSuchMethodException e4) {
            } catch (IllegalAccessException e5) {
            } catch (InvocationTargetException e6) {
            }
        }
        if (Boolean.TRUE.equals(shouldAllowRequest)) {
            RequestContext context = new RequestContext(source, target, callbackContext);
            synchronized (activeRequests) {
                activeRequests.put(objectId, context);
            }
            this.cordova.getThreadPool().execute(new C02384(context, target, resourceApi, sourceUri, isLocalTransfer, useHttps, trustEveryone, headers, source, objectId));
            return;
        }
        LOG.m16w(LOG_TAG, "Source URL is not in white list: '" + source + "'");
        callbackContext.sendPluginResult(new PluginResult(Status.IO_EXCEPTION, createFileTransferError(CONNECTION_ERR, source, target, null, Integer.valueOf(401), null)));
    }

    private void abort(String objectId) {
        synchronized (activeRequests) {
            RequestContext context = (RequestContext) activeRequests.remove(objectId);
        }
        if (context != null) {
            this.cordova.getThreadPool().execute(new C02395(context));
        }
    }
}
