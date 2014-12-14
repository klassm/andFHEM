/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 *  server.
 *
 *  Copyright (c) 2012, Matthias Klass or third-party contributors as
 *  indicated by the @author tags or express copyright attribution
 *  statements applied by the authors.  All third-party contributions are
 *  distributed under license by Red Hat Inc.
 *
 *  This copyrighted material is made available to anyone wishing to use, modify,
 *  copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 *  for more details.
 *
 *  You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 *  along with this distribution; if not, write to:
 *    Free Software Foundation, Inc.
 *    51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import li.klass.fhem.util.DisplayUtil;
import li.klass.fhem.util.ValueDescriptionUtil;

import static li.klass.fhem.util.DisplayUtil.dpToPx;

public class LitreContentView extends View {

    private static final Paint WHITE_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint BORDER_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static final int DARK_BLUE = 0xFF0808ff;
    public static final int LIGHT_BLUE = 0XFFC3E6FF;

    public static final int BORDER_WIDTH = 3;

    private double fillPercentage;
    private int size;

    public static final int FONT_SIZE_DP = 15;

    static {
        WHITE_PAINT.setColor(Color.WHITE);

        TEXT_PAINT.setColor(Color.RED);
        TEXT_PAINT.setTextSize(dpToPx(FONT_SIZE_DP));
        TEXT_PAINT.setTextAlign(Paint.Align.CENTER);

        BORDER_PAINT.setStyle(Paint.Style.STROKE);
        BORDER_PAINT.setColor(Color.GRAY);
        BORDER_PAINT.setStrokeWidth(BORDER_WIDTH);
    }

    public LitreContentView(Context context, double fillPercentage) {
        super(context);
        this.fillPercentage = fillPercentage;
        init();
    }

    public LitreContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LitreContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        size = DisplayUtil.getSmallestDimensionInPx() / 3;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.rightMargin = 0;
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int xStart = 0;

        int innerCircleYCenter = (int) (size * 0.6);
        int innerCircleXCenter = xStart + size / 2;
        int innerCircleRadius = size - innerCircleYCenter - BORDER_WIDTH;

        int topDimension = size / 10;

        drawFillstateCircle(canvas, innerCircleRadius, innerCircleYCenter, innerCircleXCenter, xStart);
        drawTop(canvas, innerCircleYCenter, innerCircleXCenter, innerCircleRadius, topDimension);
        drawText(canvas, innerCircleYCenter, innerCircleXCenter);
    }

    private void drawText(Canvas canvas, int innerCircleYCenter, int innerCircleXCenter) {
        float fillstate = (int) (fillPercentage * 100);
        String fillstateText = ValueDescriptionUtil.appendPercent(fillstate);
        canvas.drawText(fillstateText, innerCircleXCenter, innerCircleYCenter, TEXT_PAINT);
    }

    private void drawTop(Canvas canvas, int innerCircleYCenter, int innerCircleXCenter, int innerCircleRadius, int topDimension) {
        canvas.drawCircle(innerCircleXCenter, innerCircleYCenter, innerCircleRadius, BORDER_PAINT);
        canvas.drawRect(innerCircleXCenter - topDimension, innerCircleYCenter - innerCircleRadius - topDimension,
                innerCircleXCenter + topDimension, innerCircleYCenter - innerCircleRadius, BORDER_PAINT);
        canvas.drawRect(innerCircleXCenter - topDimension + 1, innerCircleYCenter - innerCircleRadius - topDimension + 1,
                innerCircleXCenter + topDimension - 1, innerCircleYCenter - innerCircleRadius + BORDER_WIDTH, WHITE_PAINT);
    }

    private void drawFillstateCircle(Canvas canvas, int innerCircleRadius, int innerCircleYCenter, int innerCircleXCenter, int xStart) {
        canvas.drawCircle(innerCircleXCenter, innerCircleYCenter, innerCircleRadius, WHITE_PAINT);

        canvas.save();

        float contentHeight = (float) (2 * innerCircleRadius * fillPercentage);
        canvas.clipRect(xStart, innerCircleYCenter + innerCircleRadius - contentHeight, innerCircleXCenter + innerCircleRadius, size, Region.Op.REPLACE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, 0, getHeight(), DARK_BLUE, LIGHT_BLUE, Shader.TileMode.MIRROR));

        paint.setAntiAlias(true);
        canvas.drawCircle(innerCircleXCenter, innerCircleYCenter, innerCircleRadius, paint);

        canvas.restore();
    }
}
