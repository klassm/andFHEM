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

package li.klass.fhem.domain;

import android.util.Log;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.DetailOverviewViewSettings;
import li.klass.fhem.util.ArrayUtil;
import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

import java.util.Arrays;

@DetailOverviewViewSettings(showState = true)
@SuppressWarnings("unused")
public class DummyDevice extends DimmableDevice<DummyDevice> {

    private boolean timerDevice = false;
    private Integer dimLowerBound;
    private Integer dimStep;
    private Integer dimUpperBound;

    public void readSTATE(String tagName, String value, NamedNodeMap attributes) {
        this.measured = attributes.getNamedItem("measured").getNodeValue();
    }

    @Override
    public boolean supportsToggle() {
        return ArrayUtil.contains(getAvailableTargetStates(), "on", "off");
    }

    @Override
    public boolean isOnByState() {
        return super.isOnByState() || getState().equalsIgnoreCase("on");
    }

    @Override
    public void afterXMLRead() {
        super.afterXMLRead();

        String[] availableTargetStates = getAvailableTargetStates();
        if (availableTargetStates == null) return;

        if (ArrayUtil.contains(availableTargetStates, "time")) {
            timerDevice = true;
        }

        for (int i = 0; i < availableTargetStates.length; i++) {
            String targetState = availableTargetStates[i];

            try {
                if (targetState.equals("slider")) {
                    dimLowerBound = Integer.valueOf(availableTargetStates[i + 1]);
                    dimStep = Integer.valueOf(availableTargetStates[i + 2]);
                    dimUpperBound = Integer.valueOf(availableTargetStates[i + 3]);
                }
            } catch (Exception e) {
                Log.e(DummyDevice.class.getName(), "cannot parse slider in " + Arrays.asList(availableTargetStates), e);
            }
        }
    }

    public boolean isTimerDevice() {
        return timerDevice;
    }

    @Override
    public int getDimLowerBound() {
        return dimLowerBound;
    }

    @Override
    public int getDimUpperBound() {
        return dimUpperBound;
    }

    @Override
    public int getDimStep() {
        return dimStep;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return position + "";
    }

    @Override
    public int getPositionForDimState(String dimState) {
        try {
            return Integer.valueOf(dimState.trim());
        } catch (Exception e) {
            Log.e(DummyDevice.class.getName(), "cannot parse dimState " + dimState, e);
            return 0;
        }
    }

    @Override
    public boolean supportsDim() {
        return dimLowerBound != null && dimUpperBound != null && dimStep != null;
    }
}
