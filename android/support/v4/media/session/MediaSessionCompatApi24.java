package android.support.v4.media.session;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@RequiresApi(24)
class MediaSessionCompatApi24 {
    private static final String TAG = "MediaSessionCompatApi24";

    public interface Callback extends android.support.v4.media.session.MediaSessionCompatApi23.Callback {
        void onPrepare();

        void onPrepareFromMediaId(String str, Bundle bundle);

        void onPrepareFromSearch(String str, Bundle bundle);

        void onPrepareFromUri(Uri uri, Bundle bundle);
    }

    static class CallbackProxy<T extends Callback> extends CallbackProxy<T> {
        public CallbackProxy(T callback) {
            super(callback);
        }

        public void onPrepare() {
            ((Callback) this.mCallback).onPrepare();
        }

        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromMediaId(mediaId, extras);
        }

        public void onPrepareFromSearch(String query, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromSearch(query, extras);
        }

        public void onPrepareFromUri(Uri uri, Bundle extras) {
            ((Callback) this.mCallback).onPrepareFromUri(uri, extras);
        }
    }

    MediaSessionCompatApi24() {
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getCallingPackage(java.lang.Object r6) {
        /*
        r2 = r6;
        r2 = (android.media.session.MediaSession) r2;
        r3 = r2.getClass();	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
        r4 = "getCallingPackage";
        r5 = 0;
        r5 = new java.lang.Class[r5];	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
        r1 = r3.getMethod(r4, r5);	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
        r3 = 0;
        r3 = new java.lang.Object[r3];	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
        r3 = r1.invoke(r2, r3);	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
        r3 = (java.lang.String) r3;	 Catch:{ NoSuchMethodException -> 0x0024, InvocationTargetException -> 0x0026, IllegalAccessException -> 0x001a }
    L_0x0019:
        return r3;
    L_0x001a:
        r0 = move-exception;
    L_0x001b:
        r3 = "MediaSessionCompatApi24";
        r4 = "Cannot execute MediaSession.getCallingPackage()";
        android.util.Log.e(r3, r4, r0);
        r3 = 0;
        goto L_0x0019;
    L_0x0024:
        r0 = move-exception;
        goto L_0x001b;
    L_0x0026:
        r0 = move-exception;
        goto L_0x001b;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.media.session.MediaSessionCompatApi24.getCallingPackage(java.lang.Object):java.lang.String");
    }
}
