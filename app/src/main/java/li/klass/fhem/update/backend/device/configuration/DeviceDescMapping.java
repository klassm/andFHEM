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

package li.klass.fhem.update.backend.device.configuration;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.resources.ResourceIdMapper;

@Singleton
public class DeviceDescMapping {
    private final JSONObject mapping;

    @Inject
    public DeviceDescMapping() {
        try {
            mapping = new JSONObject(Resources.toString(Resources.getResource(DeviceDescMapping.class, "/deviceDescMapping.json"), Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String descFor(String key, Context context) {
        String value = mapping.optString(key, key);
        return resourceFor(value, context);
    }

    public String descFor(ResourceIdMapper resourceId, Context context) {
        return context.getString(resourceId.getId());
    }

    private String resourceFor(String value, Context context) {
        try {
            return context.getString(ResourceIdMapper.valueOf(value).getId());
        } catch (IllegalArgumentException e) {
            return value;
        }
    }
}
