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

public abstract class HookedDevice extends Device {

    /**
     * Contains a name only used in widgets.
     */
    @XmllistAttribute("WIDGET_NAME")
    protected String widgetName;

    /**
     * Hides or shows a devices everywhere within this application.
     */
    protected boolean alwaysHidden = false;

    /**
     * Optionally contains some sortBy attribute that is used for device ordering.
     */
    @XmllistAttribute("SORTBY")
    protected String sortBy;

    @XmllistAttribute("ALWAYS_HIDDEN")
    public void setAlwaysHidden(String value) {
        alwaysHidden = "true".equalsIgnoreCase(value);
    }

    public String getSortBy() {
        return sortBy;
    }

    public boolean isSupported() {
        return !alwaysHidden;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

}
