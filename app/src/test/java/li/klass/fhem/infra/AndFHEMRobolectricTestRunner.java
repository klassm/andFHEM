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

package li.klass.fhem.infra;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

public class AndFHEMRobolectricTestRunner extends RobolectricTestRunner {
    public AndFHEMRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT) && manifestProperty != null) {
            String resProperty = System.getProperty("android.resources");
            String assetsProperty = System.getProperty("android.assets");
            return new AndroidManifest(Fs.fileFromPath(manifestProperty), Fs.fileFromPath(resProperty),
                    Fs.fileFromPath(assetsProperty));
        }
        return super.getAppManifest(config);
    }

//    @Override
//    protected Statement withBeforeClasses(Statement statement) {
//        AndFHEMApplication.setContext(Robolectric.buildActivity(Activity.class).create().get());
//        return super.withBeforeClasses(statement);
//    }

    //    @Override
//    public void beforeTest(Method method) {
//        AndFHEMApplication.setContext(new Activity());
//        super.beforeTest(method);
//    }
}
