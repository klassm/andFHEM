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

package li.klass.fhem.service.room.group.device;

import android.content.Context;

import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.domain.CulHmSubType;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

@Singleton
public class CulHmDeviceGroupProvider extends DeviceGroupProvider {
    @Inject
    public CulHmDeviceGroupProvider() {
        super("CUL_HM");
    }

    @Override
    public Optional<String> groupFor(XmlListDevice xmlListDevice, Context context) {
        Optional<String> subTypeAttribute = xmlListDevice.getAttribute("subType");
        if (!subTypeAttribute.isPresent()) {
            return Optional.absent();
        }

        Optional<CulHmSubType> subType = CulHmSubType.subTypeFor(subTypeAttribute.get());
        if (subType.isPresent()) {
            return Optional.of(subType.get().getFunctionality().getCaptionText(context));
        }
        return Optional.absent();
    }
}
