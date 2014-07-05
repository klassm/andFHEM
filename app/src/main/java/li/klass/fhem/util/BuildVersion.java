package li.klass.fhem.util;

import android.annotation.SuppressLint;

import com.google.common.base.Preconditions;

public class BuildVersion {
    public interface VersionDependent {
        void ifBelow();
        void ifAboveOrEqual();
    }

    public static void execute(VersionDependent versionDependent, int buildVersion) {
        Preconditions.checkNotNull(versionDependent);
        Preconditions.checkArgument(buildVersion > 0);

        int apiVersion = android.os.Build.VERSION.SDK_INT;
        if (apiVersion >= buildVersion) {
            versionDependent.ifAboveOrEqual();
        } else {
            versionDependent.ifBelow();
        }
    }
}
