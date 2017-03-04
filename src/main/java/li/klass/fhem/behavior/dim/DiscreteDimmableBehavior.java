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

package li.klass.fhem.behavior.dim;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;

import static com.google.common.collect.FluentIterable.from;

public class DiscreteDimmableBehavior implements DimmableTypeBehavior {

    public static final Pattern DIM_STATE_PATTERN = Pattern.compile("dim[0-9]+[%]?");
    private static final Predicate<String> DIMMABLE_STATE = new Predicate<String>() {
        @Override
        public boolean apply(String input) {
            return DIM_STATE_PATTERN.matcher(input).matches();
        }
    };
    public static final Comparator<String> COMPARE_BY_DIM_VALUE = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            lhs = lhs.replace("dim", "").replace("%", "");
            rhs = rhs.replace("dim", "").replace("%", "");

            return ((Integer) Integer.parseInt(lhs)).compareTo(Integer.parseInt(rhs));
        }
    };

    private final ImmutableList<String> foundDimStates;

    public DiscreteDimmableBehavior(ImmutableList<String> foundDimStates) {
        this.foundDimStates = foundDimStates;
    }

    public ImmutableList<String> getFoundDimStates() {
        return foundDimStates;
    }

    static Optional<DiscreteDimmableBehavior> behaviorFor(SetList setList) {

        List<String> keys = setList.getSortedKeys();
        ImmutableList<String> foundDimStates = from(keys).filter(DIMMABLE_STATE).toSortedList(COMPARE_BY_DIM_VALUE);

        return foundDimStates.isEmpty() ?
                Optional.<DiscreteDimmableBehavior>absent() :
                Optional.of(new DiscreteDimmableBehavior(foundDimStates));
    }

    @Override
    public float getDimLowerBound() {
        return 0;
    }

    @Override
    public float getDimStep() {
        return 1;
    }

    @Override
    public float getCurrentDimPosition(FhemDevice device) {
        String state = device.getInternalState();
        float position = getPositionForDimState(state);
        return position == -1 ? 0 : position;
    }

    @Override
    public float getDimUpperBound() {
        return foundDimStates.size();
    }

    @Override
    public String getDimStateForPosition(FhemDevice fhemDevice, float position) {
        int pos = (int) position;
        if (pos == getDimLowerBound()) {
            return "off";
        } else if (pos == getDimUpperBound()) {
            return "on";
        }
        return foundDimStates.get(pos - 1);
    }

    @Override
    public float getPositionForDimState(String state) {
        if ("on".equalsIgnoreCase(state)) {
            return getDimUpperBound();
        } else if ("off".equalsIgnoreCase(state)) {
            return getDimLowerBound();
        }
        return foundDimStates.indexOf(state) + 1;
    }

    @Override
    public String getStateName() {
        return "state";
    }

    @Override
    public void switchTo(StateUiService stateUiService, Context context, FhemDevice fhemDevice, String connectionId, float state) {
        stateUiService.setState(fhemDevice, getDimStateForPosition(fhemDevice, state), context, connectionId);
    }
}
