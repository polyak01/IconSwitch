package com.polyak.iconswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by polyak1 on 31.03.2017.
 */

public class IconSwitch extends ViewGroup {

    private final static int DEFAULT_IMAGE_SIZE_DP = 18;

    private final int FLING_MIN_VELOCITY;

    private ImageView leftIcon;
    private ImageView rightIcon;
    private ThumbView thumb;

    private IconSwitchBackground background;

    private ViewDragHelper thumbDragHelper;

    private float thumbPosition;
    private int thumbDragDistance;

    private int switchWidth, switchHeight;
    private int iconOffset;
    private int iconSize;
    private int iconTop, iconBottom;
    private int thumbStartLeft, thumbEndLeft;
    private int thumbDiameter;

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
        thumbDragHelper = ViewDragHelper.create(this, new ThumbDragCallback());
        FLING_MIN_VELOCITY = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
    }

    private void init(AttributeSet attr) {
        addView(thumb = new ThumbView(getContext()));
        addView(leftIcon = new ImageView(getContext()));
        addView(rightIcon = new ImageView(getContext()));

        setBackground(background = new IconSwitchBackground());

        iconSize = dpToPx(DEFAULT_IMAGE_SIZE_DP);

        if (attr != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attr, R.styleable.IconSwitch);
            int iconSize = ta.getDimensionPixelSize(R.styleable.IconSwitch_image_size, this.iconSize);
            this.iconSize = Math.max(iconSize, this.iconSize);

            leftIcon.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_icon_left));
            rightIcon.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_icon_right));
            ta.recycle();
        }

        calculateSwitchDimensions();
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

        thumb.layout(thumbStartLeft, 0, thumbStartLeft + thumbDiameter, switchHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        thumbDragHelper.processTouchEvent(event);
        return true;
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

    private class ThumbDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == thumb;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int newLeft = Math.abs(xvel) >= FLING_MIN_VELOCITY ?
                    getLeftAfterFling(xvel) :
                    getLeftToSettle();
            thumbDragHelper.settleCapturedViewAt(newLeft, thumb.getTop());
            invalidate();
        }

        private int getLeftAfterFling(float direction) {
            return direction > 0 ? thumbEndLeft : thumbStartLeft;
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
            return Math.max(thumbStartLeft,Math.min(left, thumbEndLeft));
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return child == thumb ? thumbDragDistance : 0;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }
}
