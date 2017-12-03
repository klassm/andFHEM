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

package li.klass.fhem.update.backend.device.configuration.sanitise

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class SanitiseGeneral(
        @JsonProperty("addAttributesIfNotPresent")
        val addAttributesIfNotPresent: Set<SanitiseToAdd>? = emptySet(),
        @JsonProperty("addStatesIfNotPresent")
        val addStatesIfNotPresent: Set<SanitiseToAdd>? = emptySet(),
        @JsonProperty("addInternalsIfNotPresent")
        val addInternalsIfNotPresent: Set<SanitiseToAdd>? = emptySet(),
        @JsonProperty("addAttributeIfModelDoesNotMatch")
        val addAttributeIfModelDoesNotMatch: AddAttributeIfModelDoesNotMatch?
) : Serializable {
    operator fun plus(toAdd: SanitiseGeneral?): SanitiseGeneral {
        return SanitiseGeneral(
                addAttributesIfNotPresent = (addAttributesIfNotPresent ?: emptySet()) + (toAdd?.addAttributesIfNotPresent ?: emptySet()),
                addStatesIfNotPresent = (addStatesIfNotPresent ?: emptySet()) + (toAdd?.addStatesIfNotPresent ?: emptySet()),
                addInternalsIfNotPresent = (addInternalsIfNotPresent ?: emptySet()) + (toAdd?.addInternalsIfNotPresent ?: emptySet()),
                addAttributeIfModelDoesNotMatch = addAttributeIfModelDoesNotMatch ?: toAdd?.addAttributeIfModelDoesNotMatch
        )
    }
}

