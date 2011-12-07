package li.klass.fhem;

import android.app.Application;
import android.content.Context;

public class AndFHEMApplication extends Application {
    private static Context context;
    public static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        INSTANCE = this;
    }

    public static Context getContext() {
        return context;
    }
}
