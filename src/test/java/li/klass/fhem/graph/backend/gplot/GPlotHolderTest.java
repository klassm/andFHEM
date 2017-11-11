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

package li.klass.fhem.graph.backend.gplot;

import android.content.Context;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import li.klass.fhem.service.Command;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.testutil.MockitoRule;

import static li.klass.fhem.graph.backend.gplot.GPlotDefinitionTestdataBuilder.defaultGPlotDefinition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GPlotHolderTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @InjectMocks
    GPlotHolder gPlotHolder;

    @Mock
    GPlotParser gPlotParser;
    @Mock
    Context context;

    @Mock
    CommandExecutionService commandExecutionService;

    @Test
    public void should_get_default_definition_for_name() {
        // given
        GPlotDefinition definition = defaultGPlotDefinition();
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(ImmutableMap.of("abc", definition));

        // when
        Optional<GPlotDefinition> foundDefinition = gPlotHolder.definitionFor("abc", false, context);

        // then
        assertThat(foundDefinition).isEqualTo(Optional.of(definition));
    }

    @Test
    public void should_successfully_lookup_GPlot_file_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        GPlotDefinition definition = defaultGPlotDefinition();
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(Collections.<String, GPlotDefinition>emptyMap());
        String gplotRawDefinition = "myValue" + System.currentTimeMillis();
        given(commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(Context.class))).willReturn(Optional.of(gplotRawDefinition));
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(Optional.of(definition));

        // when
        Optional<GPlotDefinition> garden = gPlotHolder.definitionFor("garden", false, context);

        // then
        assertThat(garden).isEqualTo(Optional.of(definition));
    }

    @Test
    public void should_lookup_GPlot_file_without_success_if_current_map_does_not_yet_contain_corresponding_key() {
        // given
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(Collections.<String, GPlotDefinition>emptyMap());
        given(commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(Context.class))).willReturn(Optional.<String>absent());

        // when
        Optional<GPlotDefinition> garden = gPlotHolder.definitionFor("garden", false, context);

        // then
        assertThat(garden).isEqualTo(Optional.absent());
        verify(gPlotParser, never()).parseSafe(anyString());
    }


    @Test
    public void should_lookup_GPlot_file_only_once_if_previous_request_was_successful() {
        // given
        GPlotDefinition definition = defaultGPlotDefinition();
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(Collections.<String, GPlotDefinition>emptyMap());
        String gplotRawDefinition = "myValue" + System.currentTimeMillis();
        given(commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(Context.class))).willReturn(Optional.of(gplotRawDefinition));
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(Optional.of(definition));
        gPlotHolder.definitionFor("garden", false, context);

        // when
        gPlotHolder.definitionFor("garden", false, context);

        // then
        verify(gPlotParser, times(1)).parseSafe(anyString());
    }

    @Test
    public void should_lookup_GPlot_file_only_once_if_previous_request_was_not_successful() {
        // given
        GPlotDefinition definition = defaultGPlotDefinition();
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(Collections.<String, GPlotDefinition>emptyMap());
        String gplotRawDefinition = "myValue" + System.currentTimeMillis();
        given(commandExecutionService.executeRequest(eq("/gplot/garden.gplot"), any(Context.class))).willReturn(Optional.<String>absent());
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(Optional.of(definition));

        // when
        assertThat(gPlotHolder.definitionFor("garden", false, context).isPresent()).isFalse();

        // then
        verify(commandExecutionService, times(1)).executeRequest(anyString(), any(Context.class));
        verify(gPlotParser, never()).parseSafe(anyString());
    }

    @Test
    public void should_handle_config_db() throws Exception {
        // given
        GPlotDefinition definition = defaultGPlotDefinition();
        given(gPlotParser.getDefaultGPlotFiles()).willReturn(Collections.<String, GPlotDefinition>emptyMap());
        String gplotRawDefinition = "myValue" + System.currentTimeMillis();
        given(commandExecutionService.executeSync(eq(new Command("configdb fileshow ./www/gplot/garden.gplot")), any(Context.class))).willReturn(gplotRawDefinition);
        given(gPlotParser.parseSafe(gplotRawDefinition)).willReturn(Optional.of(definition));

        // when
        Optional<GPlotDefinition> garden = gPlotHolder.definitionFor("garden", true, context);

        // then
        assertThat(garden).isEqualTo(Optional.of(definition));

    }
}