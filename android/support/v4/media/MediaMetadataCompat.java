package android.support.v4.media;

import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public final class MediaMetadataCompat implements Parcelable {
    public static final Creator<MediaMetadataCompat> CREATOR;
    static final ArrayMap<String, Integer> METADATA_KEYS_TYPE;
    public static final String METADATA_KEY_ADVERTISEMENT = "android.media.metadata.ADVERTISEMENT";
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    public static final String METADATA_KEY_BT_FOLDER_TYPE = "android.media.metadata.BT_FOLDER_TYPE";
    public static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";
    public static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";
    public static final String METADATA_KEY_DATE = "android.media.metadata.DATE";
    public static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";
    public static final String METADATA_KEY_DISPLAY_DESCRIPTION = "android.media.metadata.DISPLAY_DESCRIPTION";
    public static final String METADATA_KEY_DISPLAY_ICON = "android.media.metadata.DISPLAY_ICON";
    public static final String METADATA_KEY_DISPLAY_ICON_URI = "android.media.metadata.DISPLAY_ICON_URI";
    public static final String METADATA_KEY_DISPLAY_SUBTITLE = "android.media.metadata.DISPLAY_SUBTITLE";
    public static final String METADATA_KEY_DISPLAY_TITLE = "android.media.metadata.DISPLAY_TITLE";
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";
    public static final String METADATA_KEY_MEDIA_URI = "android.media.metadata.MEDIA_URI";
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";
    static final int METADATA_TYPE_BITMAP = 2;
    static final int METADATA_TYPE_LONG = 0;
    static final int METADATA_TYPE_RATING = 3;
    static final int METADATA_TYPE_TEXT = 1;
    private static final String[] PREFERRED_BITMAP_ORDER;
    private static final String[] PREFERRED_DESCRIPTION_ORDER;
    private static final String[] PREFERRED_URI_ORDER;
    private static final String TAG = "MediaMetadata";
    final Bundle mBundle;
    private MediaDescriptionCompat mDescription;
    private Object mMetadataObj;

    /* renamed from: android.support.v4.media.MediaMetadataCompat.1 */
    static class C00531 implements Creator<MediaMetadataCompat> {
        C00531() {
        }

        public MediaMetadataCompat createFromParcel(Parcel in) {
            return new MediaMetadataCompat(in);
        }

        public MediaMetadataCompat[] newArray(int size) {
            return new MediaMetadataCompat[size];
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BitmapKey {
    }

    public static final class Builder {
        private final Bundle mBundle;

        public Builder() {
            this.mBundle = new Bundle();
        }

        public Builder(MediaMetadataCompat source) {
            this.mBundle = new Bundle(source.mBundle);
        }

        @RestrictTo({Scope.LIBRARY_GROUP})
        public Builder(MediaMetadataCompat source, int maxBitmapSize) {
            this(source);
            for (String key : this.mBundle.keySet()) {
                Bitmap value = this.mBundle.get(key);
                if (value != null && (value instanceof Bitmap)) {
                    Bitmap bmp = value;
                    if (bmp.getHeight() > maxBitmapSize || bmp.getWidth() > maxBitmapSize) {
                        putBitmap(key, scaleBitmap(bmp, maxBitmapSize));
                    } else if (VERSION.SDK_INT >= 14 && (key.equals(MediaMetadataCompat.METADATA_KEY_ART) || key.equals(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))) {
                        putBitmap(key, bmp.copy(bmp.getConfig(), false));
                    }
                }
            }
        }

        public Builder putText(String key, CharSequence value) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadataCompat.METADATA_TYPE_TEXT) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a CharSequence");
        }

        public Builder putString(String key, String value) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadataCompat.METADATA_TYPE_TEXT) {
                this.mBundle.putCharSequence(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a String");
        }

        public Builder putLong(String key, long value) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(key)).intValue() == 0) {
                this.mBundle.putLong(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a long");
        }

        public Builder putRating(String key, RatingCompat value) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadataCompat.METADATA_TYPE_RATING) {
                if (VERSION.SDK_INT >= 19) {
                    this.mBundle.putParcelable(key, (Parcelable) value.getRating());
                } else {
                    this.mBundle.putParcelable(key, value);
                }
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Rating");
        }

        public Builder putBitmap(String key, Bitmap value) {
            if (!MediaMetadataCompat.METADATA_KEYS_TYPE.containsKey(key) || ((Integer) MediaMetadataCompat.METADATA_KEYS_TYPE.get(key)).intValue() == MediaMetadataCompat.METADATA_TYPE_BITMAP) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Bitmap");
        }

        public MediaMetadataCompat build() {
            return new MediaMetadataCompat(this.mBundle);
        }

        private Bitmap scaleBitmap(Bitmap bmp, int maxSize) {
            float maxSizeF = (float) maxSize;
            float scale = Math.min(maxSizeF / ((float) bmp.getWidth()), maxSizeF / ((float) bmp.getHeight()));
            return Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth()) * scale), (int) (((float) bmp.getHeight()) * scale), true);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LongKey {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RatingKey {
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextKey {
    }

    static {
        METADATA_KEYS_TYPE = new ArrayMap();
        METADATA_KEYS_TYPE.put(METADATA_KEY_TITLE, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ARTIST, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DURATION, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_AUTHOR, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_WRITER, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPOSER, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPILATION, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DATE, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_YEAR, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_GENRE, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_TRACK_NUMBER, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_NUM_TRACKS, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISC_NUMBER, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ARTIST, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART, Integer.valueOf(METADATA_TYPE_BITMAP));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART_URI, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART, Integer.valueOf(METADATA_TYPE_BITMAP));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART_URI, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_USER_RATING, Integer.valueOf(METADATA_TYPE_RATING));
        METADATA_KEYS_TYPE.put(METADATA_KEY_RATING, Integer.valueOf(METADATA_TYPE_RATING));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_TITLE, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_SUBTITLE, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_DESCRIPTION, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON, Integer.valueOf(METADATA_TYPE_BITMAP));
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON_URI, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_MEDIA_ID, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_BT_FOLDER_TYPE, Integer.valueOf(METADATA_TYPE_LONG));
        METADATA_KEYS_TYPE.put(METADATA_KEY_MEDIA_URI, Integer.valueOf(METADATA_TYPE_TEXT));
        METADATA_KEYS_TYPE.put(METADATA_KEY_ADVERTISEMENT, Integer.valueOf(METADATA_TYPE_LONG));
        PREFERRED_DESCRIPTION_ORDER = new String[]{METADATA_KEY_TITLE, METADATA_KEY_ARTIST, METADATA_KEY_ALBUM, METADATA_KEY_ALBUM_ARTIST, METADATA_KEY_WRITER, METADATA_KEY_AUTHOR, METADATA_KEY_COMPOSER};
        String[] strArr = new String[METADATA_TYPE_RATING];
        strArr[METADATA_TYPE_LONG] = METADATA_KEY_DISPLAY_ICON;
        strArr[METADATA_TYPE_TEXT] = METADATA_KEY_ART;
        strArr[METADATA_TYPE_BITMAP] = METADATA_KEY_ALBUM_ART;
        PREFERRED_BITMAP_ORDER = strArr;
        strArr = new String[METADATA_TYPE_RATING];
        strArr[METADATA_TYPE_LONG] = METADATA_KEY_DISPLAY_ICON_URI;
        strArr[METADATA_TYPE_TEXT] = METADATA_KEY_ART_URI;
        strArr[METADATA_TYPE_BITMAP] = METADATA_KEY_ALBUM_ART_URI;
        PREFERRED_URI_ORDER = strArr;
        CREATOR = new C00531();
    }

    MediaMetadataCompat(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
    }

    MediaMetadataCompat(Parcel in) {
        this.mBundle = in.readBundle();
    }

    public boolean containsKey(String key) {
        return this.mBundle.containsKey(key);
    }

    public CharSequence getText(String key) {
        return this.mBundle.getCharSequence(key);
    }

    public String getString(String key) {
        CharSequence text = this.mBundle.getCharSequence(key);
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    public long getLong(String key) {
        return this.mBundle.getLong(key, 0);
    }

    public RatingCompat getRating(String key) {
        try {
            if (VERSION.SDK_INT >= 19) {
                return RatingCompat.fromRating(this.mBundle.getParcelable(key));
            }
            return (RatingCompat) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
            return null;
        }
    }

    public Bitmap getBitmap(String key) {
        Bitmap bmp = null;
        try {
            return (Bitmap) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return bmp;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.support.v4.media.MediaDescriptionCompat getDescription() {
        /*
        r22 = this;
        r0 = r22;
        r0 = r0.mDescription;
        r19 = r0;
        if (r19 == 0) goto L_0x000f;
    L_0x0008:
        r0 = r22;
        r0 = r0.mDescription;
        r19 = r0;
    L_0x000e:
        return r19;
    L_0x000f:
        r19 = "android.media.metadata.MEDIA_ID";
        r0 = r22;
        r1 = r19;
        r12 = r0.getString(r1);
        r19 = 3;
        r0 = r19;
        r0 = new java.lang.CharSequence[r0];
        r16 = r0;
        r8 = 0;
        r9 = 0;
        r19 = "android.media.metadata.DISPLAY_TITLE";
        r0 = r22;
        r1 = r19;
        r6 = r0.getText(r1);
        r19 = android.text.TextUtils.isEmpty(r6);
        if (r19 != 0) goto L_0x010c;
    L_0x0033:
        r19 = 0;
        r16[r19] = r6;
        r19 = 1;
        r20 = "android.media.metadata.DISPLAY_SUBTITLE";
        r0 = r22;
        r1 = r20;
        r20 = r0.getText(r1);
        r16[r19] = r20;
        r19 = 2;
        r20 = "android.media.metadata.DISPLAY_DESCRIPTION";
        r0 = r22;
        r1 = r20;
        r20 = r0.getText(r1);
        r16[r19] = r20;
    L_0x0053:
        r7 = 0;
    L_0x0054:
        r19 = PREFERRED_BITMAP_ORDER;
        r0 = r19;
        r0 = r0.length;
        r19 = r0;
        r0 = r19;
        if (r7 >= r0) goto L_0x006e;
    L_0x005f:
        r19 = PREFERRED_BITMAP_ORDER;
        r19 = r19[r7];
        r0 = r22;
        r1 = r19;
        r15 = r0.getBitmap(r1);
        if (r15 == 0) goto L_0x0141;
    L_0x006d:
        r8 = r15;
    L_0x006e:
        r7 = 0;
    L_0x006f:
        r19 = PREFERRED_URI_ORDER;
        r0 = r19;
        r0 = r0.length;
        r19 = r0;
        r0 = r19;
        if (r7 >= r0) goto L_0x0090;
    L_0x007a:
        r19 = PREFERRED_URI_ORDER;
        r19 = r19[r7];
        r0 = r22;
        r1 = r19;
        r15 = r0.getString(r1);
        r19 = android.text.TextUtils.isEmpty(r15);
        if (r19 != 0) goto L_0x0145;
    L_0x008c:
        r9 = android.net.Uri.parse(r15);
    L_0x0090:
        r13 = 0;
        r19 = "android.media.metadata.MEDIA_URI";
        r0 = r22;
        r1 = r19;
        r14 = r0.getString(r1);
        r19 = android.text.TextUtils.isEmpty(r14);
        if (r19 != 0) goto L_0x00a5;
    L_0x00a1:
        r13 = android.net.Uri.parse(r14);
    L_0x00a5:
        r4 = new android.support.v4.media.MediaDescriptionCompat$Builder;
        r4.<init>();
        r4.setMediaId(r12);
        r19 = 0;
        r19 = r16[r19];
        r0 = r19;
        r4.setTitle(r0);
        r19 = 1;
        r19 = r16[r19];
        r0 = r19;
        r4.setSubtitle(r0);
        r19 = 2;
        r19 = r16[r19];
        r0 = r19;
        r4.setDescription(r0);
        r4.setIconBitmap(r8);
        r4.setIconUri(r9);
        r4.setMediaUri(r13);
        r0 = r22;
        r0 = r0.mBundle;
        r19 = r0;
        r20 = "android.media.metadata.BT_FOLDER_TYPE";
        r19 = r19.containsKey(r20);
        if (r19 == 0) goto L_0x00fa;
    L_0x00df:
        r5 = new android.os.Bundle;
        r5.<init>();
        r19 = "android.media.extra.BT_FOLDER_TYPE";
        r20 = "android.media.metadata.BT_FOLDER_TYPE";
        r0 = r22;
        r1 = r20;
        r20 = r0.getLong(r1);
        r0 = r19;
        r1 = r20;
        r5.putLong(r0, r1);
        r4.setExtras(r5);
    L_0x00fa:
        r19 = r4.build();
        r0 = r19;
        r1 = r22;
        r1.mDescription = r0;
        r0 = r22;
        r0 = r0.mDescription;
        r19 = r0;
        goto L_0x000e;
    L_0x010c:
        r17 = 0;
        r10 = 0;
    L_0x010f:
        r0 = r16;
        r0 = r0.length;
        r19 = r0;
        r0 = r17;
        r1 = r19;
        if (r0 >= r1) goto L_0x0053;
    L_0x011a:
        r19 = PREFERRED_DESCRIPTION_ORDER;
        r0 = r19;
        r0 = r0.length;
        r19 = r0;
        r0 = r19;
        if (r10 >= r0) goto L_0x0053;
    L_0x0125:
        r19 = PREFERRED_DESCRIPTION_ORDER;
        r11 = r10 + 1;
        r19 = r19[r10];
        r0 = r22;
        r1 = r19;
        r15 = r0.getText(r1);
        r19 = android.text.TextUtils.isEmpty(r15);
        if (r19 != 0) goto L_0x013f;
    L_0x0139:
        r18 = r17 + 1;
        r16[r17] = r15;
        r17 = r18;
    L_0x013f:
        r10 = r11;
        goto L_0x010f;
    L_0x0141:
        r7 = r7 + 1;
        goto L_0x0054;
    L_0x0145:
        r7 = r7 + 1;
        goto L_0x006f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.media.MediaMetadataCompat.getDescription():android.support.v4.media.MediaDescriptionCompat");
    }

    public int describeContents() {
        return METADATA_TYPE_LONG;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mBundle);
    }

    public int size() {
        return this.mBundle.size();
    }

    public Set<String> keySet() {
        return this.mBundle.keySet();
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public static MediaMetadataCompat fromMediaMetadata(Object metadataObj) {
        if (metadataObj == null || VERSION.SDK_INT < 21) {
            return null;
        }
        Parcel p = Parcel.obtain();
        MediaMetadataCompatApi21.writeToParcel(metadataObj, p, METADATA_TYPE_LONG);
        p.setDataPosition(METADATA_TYPE_LONG);
        MediaMetadataCompat metadata = (MediaMetadataCompat) CREATOR.createFromParcel(p);
        p.recycle();
        metadata.mMetadataObj = metadataObj;
        return metadata;
    }

    public Object getMediaMetadata() {
        if (this.mMetadataObj != null || VERSION.SDK_INT < 21) {
            return this.mMetadataObj;
        }
        Parcel p = Parcel.obtain();
        writeToParcel(p, METADATA_TYPE_LONG);
        p.setDataPosition(METADATA_TYPE_LONG);
        this.mMetadataObj = MediaMetadataCompatApi21.createFromParcel(p);
        p.recycle();
        return this.mMetadataObj;
    }
}
