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

import li.klass.fhem.R;
import li.klass.fhem.domain.genericview.ShowInDetail;
import li.klass.fhem.domain.genericview.ShowInOverview;
import li.klass.fhem.domain.genericview.ViewSettings;
import org.w3c.dom.NamedNodeMap;

@ViewSettings(showState = true)
public class RFXX10RECDevice extends Device<RFXX10RECDevice> {
    @ShowInOverview(description = R.string.lastStateChange)
    @ShowInDetail(description = R.string.lastStateChange)
    private String lastStateChangeTime;

    @ShowInOverview(description = R.string.lastState)
    @ShowInDetail(description = R.string.lastState)
    private String lastState;

    @Override
    protected void onChildItemRead(String tagName, String keyValue, String nodeContent, NamedNodeMap attributes) {
        if (keyValue.equals("TIME")) {
            measured = nodeContent;
        } else if (keyValue.equals("STATECHANGE")) {
            lastStateChangeTime = attributes.getNamedItem("measured").getNodeValue();
            lastState = nodeContent;
        }
    }

    public String getLastStateChangedTime() {
        return lastStateChangeTime;
    }

    public String getLastState() {
        return lastState;
    }
}
