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

package li.klass.fhem.adapter.devices;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.DeviceListOnlyAdapter;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.WeatherDevice;

import java.io.InputStream;
import java.net.URL;

public class WeatherAdapter extends DeviceListOnlyAdapter<WeatherDevice> {

    @Override
    protected int getOverviewLayout(WeatherDevice device) {
        return R.layout.room_detail_weather;
    }

    @Override
    protected void fillDeviceOverviewView(final View view, WeatherDevice device) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, device.getCondition());

        final String imageURL = WeatherDevice.IMAGE_URL_PREFIX + device.getIcon();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageURL).getContent());

                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            ImageView imageView = (ImageView) view.findViewById(R.id.weatherImage);
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    Log.e(WeatherAdapter.class.getName(), "could not load image from " + imageURL, e);
                }
            }
        }).start();
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return WeatherDevice.class;
    }
}
