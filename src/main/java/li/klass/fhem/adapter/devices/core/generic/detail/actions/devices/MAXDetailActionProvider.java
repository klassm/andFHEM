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

package li.klass.fhem.adapter.devices.core.generic.detail.actions.devices;

import android.content.Context;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton;
import li.klass.fhem.adapter.uiservice.FragmentUiService;
import li.klass.fhem.domain.heating.schedule.configuration.MAXConfiguration;
import li.klass.fhem.service.room.xmllist.XmlListDevice;

@Singleton
public class MAXDetailActionProvider extends DeviceDetailActionProvider {
    private final FragmentUiService fragmentUiService;

    @Inject
    public MAXDetailActionProvider(FragmentUiService fragmentUiService) {
        this.fragmentUiService = fragmentUiService;
    }

    @Override
    public List<ActionCardAction> actionsFor(Context context) {
        return ImmutableList.<ActionCardAction>of(
                new ActionCardButton(R.string.timetable, context) {
                    @Override
                    protected void onClick(XmlListDevice device, Context context) {
                        fragmentUiService.showIntervalWeekProfileFor(device, context, new MAXConfiguration());
                    }
                }
        );
    }

    @Override
    protected String getDeviceType() {
        return "MAX";
    }
}
