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

package li.klass.fhem.adapter.rooms;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class GroupComparatorTest {

    private static final String UNKNOWN = "unknown";

    @DataProvider
    public static Object[][] parentsOrderProvider() {

        return new Object[][]{
                {
                        newArrayList("a", "b", "c"),
                        newArrayList("b", "a", "c"),
                        newArrayList("a", "b", "c")
                },
                {
                        newArrayList("c", "b", "a"),
                        newArrayList("b", "a", "c"),
                        newArrayList("c", "b", "a")
                },
                {
                        newArrayList("c", "b", UNKNOWN),
                        newArrayList("b", "a", "c"),
                        newArrayList("c", "b", "a")
                },
                {
                        newArrayList(UNKNOWN, "b", "c"),
                        newArrayList("b", "a", "c"),
                        newArrayList("a", "b", "c")
                },
                {
                        newArrayList(UNKNOWN, "b", "c"),
                        newArrayList("b", "d", "a", "c"),
                        newArrayList("a", "d", "b", "c")
                }
        };
    }

    @UseDataProvider("parentsOrderProvider")
    @Test
    public void should_sort_parents(List<String> deviceGroupParents, List<String> toSort, List<String> expectedOutput) {
        // given
        GroupComparator comparator = new GroupComparator(UNKNOWN, deviceGroupParents);

        // when
        Collections.sort(toSort, comparator);

        // then
        assertThat(toSort).isEqualTo(expectedOutput);
    }
}