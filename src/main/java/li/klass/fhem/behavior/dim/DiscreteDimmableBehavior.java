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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Pattern;

import li.klass.fhem.domain.setlist.SetList;

import static com.google.common.collect.FluentIterable.from;

class DiscreteDimmableBehavior {

    public static final Pattern DIM_STATE_PATTERN = Pattern.compile("dim[0-9]+[%]?");
    private static final Predicate<String> DIMMABLE_STATE = new Predicate<String>() {
        @Override
        public boolean apply(String input) {
            return DIM_STATE_PATTERN.matcher(input).matches();
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
        ImmutableList<String> foundDimStates = from(keys).filter(DIMMABLE_STATE).toList();

        return foundDimStates.isEmpty() ?
                Optional.<DiscreteDimmableBehavior>absent() :
                Optional.of(new DiscreteDimmableBehavior(foundDimStates));
    }
}
