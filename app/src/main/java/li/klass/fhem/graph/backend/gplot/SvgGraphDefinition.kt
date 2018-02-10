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

package li.klass.fhem.graph.backend.gplot

import com.google.common.collect.ComparisonChain
import java.io.Serializable
import java.util.*
import java.util.regex.Pattern

data class SvgGraphDefinition(val name: String, val plotDefinition: GPlotDefinition, val logDeviceName: String, private val labels: List<String>, val title: String, val plotfunction: List<String>) : Serializable {

    internal fun formatText(toFormat: String): String {
        var result = toFormat.replace("<TL>".toRegex(), title)
        val matcher = LABEL_PATTERN.matcher(toFormat)
        while (matcher.find()) {
            val labelIndex = Integer.parseInt(matcher.group(1)) - 1
            val replaceBy = if (labelIndex < labels.size) labels[labelIndex].trim { it <= ' ' } else ""
            result = result.replace(matcher.group(0).toRegex(), replaceBy)
        }
        return result
    }

    companion object {

        private val LABEL_PATTERN = Pattern.compile("<L([0-9]+)>")
        val BY_NAME: Comparator<SvgGraphDefinition> = Comparator { o1, o2 -> ComparisonChain.start().compare(o1.name!!, o2.name!!).result() }
    }
}
