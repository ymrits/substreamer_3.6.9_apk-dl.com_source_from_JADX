package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StyleRes;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.appcompat.C0136R;
import android.support.v7.view.menu.ShowableListMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.ghenry22.substream2.C0173R;
import io.liteglue.SQLiteNative;
import java.lang.reflect.Method;
import org.apache.cordova.media.AudioHandler;

public class ListPopupWindow implements ShowableListMenu {
    private static final boolean DEBUG = false;
    static final int EXPAND_LIST_TIMEOUT = 250;
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    public static final int MATCH_PARENT = -1;
    public static final int POSITION_PROMPT_ABOVE = 0;
    public static final int POSITION_PROMPT_BELOW = 1;
    private static final String TAG = "ListPopupWindow";
    public static final int WRAP_CONTENT = -2;
    private static Method sClipToWindowEnabledMethod;
    private static Method sGetMaxAvailableHeightMethod;
    private static Method sSetEpicenterBoundsMethod;
    private ListAdapter mAdapter;
    private Context mContext;
    private boolean mDropDownAlwaysVisible;
    private View mDropDownAnchorView;
    private int mDropDownGravity;
    private int mDropDownHeight;
    private int mDropDownHorizontalOffset;
    DropDownListView mDropDownList;
    private Drawable mDropDownListHighlight;
    private int mDropDownVerticalOffset;
    private boolean mDropDownVerticalOffsetSet;
    private int mDropDownWidth;
    private int mDropDownWindowLayoutType;
    private Rect mEpicenterBounds;
    private boolean mForceIgnoreOutsideTouch;
    final Handler mHandler;
    private final ListSelectorHider mHideSelector;
    private boolean mIsAnimatedFromAnchor;
    private OnItemClickListener mItemClickListener;
    private OnItemSelectedListener mItemSelectedListener;
    int mListItemExpandMaximum;
    private boolean mModal;
    private DataSetObserver mObserver;
    PopupWindow mPopup;
    private int mPromptPosition;
    private View mPromptView;
    final ResizePopupRunnable mResizePopupRunnable;
    private final PopupScrollListener mScrollListener;
    private Runnable mShowDropDownRunnable;
    private final Rect mTempRect;
    private final PopupTouchInterceptor mTouchInterceptor;

    /* renamed from: android.support.v7.widget.ListPopupWindow.2 */
    class C01562 implements Runnable {
        C01562() {
        }

        public void run() {
            View view = ListPopupWindow.this.getAnchorView();
            if (view != null && view.getWindowToken() != null) {
                ListPopupWindow.this.show();
            }
        }
    }

    /* renamed from: android.support.v7.widget.ListPopupWindow.3 */
    class C01573 implements OnItemSelectedListener {
        C01573() {
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if (position != ListPopupWindow.MATCH_PARENT) {
                DropDownListView dropDownList = ListPopupWindow.this.mDropDownList;
                if (dropDownList != null) {
                    dropDownList.setListSelectionHidden(ListPopupWindow.DEBUG);
                }
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private class ListSelectorHider implements Runnable {
        ListSelectorHider() {
        }

        public void run() {
            ListPopupWindow.this.clearListSelection();
        }
    }

    private class PopupDataSetObserver extends DataSetObserver {
        PopupDataSetObserver() {
        }

        public void onChanged() {
            if (ListPopupWindow.this.isShowing()) {
                ListPopupWindow.this.show();
            }
        }

        public void onInvalidated() {
            ListPopupWindow.this.dismiss();
        }
    }

    private class PopupScrollListener implements OnScrollListener {
        PopupScrollListener() {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == ListPopupWindow.POSITION_PROMPT_BELOW && !ListPopupWindow.this.isInputMethodNotNeeded() && ListPopupWindow.this.mPopup.getContentView() != null) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
                ListPopupWindow.this.mResizePopupRunnable.run();
            }
        }
    }

    private class PopupTouchInterceptor implements OnTouchListener {
        PopupTouchInterceptor() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (action == 0 && ListPopupWindow.this.mPopup != null && ListPopupWindow.this.mPopup.isShowing() && x >= 0 && x < ListPopupWindow.this.mPopup.getWidth() && y >= 0 && y < ListPopupWindow.this.mPopup.getHeight()) {
                ListPopupWindow.this.mHandler.postDelayed(ListPopupWindow.this.mResizePopupRunnable, 250);
            } else if (action == ListPopupWindow.POSITION_PROMPT_BELOW) {
                ListPopupWindow.this.mHandler.removeCallbacks(ListPopupWindow.this.mResizePopupRunnable);
            }
            return ListPopupWindow.DEBUG;
        }
    }

    private class ResizePopupRunnable implements Runnable {
        ResizePopupRunnable() {
        }

        public void run() {
            if (ListPopupWindow.this.mDropDownList != null && ViewCompat.isAttachedToWindow(ListPopupWindow.this.mDropDownList) && ListPopupWindow.this.mDropDownList.getCount() > ListPopupWindow.this.mDropDownList.getChildCount() && ListPopupWindow.this.mDropDownList.getChildCount() <= ListPopupWindow.this.mListItemExpandMaximum) {
                ListPopupWindow.this.mPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                ListPopupWindow.this.show();
            }
        }
    }

    /* renamed from: android.support.v7.widget.ListPopupWindow.1 */
    class C03171 extends ForwardingListener {
        C03171(View src) {
            super(src);
        }

        public ListPopupWindow getPopup() {
            return ListPopupWindow.this;
        }
    }

    static {
        try {
            Class[] clsArr = new Class[POSITION_PROMPT_BELOW];
            clsArr[POSITION_PROMPT_ABOVE] = Boolean.TYPE;
            sClipToWindowEnabledMethod = PopupWindow.class.getDeclaredMethod("setClipToScreenEnabled", clsArr);
        } catch (NoSuchMethodException e) {
            Log.i(TAG, "Could not find method setClipToScreenEnabled() on PopupWindow. Oh well.");
        }
        try {
            sGetMaxAvailableHeightMethod = PopupWindow.class.getDeclaredMethod("getMaxAvailableHeight", new Class[]{View.class, Integer.TYPE, Boolean.TYPE});
        } catch (NoSuchMethodException e2) {
            Log.i(TAG, "Could not find method getMaxAvailableHeight(View, int, boolean) on PopupWindow. Oh well.");
        }
        try {
            clsArr = new Class[POSITION_PROMPT_BELOW];
            clsArr[POSITION_PROMPT_ABOVE] = Rect.class;
            sSetEpicenterBoundsMethod = PopupWindow.class.getDeclaredMethod("setEpicenterBounds", clsArr);
        } catch (NoSuchMethodException e3) {
            Log.i(TAG, "Could not find method setEpicenterBounds(Rect) on PopupWindow. Oh well.");
        }
    }

    public ListPopupWindow(@NonNull Context context) {
        this(context, null, C0136R.attr.listPopupWindowStyle);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, C0136R.attr.listPopupWindowStyle);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, POSITION_PROMPT_ABOVE);
    }

    public ListPopupWindow(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        this.mDropDownHeight = WRAP_CONTENT;
        this.mDropDownWidth = WRAP_CONTENT;
        this.mDropDownWindowLayoutType = PointerIconCompat.TYPE_HAND;
        this.mIsAnimatedFromAnchor = true;
        this.mDropDownGravity = POSITION_PROMPT_ABOVE;
        this.mDropDownAlwaysVisible = DEBUG;
        this.mForceIgnoreOutsideTouch = DEBUG;
        this.mListItemExpandMaximum = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        this.mPromptPosition = POSITION_PROMPT_ABOVE;
        this.mResizePopupRunnable = new ResizePopupRunnable();
        this.mTouchInterceptor = new PopupTouchInterceptor();
        this.mScrollListener = new PopupScrollListener();
        this.mHideSelector = new ListSelectorHider();
        this.mTempRect = new Rect();
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        TypedArray a = context.obtainStyledAttributes(attrs, C0136R.styleable.ListPopupWindow, defStyleAttr, defStyleRes);
        this.mDropDownHorizontalOffset = a.getDimensionPixelOffset(C0136R.styleable.ListPopupWindow_android_dropDownHorizontalOffset, POSITION_PROMPT_ABOVE);
        this.mDropDownVerticalOffset = a.getDimensionPixelOffset(C0136R.styleable.ListPopupWindow_android_dropDownVerticalOffset, POSITION_PROMPT_ABOVE);
        if (this.mDropDownVerticalOffset != 0) {
            this.mDropDownVerticalOffsetSet = true;
        }
        a.recycle();
        if (VERSION.SDK_INT >= 11) {
            this.mPopup = new AppCompatPopupWindow(context, attrs, defStyleAttr, defStyleRes);
        } else {
            this.mPopup = new AppCompatPopupWindow(context, attrs, defStyleAttr);
        }
        this.mPopup.setInputMethodMode(POSITION_PROMPT_BELOW);
    }

    public void setAdapter(@Nullable ListAdapter adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver();
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            adapter.registerDataSetObserver(this.mObserver);
        }
        if (this.mDropDownList != null) {
            this.mDropDownList.setAdapter(this.mAdapter);
        }
    }

    public void setPromptPosition(int position) {
        this.mPromptPosition = position;
    }

    public int getPromptPosition() {
        return this.mPromptPosition;
    }

    public void setModal(boolean modal) {
        this.mModal = modal;
        this.mPopup.setFocusable(modal);
    }

    public boolean isModal() {
        return this.mModal;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        this.mForceIgnoreOutsideTouch = forceIgnoreOutsideTouch;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        this.mDropDownAlwaysVisible = dropDownAlwaysVisible;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean isDropDownAlwaysVisible() {
        return this.mDropDownAlwaysVisible;
    }

    public void setSoftInputMode(int mode) {
        this.mPopup.setSoftInputMode(mode);
    }

    public int getSoftInputMode() {
        return this.mPopup.getSoftInputMode();
    }

    public void setListSelector(Drawable selector) {
        this.mDropDownListHighlight = selector;
    }

    @Nullable
    public Drawable getBackground() {
        return this.mPopup.getBackground();
    }

    public void setBackgroundDrawable(@Nullable Drawable d) {
        this.mPopup.setBackgroundDrawable(d);
    }

    public void setAnimationStyle(@StyleRes int animationStyle) {
        this.mPopup.setAnimationStyle(animationStyle);
    }

    @StyleRes
    public int getAnimationStyle() {
        return this.mPopup.getAnimationStyle();
    }

    @Nullable
    public View getAnchorView() {
        return this.mDropDownAnchorView;
    }

    public void setAnchorView(@Nullable View anchor) {
        this.mDropDownAnchorView = anchor;
    }

    public int getHorizontalOffset() {
        return this.mDropDownHorizontalOffset;
    }

    public void setHorizontalOffset(int offset) {
        this.mDropDownHorizontalOffset = offset;
    }

    public int getVerticalOffset() {
        if (this.mDropDownVerticalOffsetSet) {
            return this.mDropDownVerticalOffset;
        }
        return POSITION_PROMPT_ABOVE;
    }

    public void setVerticalOffset(int offset) {
        this.mDropDownVerticalOffset = offset;
        this.mDropDownVerticalOffsetSet = true;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    public void setDropDownGravity(int gravity) {
        this.mDropDownGravity = gravity;
    }

    public int getWidth() {
        return this.mDropDownWidth;
    }

    public void setWidth(int width) {
        this.mDropDownWidth = width;
    }

    public void setContentWidth(int width) {
        Drawable popupBackground = this.mPopup.getBackground();
        if (popupBackground != null) {
            popupBackground.getPadding(this.mTempRect);
            this.mDropDownWidth = (this.mTempRect.left + this.mTempRect.right) + width;
            return;
        }
        setWidth(width);
    }

    public int getHeight() {
        return this.mDropDownHeight;
    }

    public void setHeight(int height) {
        this.mDropDownHeight = height;
    }

    public void setWindowLayoutType(int layoutType) {
        this.mDropDownWindowLayoutType = layoutType;
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener clickListener) {
        this.mItemClickListener = clickListener;
    }

    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener selectedListener) {
        this.mItemSelectedListener = selectedListener;
    }

    public void setPromptView(@Nullable View prompt) {
        boolean showing = isShowing();
        if (showing) {
            removePromptView();
        }
        this.mPromptView = prompt;
        if (showing) {
            show();
        }
    }

    public void postShow() {
        this.mHandler.post(this.mShowDropDownRunnable);
    }

    public void show() {
        boolean z = true;
        boolean z2 = DEBUG;
        int i = MATCH_PARENT;
        int height = buildDropDown();
        boolean noInputMethod = isInputMethodNotNeeded();
        PopupWindowCompat.setWindowLayoutType(this.mPopup, this.mDropDownWindowLayoutType);
        int widthSpec;
        int heightSpec;
        if (this.mPopup.isShowing()) {
            int i2;
            if (this.mDropDownWidth == MATCH_PARENT) {
                widthSpec = MATCH_PARENT;
            } else if (this.mDropDownWidth == WRAP_CONTENT) {
                widthSpec = getAnchorView().getWidth();
            } else {
                widthSpec = this.mDropDownWidth;
            }
            if (this.mDropDownHeight == MATCH_PARENT) {
                if (noInputMethod) {
                    heightSpec = height;
                } else {
                    heightSpec = MATCH_PARENT;
                }
                PopupWindow popupWindow;
                int i3;
                if (noInputMethod) {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == MATCH_PARENT) {
                        i3 = MATCH_PARENT;
                    } else {
                        i3 = POSITION_PROMPT_ABOVE;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(POSITION_PROMPT_ABOVE);
                } else {
                    popupWindow = this.mPopup;
                    if (this.mDropDownWidth == MATCH_PARENT) {
                        i3 = MATCH_PARENT;
                    } else {
                        i3 = POSITION_PROMPT_ABOVE;
                    }
                    popupWindow.setWidth(i3);
                    this.mPopup.setHeight(MATCH_PARENT);
                }
            } else if (this.mDropDownHeight == WRAP_CONTENT) {
                heightSpec = height;
            } else {
                heightSpec = this.mDropDownHeight;
            }
            PopupWindow popupWindow2 = this.mPopup;
            if (!(this.mForceIgnoreOutsideTouch || this.mDropDownAlwaysVisible)) {
                z2 = true;
            }
            popupWindow2.setOutsideTouchable(z2);
            popupWindow2 = this.mPopup;
            View anchorView = getAnchorView();
            int i4 = this.mDropDownHorizontalOffset;
            int i5 = this.mDropDownVerticalOffset;
            if (widthSpec < 0) {
                i2 = MATCH_PARENT;
            } else {
                i2 = widthSpec;
            }
            if (heightSpec >= 0) {
                i = heightSpec;
            }
            popupWindow2.update(anchorView, i4, i5, i2, i);
            return;
        }
        if (this.mDropDownWidth == MATCH_PARENT) {
            widthSpec = MATCH_PARENT;
        } else if (this.mDropDownWidth == WRAP_CONTENT) {
            widthSpec = getAnchorView().getWidth();
        } else {
            widthSpec = this.mDropDownWidth;
        }
        if (this.mDropDownHeight == MATCH_PARENT) {
            heightSpec = MATCH_PARENT;
        } else if (this.mDropDownHeight == WRAP_CONTENT) {
            heightSpec = height;
        } else {
            heightSpec = this.mDropDownHeight;
        }
        this.mPopup.setWidth(widthSpec);
        this.mPopup.setHeight(heightSpec);
        setPopupClipToScreenEnabled(true);
        popupWindow2 = this.mPopup;
        if (this.mForceIgnoreOutsideTouch || this.mDropDownAlwaysVisible) {
            z = DEBUG;
        }
        popupWindow2.setOutsideTouchable(z);
        this.mPopup.setTouchInterceptor(this.mTouchInterceptor);
        if (sSetEpicenterBoundsMethod != null) {
            try {
                Method method = sSetEpicenterBoundsMethod;
                PopupWindow popupWindow3 = this.mPopup;
                Object[] objArr = new Object[POSITION_PROMPT_BELOW];
                objArr[POSITION_PROMPT_ABOVE] = this.mEpicenterBounds;
                method.invoke(popupWindow3, objArr);
            } catch (Exception e) {
                Log.e(TAG, "Could not invoke setEpicenterBounds on PopupWindow", e);
            }
        }
        PopupWindowCompat.showAsDropDown(this.mPopup, getAnchorView(), this.mDropDownHorizontalOffset, this.mDropDownVerticalOffset, this.mDropDownGravity);
        this.mDropDownList.setSelection(MATCH_PARENT);
        if (!this.mModal || this.mDropDownList.isInTouchMode()) {
            clearListSelection();
        }
        if (!this.mModal) {
            this.mHandler.post(this.mHideSelector);
        }
    }

    public void dismiss() {
        this.mPopup.dismiss();
        removePromptView();
        this.mPopup.setContentView(null);
        this.mDropDownList = null;
        this.mHandler.removeCallbacks(this.mResizePopupRunnable);
    }

    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        this.mPopup.setOnDismissListener(listener);
    }

    private void removePromptView() {
        if (this.mPromptView != null) {
            ViewParent parent = this.mPromptView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mPromptView);
            }
        }
    }

    public void setInputMethodMode(int mode) {
        this.mPopup.setInputMethodMode(mode);
    }

    public int getInputMethodMode() {
        return this.mPopup.getInputMethodMode();
    }

    public void setSelection(int position) {
        DropDownListView list = this.mDropDownList;
        if (isShowing() && list != null) {
            list.setListSelectionHidden(DEBUG);
            list.setSelection(position);
            if (VERSION.SDK_INT >= 11 && list.getChoiceMode() != 0) {
                list.setItemChecked(position, true);
            }
        }
    }

    public void clearListSelection() {
        DropDownListView list = this.mDropDownList;
        if (list != null) {
            list.setListSelectionHidden(true);
            list.requestLayout();
        }
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == INPUT_METHOD_NOT_NEEDED ? true : DEBUG;
    }

    public boolean performItemClick(int position) {
        if (!isShowing()) {
            return DEBUG;
        }
        if (this.mItemClickListener != null) {
            DropDownListView list = this.mDropDownList;
            int i = position;
            this.mItemClickListener.onItemClick(list, list.getChildAt(position - list.getFirstVisiblePosition()), i, list.getAdapter().getItemId(position));
        }
        return true;
    }

    @Nullable
    public Object getSelectedItem() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItem();
        }
        return null;
    }

    public int getSelectedItemPosition() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItemPosition();
        }
        return MATCH_PARENT;
    }

    public long getSelectedItemId() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedItemId();
        }
        return Long.MIN_VALUE;
    }

    @Nullable
    public View getSelectedView() {
        if (isShowing()) {
            return this.mDropDownList.getSelectedView();
        }
        return null;
    }

    @Nullable
    public ListView getListView() {
        return this.mDropDownList;
    }

    @NonNull
    DropDownListView createDropDownListView(Context context, boolean hijackFocus) {
        return new DropDownListView(context, hijackFocus);
    }

    void setListItemExpandMax(int max) {
        this.mListItemExpandMaximum = max;
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (isShowing() && keyCode != 62 && (this.mDropDownList.getSelectedItemPosition() >= 0 || !isConfirmKey(keyCode))) {
            boolean below;
            int curIndex = this.mDropDownList.getSelectedItemPosition();
            if (this.mPopup.isAboveAnchor()) {
                below = DEBUG;
            } else {
                below = true;
            }
            ListAdapter adapter = this.mAdapter;
            int firstItem = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            int lastItem = ExploreByTouchHelper.INVALID_ID;
            if (adapter != null) {
                boolean allEnabled = adapter.areAllItemsEnabled();
                if (allEnabled) {
                    firstItem = POSITION_PROMPT_ABOVE;
                } else {
                    firstItem = this.mDropDownList.lookForSelectablePosition(POSITION_PROMPT_ABOVE, true);
                }
                if (allEnabled) {
                    lastItem = adapter.getCount() + MATCH_PARENT;
                } else {
                    lastItem = this.mDropDownList.lookForSelectablePosition(adapter.getCount() + MATCH_PARENT, DEBUG);
                }
            }
            if (!(below && keyCode == 19 && curIndex <= firstItem) && (below || keyCode != 20 || curIndex < lastItem)) {
                this.mDropDownList.setListSelectionHidden(DEBUG);
                if (this.mDropDownList.onKeyDown(keyCode, event)) {
                    this.mPopup.setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
                    this.mDropDownList.requestFocusFromTouch();
                    show();
                    switch (keyCode) {
                        case SQLiteNative.SQLC_RESULT_CONSTRAINT /*19*/:
                        case AudioHandler.PERMISSION_DENIED_ERROR /*20*/:
                        case C0173R.styleable.Toolbar_collapseContentDescription /*23*/:
                        case C0173R.styleable.AppCompatTheme_textAppearanceSearchResultTitle /*66*/:
                            return true;
                    }
                } else if (below && keyCode == 20) {
                    if (curIndex == lastItem) {
                        return true;
                    }
                } else if (!below && keyCode == 19 && curIndex == firstItem) {
                    return true;
                }
            }
            clearListSelection();
            this.mPopup.setInputMethodMode(POSITION_PROMPT_BELOW);
            show();
            return true;
        }
        return DEBUG;
    }

    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (!isShowing() || this.mDropDownList.getSelectedItemPosition() < 0) {
            return DEBUG;
        }
        boolean consumed = this.mDropDownList.onKeyUp(keyCode, event);
        if (!consumed || !isConfirmKey(keyCode)) {
            return consumed;
        }
        dismiss();
        return consumed;
    }

    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == 4 && isShowing()) {
            View anchorView = this.mDropDownAnchorView;
            DispatcherState state;
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                state = anchorView.getKeyDispatcherState();
                if (state == null) {
                    return true;
                }
                state.startTracking(event, this);
                return true;
            } else if (event.getAction() == POSITION_PROMPT_BELOW) {
                state = anchorView.getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismiss();
                    return true;
                }
            }
        }
        return DEBUG;
    }

    public OnTouchListener createDragToOpenListener(View src) {
        return new C03171(src);
    }

    private int buildDropDown() {
        int padding;
        int otherHeights = POSITION_PROMPT_ABOVE;
        ViewGroup dropDownView;
        LayoutParams hintParams;
        if (this.mDropDownList == null) {
            Context context = this.mContext;
            this.mShowDropDownRunnable = new C01562();
            this.mDropDownList = createDropDownListView(context, !this.mModal ? true : DEBUG);
            if (this.mDropDownListHighlight != null) {
                this.mDropDownList.setSelector(this.mDropDownListHighlight);
            }
            this.mDropDownList.setAdapter(this.mAdapter);
            this.mDropDownList.setOnItemClickListener(this.mItemClickListener);
            this.mDropDownList.setFocusable(true);
            this.mDropDownList.setFocusableInTouchMode(true);
            this.mDropDownList.setOnItemSelectedListener(new C01573());
            this.mDropDownList.setOnScrollListener(this.mScrollListener);
            if (this.mItemSelectedListener != null) {
                this.mDropDownList.setOnItemSelectedListener(this.mItemSelectedListener);
            }
            dropDownView = this.mDropDownList;
            View hintView = this.mPromptView;
            if (hintView != null) {
                int widthMode;
                int widthSize;
                ViewGroup hintContainer = new LinearLayout(context);
                hintContainer.setOrientation(POSITION_PROMPT_BELOW);
                hintParams = new LayoutParams(MATCH_PARENT, POSITION_PROMPT_ABOVE, 1.0f);
                switch (this.mPromptPosition) {
                    case POSITION_PROMPT_ABOVE /*0*/:
                        hintContainer.addView(hintView);
                        hintContainer.addView(dropDownView, hintParams);
                        break;
                    case POSITION_PROMPT_BELOW /*1*/:
                        hintContainer.addView(dropDownView, hintParams);
                        hintContainer.addView(hintView);
                        break;
                    default:
                        Log.e(TAG, "Invalid hint position " + this.mPromptPosition);
                        break;
                }
                if (this.mDropDownWidth >= 0) {
                    widthMode = ExploreByTouchHelper.INVALID_ID;
                    widthSize = this.mDropDownWidth;
                } else {
                    widthMode = POSITION_PROMPT_ABOVE;
                    widthSize = POSITION_PROMPT_ABOVE;
                }
                hintView.measure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), POSITION_PROMPT_ABOVE);
                hintParams = (LayoutParams) hintView.getLayoutParams();
                otherHeights = (hintView.getMeasuredHeight() + hintParams.topMargin) + hintParams.bottomMargin;
                dropDownView = hintContainer;
            }
            this.mPopup.setContentView(dropDownView);
        } else {
            dropDownView = (ViewGroup) this.mPopup.getContentView();
            View view = this.mPromptView;
            if (view != null) {
                hintParams = (LayoutParams) view.getLayoutParams();
                otherHeights = (view.getMeasuredHeight() + hintParams.topMargin) + hintParams.bottomMargin;
            }
        }
        Drawable background = this.mPopup.getBackground();
        if (background != null) {
            background.getPadding(this.mTempRect);
            padding = this.mTempRect.top + this.mTempRect.bottom;
            if (!this.mDropDownVerticalOffsetSet) {
                this.mDropDownVerticalOffset = -this.mTempRect.top;
            }
        } else {
            this.mTempRect.setEmpty();
            padding = POSITION_PROMPT_ABOVE;
        }
        int maxHeight = getMaxAvailableHeight(getAnchorView(), this.mDropDownVerticalOffset, this.mPopup.getInputMethodMode() == INPUT_METHOD_NOT_NEEDED ? true : DEBUG);
        if (this.mDropDownAlwaysVisible || this.mDropDownHeight == MATCH_PARENT) {
            return maxHeight + padding;
        }
        int childWidthSpec;
        switch (this.mDropDownWidth) {
            case WRAP_CONTENT /*-2*/:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), ExploreByTouchHelper.INVALID_ID);
                break;
            case MATCH_PARENT /*-1*/:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mContext.getResources().getDisplayMetrics().widthPixels - (this.mTempRect.left + this.mTempRect.right), 1073741824);
                break;
            default:
                childWidthSpec = MeasureSpec.makeMeasureSpec(this.mDropDownWidth, 1073741824);
                break;
        }
        int listContent = this.mDropDownList.measureHeightOfChildrenCompat(childWidthSpec, POSITION_PROMPT_ABOVE, MATCH_PARENT, maxHeight - otherHeights, MATCH_PARENT);
        if (listContent > 0) {
            otherHeights += padding + (this.mDropDownList.getPaddingTop() + this.mDropDownList.getPaddingBottom());
        }
        return listContent + otherHeights;
    }

    private static boolean isConfirmKey(int keyCode) {
        return (keyCode == 66 || keyCode == 23) ? true : DEBUG;
    }

    private void setPopupClipToScreenEnabled(boolean clip) {
        if (sClipToWindowEnabledMethod != null) {
            try {
                Method method = sClipToWindowEnabledMethod;
                PopupWindow popupWindow = this.mPopup;
                Object[] objArr = new Object[POSITION_PROMPT_BELOW];
                objArr[POSITION_PROMPT_ABOVE] = Boolean.valueOf(clip);
                method.invoke(popupWindow, objArr);
            } catch (Exception e) {
                Log.i(TAG, "Could not call setClipToScreenEnabled() on PopupWindow. Oh well.");
            }
        }
    }

    private int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreBottomDecorations) {
        if (sGetMaxAvailableHeightMethod != null) {
            try {
                return ((Integer) sGetMaxAvailableHeightMethod.invoke(this.mPopup, new Object[]{anchor, Integer.valueOf(yOffset), Boolean.valueOf(ignoreBottomDecorations)})).intValue();
            } catch (Exception e) {
                Log.i(TAG, "Could not call getMaxAvailableHeightMethod(View, int, boolean) on PopupWindow. Using the public version.");
            }
        }
        return this.mPopup.getMaxAvailableHeight(anchor, yOffset);
    }
}
