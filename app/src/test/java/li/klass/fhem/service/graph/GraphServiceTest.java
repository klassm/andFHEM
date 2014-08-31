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

package li.klass.fhem.service.graph;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.List;

import li.klass.fhem.testutil.MockitoTestRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GraphServiceTest {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @InjectMocks
    private GraphService graphService = new GraphService();

    @Test
    public void testFindGraphEntries() {
        String content = "\n" +
                "2013-03-21_16:38:39 5.7\n" +
                "2013-03-21_16:48:49 5.9\n" +
                "2013-03-21_16:53:54 6.2\n" +
                "2013-03-21_17:01:32 5.4\n" +
                "2013-03-21_17:04:04 5.2\n" +
                "#4::\n" +
                "\n" +
                "\n";

        List<GraphEntry> graphEntries = graphService.findGraphEntries(content);

        assertThat(graphEntries.size(), is(5));
    }
}
