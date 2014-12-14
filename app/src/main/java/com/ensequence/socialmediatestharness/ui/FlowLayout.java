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

package com.ensequence.socialmediatestharness.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Taken from https://gist.github.com/swhite24/3251027/raw/3b059c4dff622456ff51c3e87d05ee2c0967ab20/FlowLayout.java.
 * (adapted version to support gravity right)
 */
public class FlowLayout extends ViewGroup {

    private final List<Integer> mLineHeights = newArrayList();
    private final List<Integer> mLineWidths = newArrayList();

    private static final int HORIZONTAL_SPACING = 2;
    private static final int VERTICAL_SPACING = 2;

    @SuppressWarnings("unused")
    public FlowLayout(final Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public FlowLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
                             final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int width = 0;
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        final int count = getChildCount();

        int currentLineHeight = 0;
        int currentHeight = 0;

        mLineHeights.clear();
        mLineWidths.clear();

        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                    MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(MeasureSpec.makeMeasureSpec(maxWidth,
                    MeasureSpec.UNSPECIFIED), childHeightMeasureSpec);

            final int childWidth = child.getMeasuredWidth();

            currentHeight = Math.max(
                    currentLineHeight,
                    child.getMeasuredHeight() + VERTICAL_SPACING
            );

            if (xpos + childWidth > maxWidth) {
                xpos = addLineWidth(width, xpos);

                ypos += currentLineHeight;
                mLineHeights.add(currentLineHeight);
                currentLineHeight = currentHeight;

            } else {
                width = Math.max(xpos + childWidth, width);
                currentLineHeight = currentHeight;
            }

            xpos += childWidth + HORIZONTAL_SPACING;
        }
        addLineWidth(width, xpos);
        mLineHeights.add(currentHeight);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + currentLineHeight;
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + currentLineHeight < height) {
                height = ypos + currentLineHeight;
            }
        }

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = maxWidth;
        }

        setMeasuredDimension(width, height);
    }

    private int addLineWidth(int width, int xpos) {
        xpos = Math.max(xpos, width);
        mLineWidths.add(xpos);

        xpos = getPaddingLeft();
        return xpos;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t,
                            final int r, final int b) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();

        final int count = getChildCount();
        final int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int currentLine = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            // Height of current line
            int currentHeight = mLineHeights.get(currentLine);
            int currentWidth = mLineWidths.get(currentLine);

            if (child.getVisibility() != GONE) {
                final int childWith = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                final LinearLayout.LayoutParams childLayoutParams = (LinearLayout.LayoutParams) child.getLayoutParams();

                if (xpos + childWith > width) {
                    // New line
                    xpos = getPaddingLeft();
                    ypos += currentHeight;

                    currentLine++;

                    currentHeight = mLineHeights.get(currentLine);
                    currentWidth = mLineWidths.get(currentLine);
                }

                int yOffset = 0;
                int xOffset = 0;
                if (childLayoutParams.gravity == Gravity.CENTER_VERTICAL
                        || childLayoutParams.gravity == Gravity.CENTER) {
                    // Average of difference in height
                    yOffset = (currentHeight - childHeight) / 2;
                }

                if (layoutParams.gravity == Gravity.END) {
                    xOffset += width - currentWidth;
                }

                child.layout(
                        xpos + xOffset,
                        ypos + yOffset,
                        xpos + childWith + xOffset,
                        ypos + childHeight + yOffset);
                xpos += childWith + HORIZONTAL_SPACING;
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LinearLayout.LayoutParams(super.generateDefaultLayoutParams());
    }
}