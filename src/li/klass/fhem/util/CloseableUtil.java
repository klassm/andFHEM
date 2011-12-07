package li.klass.fhem.util;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtil {
    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    Log.d(CloseableUtil.class.getName(), "error closing stream" , e);
                }
            }
        }
    }
}
