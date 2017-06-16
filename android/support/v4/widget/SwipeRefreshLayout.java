package android.support.v4.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import org.apache.cordova.file.FileUtils;

public class SwipeRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private static final int ALPHA_ANIMATION_DURATION = 300;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private static final int CIRCLE_BG_LIGHT = -328966;
    @VisibleForTesting
    static final int CIRCLE_DIAMETER = 40;
    @VisibleForTesting
    static final int CIRCLE_DIAMETER_LARGE = 56;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0f;
    public static final int DEFAULT = 1;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final float DRAG_RATE = 0.5f;
    private static final int INVALID_POINTER = -1;
    public static final int LARGE = 0;
    private static final int[] LAYOUT_ATTRS;
    private static final String LOG_TAG;
    private static final int MAX_ALPHA = 255;
    private static final float MAX_PROGRESS_ANGLE = 0.8f;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final int STARTING_PROGRESS_ALPHA = 76;
    private int mActivePointerId;
    private Animation mAlphaMaxAnimation;
    private Animation mAlphaStartAnimation;
    private final Animation mAnimateToCorrectPosition;
    private final Animation mAnimateToStartPosition;
    private OnChildScrollUpCallback mChildScrollUpCallback;
    private int mCircleDiameter;
    CircleImageView mCircleView;
    private int mCircleViewIndex;
    int mCurrentTargetOffsetTop;
    private final DecelerateInterpolator mDecelerateInterpolator;
    protected int mFrom;
    private float mInitialDownY;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    OnRefreshListener mListener;
    private int mMediumAnimationDuration;
    private boolean mNestedScrollInProgress;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    boolean mNotify;
    protected int mOriginalOffsetTop;
    private final int[] mParentOffsetInWindow;
    private final int[] mParentScrollConsumed;
    MaterialProgressDrawable mProgress;
    private AnimationListener mRefreshListener;
    boolean mRefreshing;
    private boolean mReturningToStart;
    boolean mScale;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mScaleDownToStartAnimation;
    int mSpinnerOffsetEnd;
    float mStartingScale;
    private View mTarget;
    private float mTotalDragDistance;
    private float mTotalUnconsumed;
    private int mTouchSlop;
    boolean mUsingCustomStart;

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.1 */
    class C01111 implements AnimationListener {
        C01111() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        @SuppressLint({"NewApi"})
        public void onAnimationEnd(Animation animation) {
            if (SwipeRefreshLayout.this.mRefreshing) {
                SwipeRefreshLayout.this.mProgress.setAlpha(SwipeRefreshLayout.MAX_ALPHA);
                SwipeRefreshLayout.this.mProgress.start();
                if (SwipeRefreshLayout.this.mNotify && SwipeRefreshLayout.this.mListener != null) {
                    SwipeRefreshLayout.this.mListener.onRefresh();
                }
                SwipeRefreshLayout.this.mCurrentTargetOffsetTop = SwipeRefreshLayout.this.mCircleView.getTop();
                return;
            }
            SwipeRefreshLayout.this.reset();
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.2 */
    class C01122 extends Animation {
        C01122() {
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            SwipeRefreshLayout.this.setAnimationProgress(interpolatedTime);
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.3 */
    class C01133 extends Animation {
        C01133() {
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            SwipeRefreshLayout.this.setAnimationProgress(1.0f - interpolatedTime);
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.4 */
    class C01144 extends Animation {
        final /* synthetic */ int val$endingAlpha;
        final /* synthetic */ int val$startingAlpha;

        C01144(int i, int i2) {
            this.val$startingAlpha = i;
            this.val$endingAlpha = i2;
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            SwipeRefreshLayout.this.mProgress.setAlpha((int) (((float) this.val$startingAlpha) + (((float) (this.val$endingAlpha - this.val$startingAlpha)) * interpolatedTime)));
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.5 */
    class C01155 implements AnimationListener {
        C01155() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            if (!SwipeRefreshLayout.this.mScale) {
                SwipeRefreshLayout.this.startScaleDownAnimation(null);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.6 */
    class C01166 extends Animation {
        C01166() {
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            int endTarget;
            if (SwipeRefreshLayout.this.mUsingCustomStart) {
                endTarget = SwipeRefreshLayout.this.mSpinnerOffsetEnd;
            } else {
                endTarget = SwipeRefreshLayout.this.mSpinnerOffsetEnd - Math.abs(SwipeRefreshLayout.this.mOriginalOffsetTop);
            }
            SwipeRefreshLayout.this.setTargetOffsetTopAndBottom((SwipeRefreshLayout.this.mFrom + ((int) (((float) (endTarget - SwipeRefreshLayout.this.mFrom)) * interpolatedTime))) - SwipeRefreshLayout.this.mCircleView.getTop());
            SwipeRefreshLayout.this.mProgress.setArrowScale(1.0f - interpolatedTime);
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.7 */
    class C01177 extends Animation {
        C01177() {
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            SwipeRefreshLayout.this.moveToStart(interpolatedTime);
        }
    }

    /* renamed from: android.support.v4.widget.SwipeRefreshLayout.8 */
    class C01188 extends Animation {
        C01188() {
        }

        public void applyTransformation(float interpolatedTime, Transformation t) {
            SwipeRefreshLayout.this.setAnimationProgress(SwipeRefreshLayout.this.mStartingScale + ((-SwipeRefreshLayout.this.mStartingScale) * interpolatedTime));
            SwipeRefreshLayout.this.moveToStart(interpolatedTime);
        }
    }

    public interface OnChildScrollUpCallback {
        boolean canChildScrollUp(SwipeRefreshLayout swipeRefreshLayout, @Nullable View view);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    static {
        LOG_TAG = SwipeRefreshLayout.class.getSimpleName();
        int[] iArr = new int[DEFAULT];
        iArr[LARGE] = 16842766;
        LAYOUT_ATTRS = iArr;
    }

    void reset() {
        this.mCircleView.clearAnimation();
        this.mProgress.stop();
        this.mCircleView.setVisibility(8);
        setColorViewAlpha(MAX_ALPHA);
        if (this.mScale) {
            setAnimationProgress(0.0f);
        } else {
            setTargetOffsetTopAndBottom(this.mOriginalOffsetTop - this.mCurrentTargetOffsetTop);
        }
        this.mCurrentTargetOffsetTop = this.mCircleView.getTop();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    @SuppressLint({"NewApi"})
    private void setColorViewAlpha(int targetAlpha) {
        this.mCircleView.getBackground().setAlpha(targetAlpha);
        this.mProgress.setAlpha(targetAlpha);
    }

    public void setProgressViewOffset(boolean scale, int start, int end) {
        this.mScale = scale;
        this.mOriginalOffsetTop = start;
        this.mSpinnerOffsetEnd = end;
        this.mUsingCustomStart = true;
        reset();
        this.mRefreshing = false;
    }

    public int getProgressViewStartOffset() {
        return this.mOriginalOffsetTop;
    }

    public int getProgressViewEndOffset() {
        return this.mSpinnerOffsetEnd;
    }

    public void setProgressViewEndTarget(boolean scale, int end) {
        this.mSpinnerOffsetEnd = end;
        this.mScale = scale;
        this.mCircleView.invalidate();
    }

    public void setSize(int size) {
        if (size == 0 || size == DEFAULT) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            if (size == 0) {
                this.mCircleDiameter = (int) (56.0f * metrics.density);
            } else {
                this.mCircleDiameter = (int) (40.0f * metrics.density);
            }
            this.mCircleView.setImageDrawable(null);
            this.mProgress.updateSizes(size);
            this.mCircleView.setImageDrawable(this.mProgress);
        }
    }

    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRefreshing = false;
        this.mTotalDragDistance = -1.0f;
        this.mParentScrollConsumed = new int[2];
        this.mParentOffsetInWindow = new int[2];
        this.mActivePointerId = INVALID_POINTER;
        this.mCircleViewIndex = INVALID_POINTER;
        this.mRefreshListener = new C01111();
        this.mAnimateToCorrectPosition = new C01166();
        this.mAnimateToStartPosition = new C01177();
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = getResources().getInteger(17694721);
        setWillNotDraw(false);
        this.mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mCircleDiameter = (int) (40.0f * metrics.density);
        createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        this.mSpinnerOffsetEnd = (int) (64.0f * metrics.density);
        this.mTotalDragDistance = (float) this.mSpinnerOffsetEnd;
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        this.mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        int i = -this.mCircleDiameter;
        this.mCurrentTargetOffsetTop = i;
        this.mOriginalOffsetTop = i;
        moveToStart(1.0f);
        TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(LARGE, true));
        a.recycle();
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        if (this.mCircleViewIndex < 0) {
            return i;
        }
        if (i == childCount + INVALID_POINTER) {
            return this.mCircleViewIndex;
        }
        if (i >= this.mCircleViewIndex) {
            return i + DEFAULT;
        }
        return i;
    }

    private void createProgressView() {
        this.mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT);
        this.mProgress = new MaterialProgressDrawable(getContext(), this);
        this.mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        this.mCircleView.setImageDrawable(this.mProgress);
        this.mCircleView.setVisibility(8);
        addView(this.mCircleView);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (!refreshing || this.mRefreshing == refreshing) {
            setRefreshing(refreshing, false);
            return;
        }
        int endTarget;
        this.mRefreshing = refreshing;
        if (this.mUsingCustomStart) {
            endTarget = this.mSpinnerOffsetEnd;
        } else {
            endTarget = this.mSpinnerOffsetEnd + this.mOriginalOffsetTop;
        }
        setTargetOffsetTopAndBottom(endTarget - this.mCurrentTargetOffsetTop);
        this.mNotify = false;
        startScaleUpAnimation(this.mRefreshListener);
    }

    @SuppressLint({"NewApi"})
    private void startScaleUpAnimation(AnimationListener listener) {
        this.mCircleView.setVisibility(LARGE);
        if (VERSION.SDK_INT >= 11) {
            this.mProgress.setAlpha(MAX_ALPHA);
        }
        this.mScaleAnimation = new C01122();
        this.mScaleAnimation.setDuration((long) this.mMediumAnimationDuration);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleAnimation);
    }

    void setAnimationProgress(float progress) {
        this.mCircleView.setScaleX(progress);
        this.mCircleView.setScaleY(progress);
    }

    private void setRefreshing(boolean refreshing, boolean notify) {
        if (this.mRefreshing != refreshing) {
            this.mNotify = notify;
            ensureTarget();
            this.mRefreshing = refreshing;
            if (this.mRefreshing) {
                animateOffsetToCorrectPosition(this.mCurrentTargetOffsetTop, this.mRefreshListener);
            } else {
                startScaleDownAnimation(this.mRefreshListener);
            }
        }
    }

    void startScaleDownAnimation(AnimationListener listener) {
        this.mScaleDownAnimation = new C01133();
        this.mScaleDownAnimation.setDuration(150);
        this.mCircleView.setAnimationListener(listener);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownAnimation);
    }

    private void startProgressAlphaStartAnimation() {
        this.mAlphaStartAnimation = startAlphaAnimation(this.mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
    }

    private void startProgressAlphaMaxAnimation() {
        this.mAlphaMaxAnimation = startAlphaAnimation(this.mProgress.getAlpha(), MAX_ALPHA);
    }

    private Animation startAlphaAnimation(int startingAlpha, int endingAlpha) {
        Animation alpha = new C01144(startingAlpha, endingAlpha);
        alpha.setDuration(300);
        this.mCircleView.setAnimationListener(null);
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(alpha);
        return alpha;
    }

    @Deprecated
    public void setProgressBackgroundColor(int colorRes) {
        setProgressBackgroundColorSchemeResource(colorRes);
    }

    public void setProgressBackgroundColorSchemeResource(@ColorRes int colorRes) {
        setProgressBackgroundColorSchemeColor(ContextCompat.getColor(getContext(), colorRes));
    }

    public void setProgressBackgroundColorSchemeColor(@ColorInt int color) {
        this.mCircleView.setBackgroundColor(color);
        this.mProgress.setBackgroundColor(color);
    }

    @Deprecated
    public void setColorScheme(@ColorInt int... colors) {
        setColorSchemeResources(colors);
    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = LARGE; i < colorResIds.length; i += DEFAULT) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        ensureTarget();
        this.mProgress.setColorSchemeColors(colors);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            int i = LARGE;
            while (i < getChildCount()) {
                View child = getChildAt(i);
                if (child.equals(this.mCircleView)) {
                    i += DEFAULT;
                } else {
                    this.mTarget = child;
                    return;
                }
            }
        }
    }

    public void setDistanceToTriggerSync(int distance) {
        this.mTotalDragDistance = (float) distance;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (getChildCount() != 0) {
            if (this.mTarget == null) {
                ensureTarget();
            }
            if (this.mTarget != null) {
                View child = this.mTarget;
                int childLeft = getPaddingLeft();
                int childTop = getPaddingTop();
                child.layout(childLeft, childTop, childLeft + ((width - getPaddingLeft()) - getPaddingRight()), childTop + ((height - getPaddingTop()) - getPaddingBottom()));
                int circleWidth = this.mCircleView.getMeasuredWidth();
                this.mCircleView.layout((width / 2) - (circleWidth / 2), this.mCurrentTargetOffsetTop, (width / 2) + (circleWidth / 2), this.mCurrentTargetOffsetTop + this.mCircleView.getMeasuredHeight());
            }
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mTarget == null) {
            ensureTarget();
        }
        if (this.mTarget != null) {
            this.mTarget.measure(MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
            this.mCircleView.measure(MeasureSpec.makeMeasureSpec(this.mCircleDiameter, 1073741824), MeasureSpec.makeMeasureSpec(this.mCircleDiameter, 1073741824));
            this.mCircleViewIndex = INVALID_POINTER;
            for (int index = LARGE; index < getChildCount(); index += DEFAULT) {
                if (getChildAt(index) == this.mCircleView) {
                    this.mCircleViewIndex = index;
                    return;
                }
            }
        }
    }

    public int getProgressCircleDiameter() {
        return this.mCircleDiameter;
    }

    public boolean canChildScrollUp() {
        if (this.mChildScrollUpCallback != null) {
            return this.mChildScrollUpCallback.canChildScrollUp(this, this.mTarget);
        }
        return ViewCompat.canScrollVertically(this.mTarget, INVALID_POINTER);
    }

    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        this.mChildScrollUpCallback = callback;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        int action = ev.getActionMasked();
        if (this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }
        if (!isEnabled() || this.mReturningToStart || canChildScrollUp() || this.mRefreshing || this.mNestedScrollInProgress) {
            return false;
        }
        int pointerIndex;
        switch (action) {
            case LARGE /*0*/:
                setTargetOffsetTopAndBottom(this.mOriginalOffsetTop - this.mCircleView.getTop());
                this.mActivePointerId = ev.getPointerId(LARGE);
                this.mIsBeingDragged = false;
                pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex >= 0) {
                    this.mInitialDownY = ev.getY(pointerIndex);
                    break;
                }
                return false;
            case DEFAULT /*1*/:
            case FileUtils.WRITE /*3*/:
                this.mIsBeingDragged = false;
                this.mActivePointerId = INVALID_POINTER;
                break;
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                if (this.mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }
                pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex >= 0) {
                    startDragging(ev.getY(pointerIndex));
                    break;
                }
                return false;
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    public void requestDisallowInterceptTouchEvent(boolean b) {
        if (VERSION.SDK_INT < 21 && (this.mTarget instanceof AbsListView)) {
            return;
        }
        if (this.mTarget == null || ViewCompat.isNestedScrollingEnabled(this.mTarget)) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (!isEnabled() || this.mReturningToStart || this.mRefreshing || (nestedScrollAxes & 2) == 0) ? false : true;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & 2);
        this.mTotalUnconsumed = 0.0f;
        this.mNestedScrollInProgress = true;
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && this.mTotalUnconsumed > 0.0f) {
            if (((float) dy) > this.mTotalUnconsumed) {
                consumed[DEFAULT] = dy - ((int) this.mTotalUnconsumed);
                this.mTotalUnconsumed = 0.0f;
            } else {
                this.mTotalUnconsumed -= (float) dy;
                consumed[DEFAULT] = dy;
            }
            moveSpinner(this.mTotalUnconsumed);
        }
        if (this.mUsingCustomStart && dy > 0 && this.mTotalUnconsumed == 0.0f && Math.abs(dy - consumed[DEFAULT]) > 0) {
            this.mCircleView.setVisibility(8);
        }
        int[] parentConsumed = this.mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[LARGE], dy - consumed[DEFAULT], parentConsumed, null)) {
            consumed[LARGE] = consumed[LARGE] + parentConsumed[LARGE];
            consumed[DEFAULT] = consumed[DEFAULT] + parentConsumed[DEFAULT];
        }
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    public void onStopNestedScroll(View target) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(target);
        this.mNestedScrollInProgress = false;
        if (this.mTotalUnconsumed > 0.0f) {
            finishSpinner(this.mTotalUnconsumed);
            this.mTotalUnconsumed = 0.0f;
        }
        stopNestedScroll();
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, this.mParentOffsetInWindow);
        int dy = dyUnconsumed + this.mParentOffsetInWindow[DEFAULT];
        if (dy < 0 && !canChildScrollUp()) {
            this.mTotalUnconsumed += (float) Math.abs(dy);
            moveSpinner(this.mTotalUnconsumed);
        }
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        this.mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return this.mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return this.mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        this.mNestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return this.mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return this.mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return this.mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return this.mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    private boolean isAnimationRunning(Animation animation) {
        return (animation == null || !animation.hasStarted() || animation.hasEnded()) ? false : true;
    }

    @SuppressLint({"NewApi"})
    private void moveSpinner(float overscrollTop) {
        float slingshotDist;
        this.mProgress.showArrow(true);
        float dragPercent = Math.min(1.0f, Math.abs(overscrollTop / this.mTotalDragDistance));
        float adjustedPercent = (((float) Math.max(((double) dragPercent) - 0.4d, 0.0d)) * 5.0f) / 3.0f;
        float extraOS = Math.abs(overscrollTop) - this.mTotalDragDistance;
        if (this.mUsingCustomStart) {
            slingshotDist = (float) (this.mSpinnerOffsetEnd - this.mOriginalOffsetTop);
        } else {
            slingshotDist = (float) this.mSpinnerOffsetEnd;
        }
        float tensionSlingshotPercent = Math.max(0.0f, Math.min(extraOS, DECELERATE_INTERPOLATION_FACTOR * slingshotDist) / slingshotDist);
        float tensionPercent = ((float) (((double) (tensionSlingshotPercent / 4.0f)) - Math.pow((double) (tensionSlingshotPercent / 4.0f), 2.0d))) * DECELERATE_INTERPOLATION_FACTOR;
        int targetY = this.mOriginalOffsetTop + ((int) ((slingshotDist * dragPercent) + ((slingshotDist * tensionPercent) * DECELERATE_INTERPOLATION_FACTOR)));
        if (this.mCircleView.getVisibility() != 0) {
            this.mCircleView.setVisibility(LARGE);
        }
        if (!this.mScale) {
            this.mCircleView.setScaleX(1.0f);
            this.mCircleView.setScaleY(1.0f);
        }
        if (this.mScale) {
            setAnimationProgress(Math.min(1.0f, overscrollTop / this.mTotalDragDistance));
        }
        if (overscrollTop < this.mTotalDragDistance) {
            if (this.mProgress.getAlpha() > STARTING_PROGRESS_ALPHA) {
                if (!isAnimationRunning(this.mAlphaStartAnimation)) {
                    startProgressAlphaStartAnimation();
                }
            }
        } else if (this.mProgress.getAlpha() < MAX_ALPHA) {
            if (!isAnimationRunning(this.mAlphaMaxAnimation)) {
                startProgressAlphaMaxAnimation();
            }
        }
        this.mProgress.setStartEndTrim(0.0f, Math.min(MAX_PROGRESS_ANGLE, adjustedPercent * MAX_PROGRESS_ANGLE));
        this.mProgress.setArrowScale(Math.min(1.0f, adjustedPercent));
        this.mProgress.setProgressRotation(((-0.25f + (0.4f * adjustedPercent)) + (DECELERATE_INTERPOLATION_FACTOR * tensionPercent)) * DRAG_RATE);
        setTargetOffsetTopAndBottom(targetY - this.mCurrentTargetOffsetTop);
    }

    private void finishSpinner(float overscrollTop) {
        if (overscrollTop > this.mTotalDragDistance) {
            setRefreshing(true, true);
            return;
        }
        this.mRefreshing = false;
        this.mProgress.setStartEndTrim(0.0f, 0.0f);
        AnimationListener listener = null;
        if (!this.mScale) {
            listener = new C01155();
        }
        animateOffsetToStartPosition(this.mCurrentTargetOffsetTop, listener);
        this.mProgress.showArrow(false);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (this.mReturningToStart && action == 0) {
            this.mReturningToStart = false;
        }
        if (!isEnabled() || this.mReturningToStart || canChildScrollUp() || this.mRefreshing || this.mNestedScrollInProgress) {
            return false;
        }
        int pointerIndex;
        float overscrollTop;
        switch (action) {
            case LARGE /*0*/:
                this.mActivePointerId = ev.getPointerId(LARGE);
                this.mIsBeingDragged = false;
                break;
            case DEFAULT /*1*/:
                pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }
                if (this.mIsBeingDragged) {
                    overscrollTop = (ev.getY(pointerIndex) - this.mInitialMotionY) * DRAG_RATE;
                    this.mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                this.mActivePointerId = INVALID_POINTER;
                return false;
            case FileUtils.ACTION_GET_DIRECTORY /*2*/:
                pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                float y = ev.getY(pointerIndex);
                startDragging(y);
                if (this.mIsBeingDragged) {
                    overscrollTop = (y - this.mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0.0f) {
                        moveSpinner(overscrollTop);
                        break;
                    }
                    return false;
                }
                break;
            case FileUtils.WRITE /*3*/:
                return false;
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                pointerIndex = ev.getActionIndex();
                if (pointerIndex >= 0) {
                    this.mActivePointerId = ev.getPointerId(pointerIndex);
                    break;
                }
                Log.e(LOG_TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                return false;
            case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
                onSecondaryPointerUp(ev);
                break;
        }
        return true;
    }

    @SuppressLint({"NewApi"})
    private void startDragging(float y) {
        if (y - this.mInitialDownY > ((float) this.mTouchSlop) && !this.mIsBeingDragged) {
            this.mInitialMotionY = this.mInitialDownY + ((float) this.mTouchSlop);
            this.mIsBeingDragged = true;
            this.mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
        }
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        this.mFrom = from;
        this.mAnimateToCorrectPosition.reset();
        this.mAnimateToCorrectPosition.setDuration(200);
        this.mAnimateToCorrectPosition.setInterpolator(this.mDecelerateInterpolator);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        if (this.mScale) {
            startScaleDownReturnToStartAnimation(from, listener);
            return;
        }
        this.mFrom = from;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration(200);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mAnimateToStartPosition);
    }

    void moveToStart(float interpolatedTime) {
        setTargetOffsetTopAndBottom((this.mFrom + ((int) (((float) (this.mOriginalOffsetTop - this.mFrom)) * interpolatedTime))) - this.mCircleView.getTop());
    }

    @SuppressLint({"NewApi"})
    private void startScaleDownReturnToStartAnimation(int from, AnimationListener listener) {
        this.mFrom = from;
        this.mStartingScale = this.mCircleView.getScaleX();
        this.mScaleDownToStartAnimation = new C01188();
        this.mScaleDownToStartAnimation.setDuration(150);
        if (listener != null) {
            this.mCircleView.setAnimationListener(listener);
        }
        this.mCircleView.clearAnimation();
        this.mCircleView.startAnimation(this.mScaleDownToStartAnimation);
    }

    void setTargetOffsetTopAndBottom(int offset) {
        this.mCircleView.bringToFront();
        ViewCompat.offsetTopAndBottom(this.mCircleView, offset);
        this.mCurrentTargetOffsetTop = this.mCircleView.getTop();
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            this.mActivePointerId = ev.getPointerId(pointerIndex == 0 ? DEFAULT : LARGE);
        }
    }
}
