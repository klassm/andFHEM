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

package li.klass.fhem.service.importexport;

import android.content.Context;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;

import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.ApplicationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

public class ImportExportServiceTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    Context applicationContext;

    @InjectMocks
    ImportExportService importExportService;

    @Before
    public void setUp() {
        given(applicationProperties.getApplicationSharedPreferencesName(applicationContext)).willReturn("abc");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_match_export_and_import_values() {
        // given
        Map<String, Object> values = ImmutableMap.<String, Object>builder()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1.f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .build();

        // when
        Map<String, Object> converted = (Map<String, Object>) importExportService.toImportValues(importExportService.toExportValues(values));

        // then
        assertThat(converted).isEqualTo(values);
    }

    @Test
    public void should_export_values() {
        // given
        Map<String, Object> values = ImmutableMap.<String, Object>builder()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1.0f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .build();

        // when
        Map<String, String> exported = importExportService.toExportValues(values);

        // then
        assertThat(exported).isEqualTo(ImmutableMap.<String, String>builder()
                .put("a", "1/java.lang.String")
                .put("b", "1/java.lang.Integer")
                .put("c", "1.0/java.lang.Double")
                .put("f", "1.0/java.lang.Float")
                .put("d", "1.0/java.lang.String")
                .put("e", "anc/java.lang.String")
                .put("g", "anc/bas/java.lang.String")
                .build());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_import_values() {
        Map<String, String> values = ImmutableMap.<String, String>builder()
                .put("a", "1/java.lang.String")
                .put("b", "1/java.lang.Integer")
                .put("c", "1.0/java.lang.Double")
                .put("f", "1.0/java.lang.Float")
                .put("d", "1.0/java.lang.String")
                .put("e", "anc/java.lang.String")
                .put("g", "anc/bas/java.lang.String")
                .build();

        // when
        Map<String, Object> imported = (Map<String, Object>) importExportService.toImportValues(values);

        // then
        assertThat(imported).isEqualTo(ImmutableMap.<String, Object>builder()
                .put("a", "1")
                .put("b", 1)
                .put("c", 1.0)
                .put("f", 1.0f)
                .put("d", "1.0")
                .put("e", "anc")
                .put("g", "anc/bas")
                .build());
    }
}