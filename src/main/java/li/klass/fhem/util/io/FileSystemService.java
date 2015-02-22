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

package li.klass.fhem.util.io;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class FileSystemService {
    @Inject
    public FileSystemService() {
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public File getDocumentsFolder() {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!directory.exists()) {
            checkArgument(directory.mkdir());
        }
        return directory;
    }

    public File getOrCreateDirectoryIn(File file, String directoryName) {
        checkArgument(file.exists() && file.isDirectory());
        File newFile = new File(file, directoryName);

        if (!newFile.exists()) {
            checkArgument(newFile.mkdir());
        }
        return newFile;
    }

    public File getCacheDir(Context context) {
        return context.getCacheDir();
    }
}
