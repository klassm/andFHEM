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

package li.klass.fhem.domain.setlist;

import com.google.common.base.Optional;

import java.util.Locale;

class SupportsType {

    private final String type;
    private Optional<Integer> expectedLength;

    public SupportsType(String type) {
        this(type, Optional.absent());
    }

    public SupportsType(String type, int length) {
        this(type, Optional.of(length));
    }

    public SupportsType(String type, Optional<Integer> expectedLength) {
        this.type = type;
        this.expectedLength = expectedLength;
    }

    public boolean supports(String[] parts) {
        //noinspection SimplifiableIfStatement
        if (parts.length == 0 || !type.toLowerCase(Locale.getDefault()).equals(parts[0].toLowerCase(Locale.getDefault()))) {
            return false;
        }
        return !(expectedLength.isPresent() && parts.length != expectedLength.get());
    }

    public String getType() {
        return type;
    }
}
