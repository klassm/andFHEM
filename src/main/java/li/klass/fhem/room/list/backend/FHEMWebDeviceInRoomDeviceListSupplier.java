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

package li.klass.fhem.room.list.backend;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import li.klass.fhem.connection.backend.ConnectionService;
import li.klass.fhem.domain.FHEMWEBDevice;
import li.klass.fhem.domain.core.DeviceType;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.util.ApplicationProperties;

import static com.google.common.collect.FluentIterable.from;
import static li.klass.fhem.settings.SettingsKeys.FHEMWEB_DEVICE_NAME;

public class FHEMWebDeviceInRoomDeviceListSupplier implements Supplier<FHEMWEBDevice> {
    private static final String DEFAULT_FHEMWEB_QUALIFIER = "andFHEM";

    private ApplicationProperties applicationProperties;
    private ConnectionService connectionService;
    private RoomDeviceList roomDeviceList;
    private Context context;

    public FHEMWebDeviceInRoomDeviceListSupplier(ApplicationProperties applicationProperties, ConnectionService connectionService, RoomDeviceList roomDeviceList, Context context) {
        this.applicationProperties = applicationProperties;
        this.connectionService = connectionService;
        this.context = context;
        this.roomDeviceList = roomDeviceList;
    }

    @Override
    public FHEMWEBDevice get() {
        List<FhemDevice> devicesOfType = roomDeviceList == null ?
                Lists.<FhemDevice>newArrayList() : roomDeviceList.getDevicesOfType(DeviceType.FHEMWEB);
        return get(devicesOfType);
    }

    private FHEMWEBDevice get(List<FhemDevice> devices) {
        if (devices.isEmpty()) return new FHEMWEBDevice();

        String qualifier = StringUtils.stripToNull(applicationProperties.getStringSharedPreference(FHEMWEB_DEVICE_NAME, null, context));

        if (qualifier == null) {
            int port = connectionService.getPortOfSelectedConnection(context);
            Optional<FhemDevice> match = from(devices).filter(predicateFHEMWEBDeviceForPort(port)).first();
            if (match.isPresent()) {
                return (FHEMWEBDevice) match.get();
            }
        }

        qualifier = (qualifier == null ? DEFAULT_FHEMWEB_QUALIFIER : qualifier).toUpperCase(Locale.getDefault());

        Optional<FhemDevice> match = from(devices).filter(predicateFHEMWEBDeviceForQualifier(qualifier)).first();
        if (match.isPresent()) {
            return (FHEMWEBDevice) match.get();
        }
        return (FHEMWEBDevice) devices.get(0);
    }

    private Predicate<FhemDevice> predicateFHEMWEBDeviceForQualifier(final String qualifier) {
        return new Predicate<FhemDevice>() {
            @Override
            public boolean apply(FhemDevice device) {
                return device instanceof FHEMWEBDevice && device.getName() != null
                        && device.getName().toUpperCase(Locale.getDefault()).contains(qualifier);
            }
        };
    }

    private Predicate<FhemDevice> predicateFHEMWEBDeviceForPort(final int port) {
        return new Predicate<FhemDevice>() {
            @Override
            public boolean apply(FhemDevice device) {
                if (!(device instanceof FHEMWEBDevice)) return false;
                FHEMWEBDevice fhemwebDevice = (FHEMWEBDevice) device;
                return fhemwebDevice.getPort().equals(port + "");
            }
        };
    }
}
