/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package com.chiralcode.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// taken from https://raw.github.com/chiralcode/Android-Color-Picker/master/src/com/chiralcode/colorpicker/ColorPicker.java
@SuppressLint("ClickableViewAccessibility")
public class ColorPicker extends View {

    /**
     * Customizable display parameters (in percents)
     */
    private static final int paramOuterPadding = 2; // outer padding of the whole color picker view
    private static final int paramInnerPadding = 5; // distance between value slider wheel and inner color wheel
    private static final int paramValueSliderWidth = 10; // width of the value slider
    private static final int paramArrowPointerSize = 4; // size of the arrow pointer; set to 0 to hide the pointer

    private Paint colorWheelPaint;
    private Paint valueSliderPaint;

    private Paint colorViewPaint;

    private Paint colorPointerPaint;
    private RectF colorPointerCoords;

    private Paint valuePointerPaint;
    private Paint valuePointerArrowPaint;

    private RectF outerWheelRect;
    private RectF innerWheelRect;

    private Path colorViewPath;
    private Path valueSliderPath;
    private Path arrowPointerPath;

    private Bitmap colorWheelBitmap;

    private int arrowPointerSize;
    private int outerWheelRadius;
    private int innerWheelRadius;
    private int colorWheelRadius;

    private Matrix gradientRotationMatrix;

    /**
     * Currently selected color
     */
    private float[] colorHSV = new float[]{0f, 0f, 1f};

    private ColorPickerListener listener;

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        colorPointerPaint = new Paint();
        colorPointerPaint.setStyle(Style.STROKE);
        colorPointerPaint.setStrokeWidth(2f);
        colorPointerPaint.setARGB(128, 0, 0, 0);

        valuePointerPaint = new Paint();
        valuePointerPaint.setStyle(Style.STROKE);
        valuePointerPaint.setStrokeWidth(2f);

        valuePointerArrowPaint = new Paint();

        colorWheelPaint = new Paint();
        colorWheelPaint.setAntiAlias(true);
        colorWheelPaint.setDither(true);

        valueSliderPaint = new Paint();
        valueSliderPaint.setAntiAlias(true);
        valueSliderPaint.setDither(true);

        colorViewPaint = new Paint();
        colorViewPaint.setAntiAlias(true);

        colorViewPath = new Path();
        valueSliderPath = new Path();
        arrowPointerPath = new Path();

        outerWheelRect = new RectF();
        innerWheelRect = new RectF();

        colorPointerCoords = new RectF();

    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPicker(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // drawing color wheel

        canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);

        // drawing color view

        colorViewPaint.setColor(Color.HSVToColor(colorHSV));
        canvas.drawPath(colorViewPath, colorViewPaint);

        // drawing value slider

        float[] hsv = new float[]{colorHSV[0], colorHSV[1], 1f};

        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, new int[]{Color.BLACK, Color.HSVToColor(hsv), Color.WHITE}, null);
        sweepGradient.setLocalMatrix(gradientRotationMatrix);
        valueSliderPaint.setShader(sweepGradient);

        canvas.drawPath(valueSliderPath, valueSliderPaint);

        // drawing color wheel pointer

        float hueAngle = (float) Math.toRadians(colorHSV[0]);
        int colorPointX = (int) (-Math.cos(hueAngle) * colorHSV[1] * colorWheelRadius) + centerX;
        int colorPointY = (int) (-Math.sin(hueAngle) * colorHSV[1] * colorWheelRadius) + centerY;

        float pointerRadius = 0.075f * colorWheelRadius;
        int pointerX = (int) (colorPointX - pointerRadius / 2);
        int pointerY = (int) (colorPointY - pointerRadius / 2);

        colorPointerCoords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
        canvas.drawOval(colorPointerCoords, colorPointerPaint);

        // drawing value pointer

        valuePointerPaint.setColor(Color.HSVToColor(new float[]{0f, 0f, 1f - colorHSV[2]}));

        double valueAngle = (colorHSV[2] - 0.5f) * Math.PI;
        float valueAngleX = (float) Math.cos(valueAngle);
        float valueAngleY = (float) Math.sin(valueAngle);

        canvas.drawLine(valueAngleX * innerWheelRadius + centerX, valueAngleY * innerWheelRadius + centerY, valueAngleX * outerWheelRadius + centerX,
                valueAngleY * outerWheelRadius + centerY, valuePointerPaint);

        // drawing pointer arrow

        if (arrowPointerSize > 0) {
            drawPointerArrow(canvas);
        }

    }

    private void drawPointerArrow(Canvas canvas) {

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double tipAngle = (colorHSV[2] - 0.5f) * Math.PI;
        double leftAngle = tipAngle + Math.PI / 96;
        double rightAngle = tipAngle - Math.PI / 96;

        double tipAngleX = Math.cos(tipAngle) * outerWheelRadius;
        double tipAngleY = Math.sin(tipAngle) * outerWheelRadius;
        double leftAngleX = Math.cos(leftAngle) * (outerWheelRadius + arrowPointerSize);
        double leftAngleY = Math.sin(leftAngle) * (outerWheelRadius + arrowPointerSize);
        double rightAngleX = Math.cos(rightAngle) * (outerWheelRadius + arrowPointerSize);
        double rightAngleY = Math.sin(rightAngle) * (outerWheelRadius + arrowPointerSize);

        arrowPointerPath.reset();
        arrowPointerPath.moveTo((float) tipAngleX + centerX, (float) tipAngleY + centerY);
        arrowPointerPath.lineTo((float) leftAngleX + centerX, (float) leftAngleY + centerY);
        arrowPointerPath.lineTo((float) rightAngleX + centerX, (float) rightAngleY + centerY);
        arrowPointerPath.lineTo((float) tipAngleX + centerX, (float) tipAngleY + centerY);

        valuePointerArrowPaint.setColor(Color.HSVToColor(colorHSV));
        valuePointerArrowPaint.setStyle(Style.FILL);
        canvas.drawPath(arrowPointerPath, valuePointerArrowPaint);

        valuePointerArrowPaint.setStyle(Style.STROKE);
        valuePointerArrowPaint.setStrokeJoin(Join.ROUND);
        valuePointerArrowPaint.setColor(Color.BLACK);
        canvas.drawPath(arrowPointerPath, valuePointerArrowPaint);

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {

        int centerX = width / 2;
        int centerY = height / 2;

        int innerPadding = paramInnerPadding * width / 100;
        int outerPadding = paramOuterPadding * width / 100;
        arrowPointerSize = paramArrowPointerSize * width / 100;
        int valueSliderWidth = paramValueSliderWidth * width / 100;

        outerWheelRadius = width / 2 - outerPadding - arrowPointerSize;
        innerWheelRadius = outerWheelRadius - valueSliderWidth;
        colorWheelRadius = innerWheelRadius - innerPadding;

        outerWheelRect.set(centerX - outerWheelRadius, centerY - outerWheelRadius, centerX + outerWheelRadius, centerY + outerWheelRadius);
        innerWheelRect.set(centerX - innerWheelRadius, centerY - innerWheelRadius, centerX + innerWheelRadius, centerY + innerWheelRadius);

        colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2);

        gradientRotationMatrix = new Matrix();
        gradientRotationMatrix.preRotate(270, width / 2, height / 2);

        colorViewPath.arcTo(outerWheelRect, 270, -180);
        colorViewPath.arcTo(innerWheelRect, 90, 180);

        valueSliderPath.arcTo(outerWheelRect, 270, 180);
        valueSliderPath.arcTo(innerWheelRect, 90, -180);

    }

    private Bitmap createColorWheelBitmap(int width, int height) {

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        int colorCount = 12;
        int colorAngleStep = 360 / 12;
        int colors[] = new int[colorCount + 1];
        float hsv[] = new float[]{0f, 1f, 1f};
        for (int i = 0; i < colors.length; i++) {
            hsv[0] = (i * colorAngleStep + 180) % 360;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[colorCount] = colors[0];

        SweepGradient sweepGradient = new SweepGradient(width / 2, height / 2, colors, null);
        RadialGradient radialGradient = new RadialGradient(width / 2, height / 2, colorWheelRadius, 0xFFFFFFFF, 0x00FFFFFF, TileMode.CLAMP);
        ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);

        colorWheelPaint.setShader(composeShader);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(width / 2, height / 2, colorWheelRadius, colorWheelPaint);

        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                int x = (int) event.getX();
                int y = (int) event.getY();
                int cx = x - getWidth() / 2;
                int cy = y - getHeight() / 2;
                double d = Math.sqrt(cx * cx + cy * cy);

                if (d <= colorWheelRadius) {

                    colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 180f);
                    colorHSV[1] = Math.max(0f, Math.min(1f, (float) (d / colorWheelRadius)));

                    invalidate();

                } else if (x >= getWidth() / 2 && d >= innerWheelRadius) {

                    colorHSV[2] = (float) Math.max(0, Math.min(1, Math.atan2(cy, cx) / Math.PI + 0.5f));

                    invalidate();
                }
                notifyListener();

                return true;
        }

        notifyListener();

        return super.onTouchEvent(event);
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onColorChange(getColor());
        }
    }

    public int getColor() {
        return Color.HSVToColor(colorHSV);
    }

    public void setColor(int color) {
        Color.colorToHSV(color, colorHSV);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putFloatArray("color", colorHSV);
        state.putParcelable("super", super.onSaveInstanceState());
        return state;
    }

    public void setListener(ColorPickerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            colorHSV = bundle.getFloatArray("color");
            super.onRestoreInstanceState(bundle.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

}