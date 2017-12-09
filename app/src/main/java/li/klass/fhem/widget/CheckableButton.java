package li.klass.fhem.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

public class CheckableButton extends CompoundButton {
    private boolean mClicking = false;

    public CheckableButton(Context context) {
        super(context);
    }

    public CheckableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public CheckableButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean performClick() {
        mClicking = true;
        boolean result = super.performClick();
        mClicking = false;
        return result;
    }

    @Override
    public void toggle() {
        if (!mClicking) {
            super.toggle();
        }
    }
}
