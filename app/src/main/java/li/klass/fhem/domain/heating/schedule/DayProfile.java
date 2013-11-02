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

package li.klass.fhem.domain.heating.schedule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.heating.schedule.configuration.HeatingConfiguration;
import li.klass.fhem.domain.heating.schedule.interval.BaseHeatingInterval;
import li.klass.fhem.util.DayUtil;

public class DayProfile<H extends BaseHeatingInterval, D extends Device<D>, C extends HeatingConfiguration<H, D, C>> implements Serializable {
    private DayUtil.Day day;
    private List<H> heatingIntervals = new ArrayList<H>();
    private List<H> deletedIntervals = new ArrayList<H>();

    private final C heatingConfiguration;

    public DayProfile(DayUtil.Day day, C configuration) {
        this.day = day;
        this.heatingConfiguration = configuration;

        if (heatingConfiguration.numberOfIntervalsType == HeatingConfiguration.NumberOfIntervalsType.FIXED) {
            for (int i = 0; i < getMaximumNumberOfHeatingIntervals(); i++) {
                heatingIntervals.add(configuration.createHeatingInterval());
            }
        }
    }

    public DayUtil.Day getDay() {
        return day;
    }

    public boolean addHeatingInterval(H interval) {
        if (canAddHeatingInterval()) return false;
        heatingIntervals.add(interval);
        return true;
    }

    public boolean deleteHeatingIntervalAt(int position) {
        if (position > heatingIntervals.size()) return false;

        H interval = heatingIntervals.get(position);
        deletedIntervals.add(interval);

        return heatingIntervals.remove(position) != null;
    }

    public H getHeatingIntervalAt(int position) {
        if (position >= heatingIntervals.size()) return null;

        return heatingIntervals.get(position);
    }

    public int getNumberOfHeatingIntervals() {
        return heatingIntervals.size();
    }

    public List<H> getHeatingIntervals() {
        return Collections.unmodifiableList(heatingIntervals);
    }

    public boolean canAddHeatingInterval() {
        if (heatingConfiguration.numberOfIntervalsType == HeatingConfiguration.NumberOfIntervalsType.FIXED) {
            return false;
        }

        int maximumNumberOfHeatingIntervals = getMaximumNumberOfHeatingIntervals();
        return maximumNumberOfHeatingIntervals != -1 && heatingIntervals.size() >= maximumNumberOfHeatingIntervals;
    }

    public boolean isModified() {
        for (H heatingInterval : heatingIntervals) {
            if (heatingInterval.isModified()) {
                return true;
            }
        }

        return !deletedIntervals.isEmpty();
    }

    public int getMaximumNumberOfHeatingIntervals() {
        return heatingConfiguration.maximumNumberOfHeatingIntervals;
    }

    public void acceptChanges() {
        for (H heatingInterval : heatingIntervals) {
            heatingInterval.acceptChanges();
        }
        Collections.sort(heatingIntervals);
        deletedIntervals.clear();
    }

    public void reset() {
        Iterator<H> iterator = heatingIntervals.iterator();
        while (iterator.hasNext()) {
            H heatingInterval = iterator.next();

            heatingInterval.reset();
            if (heatingInterval.isNew()) {
                iterator.remove();
            }
        }

        for (H deletedInterval : deletedIntervals) {
            if (!deletedInterval.isNew()) {
                heatingIntervals.add(deletedInterval);
            }
        }
        deletedIntervals.clear();

        Collections.sort(heatingIntervals);
    }
}
