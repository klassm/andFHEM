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
package li.klass.fhem.adapter.rooms

import com.tngtech.java.junit.dataprovider.DataProvider
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.tngtech.java.junit.dataprovider.UseDataProvider
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(DataProviderRunner::class)
class GroupComparatorTest {
    @UseDataProvider("parentsOrderProvider")
    @Test
    fun should_sort_parents(fixture: Fixture) {
        // given
        val comparator = GroupComparator(UNKNOWN, fixture.deviceGroupParents)

        // when
        Collections.sort(fixture.toSort, comparator)

        // then
        Assertions.assertThat(fixture.toSort).isEqualTo(fixture.expectedOutput)
    }

    data class Fixture(val deviceGroupParents: List<String>, val toSort: MutableList<String>, val expectedOutput: List<String>)

    companion object {
        private const val UNKNOWN = "unknown"

        @DataProvider
        @JvmStatic
        fun parentsOrderProvider(): List<Fixture> = listOf(
                Fixture(
                        deviceGroupParents = listOf("a", "b", "c"),
                        toSort = mutableListOf("b", "a", "c"),
                        expectedOutput = listOf("a", "b", "c")
                ),
                Fixture(
                        deviceGroupParents = listOf("c", "b", "a"),
                        toSort = mutableListOf("b", "a", "c"),
                        expectedOutput = listOf("c", "b", "a")
                ),
                Fixture(
                        deviceGroupParents = listOf("c", "b", UNKNOWN),
                        toSort = mutableListOf("b", "a", "c"),
                        expectedOutput = listOf("c", "b", "a")
                ),
                Fixture(
                        deviceGroupParents = listOf(UNKNOWN, "b", "c"),
                        toSort = mutableListOf("b", "a", "c"),
                        expectedOutput = listOf("a", "b", "c")
                ),
                Fixture(
                        deviceGroupParents = listOf(UNKNOWN, "b", "c"),
                        toSort = mutableListOf("b", "d", "a", "c"),
                        expectedOutput = listOf("a", "d", "b", "c")
                )
        )
    }
}