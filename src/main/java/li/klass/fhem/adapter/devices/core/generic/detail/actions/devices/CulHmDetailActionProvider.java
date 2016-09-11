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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.DeviceDetailActionProvider;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardAction;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.action_card.ActionCardButton;
import li.klass.fhem.adapter.devices.core.generic.detail.actions.state.StateAttributeAction;
import li.klass.fhem.adapter.devices.genericui.CustomViewTableRow;
import li.klass.fhem.adapter.uiservice.FragmentUiService;
import li.klass.fhem.domain.CulHmHeatingMode;
import li.klass.fhem.domain.GenericDevice;
import li.klass.fhem.domain.heating.schedule.configuration.CULHMConfiguration;
import li.klass.fhem.service.room.xmllist.XmlListDevice;
import li.klass.fhem.widget.LitreContentView;

import static li.klass.fhem.domain.CulHmHeatingMode.heatingModeFor;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingDouble;
import static li.klass.fhem.util.ValueExtractUtil.extractLeadingInt;

@Singleton
public class CulHmDetailActionProvider extends DeviceDetailActionProvider {

    public static final String MODE_STATE_NAME = "controlMode";
    private final FragmentUiService fragmentUiService;

    public static double MINIMUM_TEMPERATURE = 5.5;

    @Inject
    public CulHmDetailActionProvider(FragmentUiService fragmentUiService) {
        this.fragmentUiService = fragmentUiService;
        addStateAttributeAction(MODE_STATE_NAME, new CulHmHeatingModeDetailAction());
        addStateAttributeAction("state", new KFM100ContentView());
    }

    @Override
    protected String getDeviceType() {
        return "CUL_HM";
    }

    @Override
    public List<ActionCardAction> actionsFor(Context context) {
        return ImmutableList.<ActionCardAction>of(
                new ActionCardButton(R.string.timetable, context) {
                    @Override
                    protected void onClick(XmlListDevice device, Context context) {
                        fragmentUiService.showIntervalWeekProfileFor(device, context, new CULHMConfiguration());
                    }

                    @Override
                    public boolean supports(GenericDevice genericDevice) {
                        return supportsHeating(genericDevice.getXmlListDevice());
                    }
                }
        );
    }

    private static boolean supportsHeating(XmlListDevice xmlListDevice) {
        Optional<String> controlMode = xmlListDevice.getState(MODE_STATE_NAME);
        return controlMode.isPresent() && heatingModeFor(controlMode.get()).isPresent();
    }

    private static class CulHmHeatingModeDetailAction extends HeatingModeDetailAction<CulHmHeatingMode> {

        @Override
        protected CulHmHeatingMode getCurrentModeFor(XmlListDevice device) {
            return heatingModeFor(device.getState(MODE_STATE_NAME).get()).get();
        }

        @Override
        protected CulHmHeatingMode[] getAvailableModes() {
            return CulHmHeatingMode.values();
        }

        @Override
        public boolean supports(XmlListDevice xmlListDevice) {
            return CulHmDetailActionProvider.supportsHeating(xmlListDevice);
        }
    }

    private static class KFM100ContentView implements StateAttributeAction {

        @Override
        public TableRow createRow(XmlListDevice device, String key, String stateValue, final Context context, LayoutInflater inflater, ViewGroup parent) {
            String model = device.getAttribute("model").get();
            final double fillContentPercentage = determineContentPercentage(device, model);


            return new CustomViewTableRow() {
                @Override
                public View getContentView() {
                    return new LitreContentView(context, fillContentPercentage);
                }
            }.createRow(inflater, parent);
        }

        private double determineContentPercentage(XmlListDevice device, String model) {
            double fillContentPercentage;
            if ("HM-Sen-Wa-Od".equals(model)) {
                fillContentPercentage = extractLeadingDouble(device.getState("level").get()) / 100d;
            } else {
                String rawToReadable = device.getAttribute("rawToReadable").get();
                double maximum = 0;
                double content;
                String[] parts = parseRawToReadable(rawToReadable);
                if (parts.length == 2) {
                    maximum = extractLeadingInt(parts[1]);
                }

                content = extractLeadingDouble(device.getState("content").get());
                if (content > maximum) {
                    content = maximum;
                }

                fillContentPercentage = content / maximum;

            }
            return fillContentPercentage;
        }

        private String[] parseRawToReadable(String value) {
            int lastSpace = value.lastIndexOf(" ");
            String lastDefinition = lastSpace == -1 ? value : value.substring(lastSpace + 1);
            return lastDefinition.split(":");
        }

        @Override
        public boolean supports(XmlListDevice xmlListDevice) {
            Optional<String> modelOpt = xmlListDevice.getAttribute("model");
            if (!modelOpt.isPresent()) {
                return false;
            }

            String model = modelOpt.get();
            return ("HM-Sen-Wa-Od".equalsIgnoreCase(model) && xmlListDevice.containsState("level"))
                    || (xmlListDevice.containsAttribute("rawToReadable") && xmlListDevice.containsState("content"));

        }
    }
}
