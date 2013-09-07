/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
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
 */

package li.klass.fhem.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class ImageUtil {

    private static long lastFail = 0;

    public interface ImageLoadedListener {
        void imageLoaded(Bitmap bitmap);
    }

    public static void loadImageFrom(final String imageURL, final ImageLoadedListener callback) {
        final Handler handler = new Handler();
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                return loadBitmap(imageURL);
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                // onPostExecute is run from within the UI thread, but Android allows to run multiple UI threads.
                // We cannot be sure which one is chosen, so we enforce the right UI thread by using an explicit
                // handler.
                // see http://stackoverflow.com/questions/10426120/android-got-calledfromwrongthreadexception-in-onpostexecute-how-could-it-be
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.imageLoaded(bitmap);
                    }
                });
            }
        }.execute(null, null);
    }

    public static Bitmap loadBitmap(String imageURL) {
        if (System.currentTimeMillis() - lastFail < 60 * 10) {
            return null;
        }

        try {
            URL url = new URL(imageURL);
            return BitmapFactory.decodeStream((InputStream) url.getContent());
        } catch (Exception e) {
            Log.e(ImageUtil.class.getName(), "could not load image from " + imageURL, e);
            lastFail = System.currentTimeMillis();
            return null;
        }
    }

    public static void setExternalImageIn(final ImageView imageView, final String imageURL) {
        loadImageFrom(imageURL, new ImageLoadedListener() {
            @Override
            public void imageLoaded(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }
}
