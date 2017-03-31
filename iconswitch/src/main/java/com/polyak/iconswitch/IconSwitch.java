package com.polyak.iconswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by polyak1 on 31.03.2017.
 */

public class IconSwitch extends ViewGroup {

    private static final int DEFAULT_IMAGE_SIZE_DP = 18;
    private static final int UNITS_VELOCITY = 1000;

    private final double TOUCH_SLOP_SQUARE;
    private final int FLING_MIN_VELOCITY;

    private ImageView leftIcon;
    private ImageView rightIcon;
    private ThumbView thumb;

    private IconSwitchBackground background;

    private ViewDragHelper thumbDragHelper;
    private VelocityTracker velocityTracker;

    private float thumbPosition;
    private int thumbDragDistance;

    private int switchWidth, switchHeight;
    private int iconOffset, iconSize;
    private int iconTop, iconBottom;
    private int thumbStartLeft, thumbEndLeft;
    private int thumbDiameter;

    private boolean isLeftChecked;

    private int inactiveTintIconLeft, activeTintIconLeft;
    private int inactiveTintIconRight, activeTintIconRight;
    private int thumbColorLeft, thumbColorRight;

    private PointF downPoint;
    private boolean isClick;

    private Listener listener;

    public IconSwitch(Context context) {
        super(context);
        init(null);
    }

    public IconSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public IconSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IconSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    {
        ViewConfiguration viewConf = ViewConfiguration.get(getContext());
        FLING_MIN_VELOCITY = viewConf.getScaledMinimumFlingVelocity();
        TOUCH_SLOP_SQUARE = Math.pow(viewConf.getScaledTouchSlop(), 2);
        thumbDragHelper = ViewDragHelper.create(this, new ThumbDragCallback());
        downPoint = new PointF();
    }

    private void init(AttributeSet attr) {
        addView(thumb = new ThumbView(getContext()));
        addView(leftIcon = new ImageView(getContext()));
        addView(rightIcon = new ImageView(getContext()));

        setBackground(background = new IconSwitchBackground());

        iconSize = dpToPx(DEFAULT_IMAGE_SIZE_DP);

        int colorDefInactive = getAccentColor();
        int colorDefActive = Color.WHITE;
        int colorDefBackground = ContextCompat.getColor(getContext(), R.color.isw_defaultBg);
        //noinspection UnnecessaryLocalVariable
        int colorDefThumb = colorDefInactive;

        if (attr != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attr, R.styleable.IconSwitch);
            int iconSide = ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_image_size, iconSize);
            iconSize = Math.max(iconSide, iconSize);
            leftIcon.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_isw_icon_left));
            rightIcon.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_isw_icon_right));
            inactiveTintIconLeft = ta.getColor(R.styleable.IconSwitch_isw_inactive_tint_icon_left, colorDefInactive);
            activeTintIconLeft = ta.getColor(R.styleable.IconSwitch_isw_active_tint_icon_left, colorDefActive);
            inactiveTintIconRight = ta.getColor(R.styleable.IconSwitch_isw_inactive_tint_icon_right, colorDefInactive);
            activeTintIconRight = ta.getColor(R.styleable.IconSwitch_isw_active_tint_icon_right, colorDefActive);
            background.setColor(ta.getColor(R.styleable.IconSwitch_isw_background_color, colorDefBackground));
            thumbColorLeft = ta.getColor(R.styleable.IconSwitch_isw_thumb_color_left, colorDefThumb);
            thumbColorRight = ta.getColor(R.styleable.IconSwitch_isw_thumb_color_right, colorDefThumb);
            isLeftChecked = ta.getInt(R.styleable.IconSwitch_isw_default_selection, 0) == 0;
            ta.recycle();
        } else {
            isLeftChecked = true;
            inactiveTintIconLeft = colorDefInactive;
            activeTintIconLeft = colorDefActive;
            inactiveTintIconRight = colorDefInactive;
            activeTintIconRight = colorDefActive;
            background.setColor(colorDefBackground);
            thumbColorLeft = colorDefThumb;
            thumbColorRight = colorDefThumb;
        }

        calculateSwitchDimensions();

        ensureCorrectColors();
    }

    private void calculateSwitchDimensions() {
        switchWidth = iconSize * 4;
        switchHeight = Math.round(iconSize * 2f);

        iconOffset = Math.round(iconSize * 0.6f);
        iconTop = (switchHeight - iconSize) / 2;
        iconBottom = iconTop + iconSize;
        thumbDiameter = switchHeight;

        int thumbRadius = thumbDiameter / 2;
        int iconHalfSize = iconSize / 2;
        thumbStartLeft = iconOffset + iconHalfSize - thumbRadius;
        thumbEndLeft = switchWidth - iconOffset - iconHalfSize - thumbRadius;
        thumbDragDistance = thumbEndLeft - thumbStartLeft;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSize(widthMeasureSpec, switchWidth);
        int height = getSize(heightMeasureSpec, switchHeight);

        int thumbSpec = MeasureSpec.makeMeasureSpec(switchHeight, MeasureSpec.EXACTLY);
        thumb.measure(thumbSpec, thumbSpec);

        int iconSpec = MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY);
        leftIcon.measure(iconSpec, iconSpec);
        rightIcon.measure(iconSpec, iconSpec);

        background.init(iconSize, width, height);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        leftIcon.layout(iconOffset, iconTop, iconOffset + iconSize, iconBottom);

        int rightIconLeft = switchWidth - iconOffset - iconSize;
        rightIcon.layout(rightIconLeft, iconTop, rightIconLeft + iconSize, iconBottom);

        int thumbLeft = (int) (thumbStartLeft + thumbDragDistance * thumbPosition);
        thumb.layout(thumbLeft, 0, thumbLeft + thumbDiameter, switchHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                clearTouchInfo();
                break;
            case MotionEvent.ACTION_CANCEL:
                clearTouchInfo();
                break;
        }
        thumbDragHelper.processTouchEvent(event);
        return true;
    }

    private void onDown(MotionEvent e) {
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(e);
        downPoint.set(e.getX(), e.getY());
        isClick = true;
    }

    private void onMove(MotionEvent e) {
        velocityTracker.addMovement(e);
        double distance = Math.hypot(e.getX() - downPoint.x, e.getY() - downPoint.y);
        if (isClick) {
            isClick = distance < TOUCH_SLOP_SQUARE;
        }
    }

    private void onUp(MotionEvent e) {
        velocityTracker.addMovement(e);
        velocityTracker.computeCurrentVelocity(UNITS_VELOCITY);
        if (isClick) {
            isClick = Math.abs(velocityTracker.getXVelocity()) < FLING_MIN_VELOCITY;
        }
        if (isClick) {
            toggleSwitch();
            notifySelectionChanged();
        }
    }

    private void clearTouchInfo() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void toggleSwitch() {
        isLeftChecked = !isLeftChecked;
        int newLeft = isLeftChecked ? thumbStartLeft : thumbEndLeft;
        thumbDragHelper.smoothSlideViewTo(thumb, newLeft, thumb.getTop());
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (thumbDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int getSize(int measureSpec, int fallbackSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                return Math.min(size, fallbackSize);
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.UNSPECIFIED:
                return fallbackSize;
            default:
                return size;
        }
    }

    private int getLeftAfterFling(float direction) {
        return direction > 0 ? thumbEndLeft : thumbStartLeft;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void ensureCorrectColors() {
        leftIcon.setColorFilter(isLeftChecked ? activeTintIconLeft : inactiveTintIconLeft);
        rightIcon.setColorFilter(isLeftChecked ? inactiveTintIconRight : activeTintIconRight);
        thumb.setColor(isLeftChecked ? thumbColorLeft : thumbColorRight);
    }

    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onCheckChanged(!isLeftChecked);
        }
    }

    private class ThumbDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (child != thumb) {
                thumbDragHelper.captureChildView(thumb, pointerId);
                return false;
            }
            return true;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (isClick) {
                return;
            }
            boolean isFling = Math.abs(xvel) >= FLING_MIN_VELOCITY;
            int newLeft = isFling ? getLeftAfterFling(xvel) : getLeftToSettle();
            boolean currentSelection = newLeft == thumbStartLeft;
            if (currentSelection != isLeftChecked) {
                isLeftChecked = currentSelection;
                notifySelectionChanged();
            }
            thumbDragHelper.settleCapturedViewAt(newLeft, thumb.getTop());
            invalidate();
        }

        private int getLeftToSettle() {
            return thumbPosition > 0.5f ? thumbEndLeft : thumbStartLeft;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            thumbPosition = ((float) (left - thumbStartLeft)) / thumbDragDistance;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(thumbStartLeft, Math.min(left, thumbEndLeft));
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child == thumb ? thumbDragDistance : 0;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    private int getAccentColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public interface Listener {
        void onCheckChanged(boolean checked);
    }
}
