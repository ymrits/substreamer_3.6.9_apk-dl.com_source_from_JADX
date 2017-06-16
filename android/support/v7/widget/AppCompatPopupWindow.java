package android.support.v7.widget;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StyleRes;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.appcompat.C0136R;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.PopupWindow;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

class AppCompatPopupWindow extends PopupWindow {
    private static final boolean COMPAT_OVERLAP_ANCHOR;
    private static final String TAG = "AppCompatPopupWindow";
    private boolean mOverlapAnchor;

    /* renamed from: android.support.v7.widget.AppCompatPopupWindow.1 */
    static class C01501 implements OnScrollChangedListener {
        final /* synthetic */ Field val$fieldAnchor;
        final /* synthetic */ OnScrollChangedListener val$originalListener;
        final /* synthetic */ PopupWindow val$popup;

        C01501(Field field, PopupWindow popupWindow, OnScrollChangedListener onScrollChangedListener) {
            this.val$fieldAnchor = field;
            this.val$popup = popupWindow;
            this.val$originalListener = onScrollChangedListener;
        }

        public void onScrollChanged() {
            try {
                WeakReference<View> mAnchor = (WeakReference) this.val$fieldAnchor.get(this.val$popup);
                if (mAnchor != null && mAnchor.get() != null) {
                    this.val$originalListener.onScrollChanged();
                }
            } catch (IllegalAccessException e) {
            }
        }
    }

    static {
        COMPAT_OVERLAP_ANCHOR = VERSION.SDK_INT < 21 ? true : COMPAT_OVERLAP_ANCHOR;
    }

    public AppCompatPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public AppCompatPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, C0136R.styleable.PopupWindow, defStyleAttr, defStyleRes);
        if (a.hasValue(C0136R.styleable.PopupWindow_overlapAnchor)) {
            setSupportOverlapAnchor(a.getBoolean(C0136R.styleable.PopupWindow_overlapAnchor, COMPAT_OVERLAP_ANCHOR));
        }
        setBackgroundDrawable(a.getDrawable(C0136R.styleable.PopupWindow_android_popupBackground));
        int sdk = VERSION.SDK_INT;
        if (defStyleRes != 0 && sdk < 11 && a.hasValue(C0136R.styleable.PopupWindow_android_popupAnimationStyle)) {
            setAnimationStyle(a.getResourceId(C0136R.styleable.PopupWindow_android_popupAnimationStyle, -1));
        }
        a.recycle();
        if (VERSION.SDK_INT < 14) {
            wrapOnScrollChangedListener(this);
        }
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (COMPAT_OVERLAP_ANCHOR && this.mOverlapAnchor) {
            yoff -= anchor.getHeight();
        }
        super.showAsDropDown(anchor, xoff, yoff);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (COMPAT_OVERLAP_ANCHOR && this.mOverlapAnchor) {
            yoff -= anchor.getHeight();
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    public void update(View anchor, int xoff, int yoff, int width, int height) {
        if (COMPAT_OVERLAP_ANCHOR && this.mOverlapAnchor) {
            yoff -= anchor.getHeight();
        }
        super.update(anchor, xoff, yoff, width, height);
    }

    private static void wrapOnScrollChangedListener(PopupWindow popup) {
        try {
            Field fieldAnchor = PopupWindow.class.getDeclaredField("mAnchor");
            fieldAnchor.setAccessible(true);
            Field fieldListener = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
            fieldListener.setAccessible(true);
            fieldListener.set(popup, new C01501(fieldAnchor, popup, (OnScrollChangedListener) fieldListener.get(popup)));
        } catch (Exception e) {
            Log.d(TAG, "Exception while installing workaround OnScrollChangedListener", e);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setSupportOverlapAnchor(boolean overlapAnchor) {
        if (COMPAT_OVERLAP_ANCHOR) {
            this.mOverlapAnchor = overlapAnchor;
        } else {
            PopupWindowCompat.setOverlapAnchor(this, overlapAnchor);
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean getSupportOverlapAnchor() {
        if (COMPAT_OVERLAP_ANCHOR) {
            return this.mOverlapAnchor;
        }
        return PopupWindowCompat.getOverlapAnchor(this);
    }
}
