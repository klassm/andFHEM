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

package li.klass.fhem.service.device;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMRegistrar;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.GCMSendDevice;
import li.klass.fhem.service.AbstractService;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.StringUtil;

import static com.google.common.base.Strings.isNullOrEmpty;
import static li.klass.fhem.constants.PreferenceKeys.GCM_PROJECT_ID;
import static li.klass.fhem.constants.PreferenceKeys.GCM_REGISTRATION_ID;

@Singleton
public class GCMSendDeviceService extends AbstractService {
    private static final String ATTR_REG_IDS_COMMAND = "attr %s regIds %s";

    @Inject
    CommandExecutionService commandExecutionService;

    @Inject
    ApplicationProperties applicationProperties;

    public void addSelf(GCMSendDevice device) {

        String registrationId = applicationProperties.getStringSharedPreference(GCM_REGISTRATION_ID, null);
        if (StringUtil.isBlank(registrationId)) {
            showToast(R.string.gcmRegistrationNotActive);
            return;
        }

        if (ArrayUtil.contains(device.getRegIds(), registrationId)) {
            showToast(R.string.gcmAlreadyRegistered);
            return;
        }

        String[] newRegIds = ArrayUtil.addToArray(device.getRegIds(), registrationId);
        setRegIdsAttributeFor(device, newRegIds);

        Intent intent = new Intent(Actions.DO_UPDATE);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, true);
        applicationContext.sendBroadcast(intent);

        showToast(R.string.gcmSuccessfullyRegistered);
    }

    private void setRegIdsAttributeFor(GCMSendDevice device, String[] newRegIds) {
        String regIdsAttribute = ArrayUtil.join(newRegIds, "|");

        commandExecutionService.executeSafely(String.format(ATTR_REG_IDS_COMMAND, device.getName(), regIdsAttribute));
        device.readREGIDS(regIdsAttribute);
    }

    public void removeRegistrationId(GCMSendDevice device, String registrationId) {
        if (!ArrayUtil.contains(device.getRegIds(), registrationId)) {
            return;
        }

        String[] newRegIds = ArrayUtil.removeFromArray(device.getRegIds(), registrationId);
        setRegIdsAttributeFor(device, newRegIds);
    }

    public boolean isDeviceRegistered(GCMSendDevice device) {
        String registrationId = applicationProperties.getStringSharedPreference(GCM_REGISTRATION_ID, null);

        return (registrationId != null && ArrayUtil.contains(device.getRegIds(), registrationId));
    }

    public void registerWithGCM(Context context) {
        String projectId = applicationProperties.getStringSharedPreference(GCM_PROJECT_ID, null);
        registerWithGCMInternal(context, projectId);
    }

    private void registerWithGCMInternal(Context context, String projectId) {
        if (isNullOrEmpty(projectId)) return;

        if (!GCMRegistrar.isRegistered(context)) {
            GCMRegistrar.checkDevice(context);
            GCMRegistrar.checkManifest(context);

            GCMRegistrar.setRegisterOnServerLifespan(context, 1000L * 60 * 60 * 24 * 30);
            GCMRegistrar.register(context, projectId);
        }
    }

    public void registerWithGCM(Context context, String projectId) {
        applicationProperties.setSharedPreference(GCM_REGISTRATION_ID, null);
        GCMRegistrar.unregister(context);
        registerWithGCMInternal(context, projectId);
    }
}
