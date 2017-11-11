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

package li.klass.fhem.room.list.backend.deviceConfiguration;

import java.io.Serializable;
import java.util.Set;

public class ViewItemConfig implements Serializable {
    private final Set<String> markers;
    String key;
    String desc;
    String showAfter;
    boolean showDelayNotificationOnSwitch = false;
    boolean showInOverview = false;
    boolean showInDetail = false;

    private ViewItemConfig(Builder builder) {
        markers = builder.markers;
        key = builder.key;
        desc = builder.desc;
        showAfter = builder.showAfter;
        showDelayNotificationOnSwitch = builder.showDelayNotificationOnSwitch;
        showInOverview = builder.showInOverview;
        showInDetail = builder.showInDetail;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isShowInOverview() {
        return showInOverview;
    }

    public Set<String> getMarkers() {
        return markers;
    }

    public String getShowAfter() {
        return showAfter;
    }

    public boolean isShowInDetail() {
        return showInDetail;
    }

    public boolean isShowDelayNotificationOnSwitch() {
        return showDelayNotificationOnSwitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewItemConfig state = (ViewItemConfig) o;

        return !(key != null ? !key.equals(state.key) : state.key != null);

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }


    public static final class Builder {
        private Set<String> markers;
        private String key;
        private String desc;
        private String showAfter;
        private boolean showDelayNotificationOnSwitch;
        private boolean showInOverview;
        private boolean showInDetail;

        public Builder() {
        }

        public Builder withMarkers(Set<String> val) {
            markers = val;
            return this;
        }

        public Builder withKey(String val) {
            key = val;
            return this;
        }

        public Builder withDesc(String val) {
            desc = val;
            return this;
        }

        public Builder withShowAfter(String val) {
            showAfter = val;
            return this;
        }

        public Builder withShowDelayNotificationOnSwitch(boolean val) {
            showDelayNotificationOnSwitch = val;
            return this;
        }

        public Builder withShowInOverview(boolean val) {
            showInOverview = val;
            return this;
        }

        public Builder withShowInDetail(boolean val) {
            showInDetail = val;
            return this;
        }

        public ViewItemConfig build() {
            return new ViewItemConfig(this);
        }
    }
}
