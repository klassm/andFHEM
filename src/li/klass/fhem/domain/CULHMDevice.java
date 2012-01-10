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

import li.klass.fhem.util.ValueExtractUtil;
import org.w3c.dom.NamedNodeMap;

public class CULHMDevice extends Device<CULHMDevice> {

    public enum SubType {
        DIMMER, BLINDACTUATOR, SWITCH
    }

    private SubType subType = null;
    private int dimProgress;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("SUBTYPE")) {
            try {
                subType = SubType.valueOf(nodeContent.toUpperCase());
            } catch (IllegalArgumentException e) {
                subType = null;
            }
        } else if (keyValue.equals("STATE")) {
            if (nodeContent.endsWith("%")) {
                dimProgress = ValueExtractUtil.extractLeadingInt(nodeContent);
            }
        }
    }

    @Override
    public boolean isSupported() {
        return subType != null;
    }

    public boolean isDimDevice() {
        return subType == SubType.DIMMER || subType == SubType.BLINDACTUATOR;
    }

    public boolean isSwitchDevice() {
        return subType == SubType.SWITCH;
    }

    public boolean isOn() {
        return state.equalsIgnoreCase("on") || state.equalsIgnoreCase("on-for-timer");
    }

    public int getDimProgress() {
        return dimProgress;
    }

    public void setDimProgress(int dimProgress) {
        this.dimProgress = dimProgress;
    }
}
