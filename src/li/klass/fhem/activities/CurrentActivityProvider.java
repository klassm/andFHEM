package li.klass.fhem.activities;

import li.klass.fhem.activities.base.BaseActivity;

public class CurrentActivityProvider {
    public static final CurrentActivityProvider INSTANCE = new CurrentActivityProvider();

    private volatile BaseActivity<?> currentActivity = null;
    private CurrentActivityProvider() {
    }

    public BaseActivity<?> getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(BaseActivity<?> currentActivity) {
        this.currentActivity = currentActivity;
    }
}
