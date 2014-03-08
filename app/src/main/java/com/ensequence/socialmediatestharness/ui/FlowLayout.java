package com.ensequence.socialmediatestharness.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Taken from https://gist.github.com/swhite24/3251027/raw/3b059c4dff622456ff51c3e87d05ee2c0967ab20/FlowLayout.java.
 * (adapted version to support gravity right)
 */
public class FlowLayout extends ViewGroup {

    private final List<Integer> mLineHeights = new ArrayList<Integer>();
    private final List<Integer> mLineWidths = new ArrayList<Integer>();

    public static class LayoutParams extends LinearLayout.LayoutParams {

        public final int horizontalSpacing;
        public final int verticalSpacing;

        /**
         * @param horizontalSpacing Pixels between items, horizontally
         * @param verticalSpacing   Pixels between items, vertically
         */
        public LayoutParams(final int horizontalSpacing,
                            final int verticalSpacing) {
            super(0, 0);
            this.horizontalSpacing = horizontalSpacing;
            this.verticalSpacing = verticalSpacing;
        }
    }

    public FlowLayout(final Context context) {
        super(context);
    }

    public FlowLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
                             final int heightMeasureSpec) {
        assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

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

            final LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

            child.measure(MeasureSpec.makeMeasureSpec(maxWidth,
                    MeasureSpec.EXACTLY), childHeightMeasureSpec);

            final int childWidth = child.getMeasuredWidth();

            currentHeight = Math.max(
                    currentLineHeight,
                    child.getMeasuredHeight() + childLayoutParams.verticalSpacing
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

            xpos += childWidth + childLayoutParams.horizontalSpacing;
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
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return true;
        }
        return false;
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
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                final LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

                if (xpos + childw > width) {
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
                    yOffset = (currentHeight - childh) / 2;
                }

                if (layoutParams.gravity == Gravity.RIGHT) {
                    xOffset += width - currentWidth;
                }

                child.layout(
                        xpos + xOffset,
                        ypos + yOffset,
                        xpos + childw + xOffset,
                        ypos + childh + yOffset);
                xpos += childw + childLayoutParams.horizontalSpacing;
            }
        }
    }
}