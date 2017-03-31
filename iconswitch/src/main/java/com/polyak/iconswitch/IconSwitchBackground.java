package com.polyak.iconswitch;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by yarolegovich on 31.03.2017.
 */

public class IconSwitchBackground extends Drawable {

    private RectF bounds;
    private Paint paint;
    private RectF rightSemi, leftSemi;
    private Paint semiPaint;

    private float radiusX, radiusY;

    public IconSwitchBackground() {
        bounds = new RectF();
        rightSemi = new RectF();
        leftSemi = new RectF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        semiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        semiPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public void init(int imageSize, int width, int height) {
        final float centerX = width * 0.5f;
        final float centerY = height * 0.5f;
        final float halfWidth = imageSize * 1.75f;
        final float halfHeight = imageSize * 0.75f;

        bounds.set(
                centerX - halfWidth, centerY - halfHeight,
                centerX + halfWidth, centerY + halfHeight);

        radiusX = bounds.width() * 0.5f;
        radiusY = bounds.height();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(bounds, radiusX, radiusY, paint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }
}
