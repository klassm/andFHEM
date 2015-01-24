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

package li.klass.fhem.domain.core;

public abstract class HookedDevice<T extends HookedDevice<T>> extends Device<T> {

    /**
     * Contains a name only used in widgets.
     */
    protected String widgetName;

    /**
     * Hides or shows a devices everywhere within this application.
     */
    protected boolean alwaysHidden = false;

    /**
     * Provides some pronunciation for only this device.
     */
    protected String pronunciation;

    @XmllistAttribute("WIDGET_NAME")
    public void setWidgetName(String value) {
        this.widgetName = value;
    }

    @XmllistAttribute("ALWAYS_HIDDEN")
    public void setAlwaysHidden(String value) {
        alwaysHidden = "true".equalsIgnoreCase(value);
    }

    @XmllistAttribute("PRONUNCIATION")
    public void setPronunciation(String value) {
        this.pronunciation = value;
    }

    public String getPronunciation() {
        return pronunciation;
    }


    public boolean isSupported() {
        return !alwaysHidden;
    }
}
