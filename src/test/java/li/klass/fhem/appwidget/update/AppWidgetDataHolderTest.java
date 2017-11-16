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

package li.klass.fhem.appwidget.update;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import li.klass.fhem.appwidget.ui.widget.WidgetType;
import li.klass.fhem.appwidget.ui.widget.base.AppWidgetView;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.preferences.SharedPreferencesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AppWidgetDataHolderTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor sharedPreferencesEditor;
    @Mock
    private SharedPreferencesService sharedPreferencesService;
    @Mock
    private AlarmManager alarmManager;
    @Mock
    private AppWidgetHost appWidgetHost;
    @Mock
    private AppWidgetManager appWidgetManager;
    @Mock
    private AppWidgetView appWidgetView;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private RemoteViews remoteViews;

    private Context context;

    @InjectMocks
    @Spy
    private AppWidgetDataHolder holder;

    @SuppressLint("CommitPrefEdits")
    @Before
    public void before() {
        context = mock(Context.class);
        given(sharedPreferencesService.getPreferences(AppWidgetDataHolder.Companion.getSAVE_PREFERENCE_NAME(), context))
                .willReturn(sharedPreferences);
        given(sharedPreferencesService.getSharedPreferencesEditor(AppWidgetDataHolder.Companion.getSAVE_PREFERENCE_NAME(), context))
                .willReturn(sharedPreferencesEditor);
        given(sharedPreferences.edit()).willReturn(sharedPreferencesEditor);
        given(context.getSystemService(Context.ALARM_SERVICE)).willReturn(alarmManager);
    }

    @Test
    public void should_find_all_widget_ids() {
        // given
        BDDMockito.<Map<String, ?>>given(sharedPreferences.getAll()).willReturn(ImmutableMap.of("a", 1, "b", 2));

        // when
        Set<String> ids = holder.getAllAppWidgetIds(context);

        // then
        assertThat(ids).containsExactly("a", "b");
    }

    @Test
    public void should_return_absent_for_unsaved_widget_id() {
        // given
        BDDMockito.<Map<String, ?>>given(sharedPreferences.getAll()).willReturn(ImmutableMap.of("1", 1, "2", 2));

        // when
        Optional<WidgetConfiguration> widgetConfiguration = holder.getWidgetConfiguration(3, context);

        // then
        assertThat(widgetConfiguration).isEqualTo(Optional.<WidgetConfiguration>absent());
    }

    @Test
    public void should_return_WidgetConfiguration() {
        // given
        WidgetConfiguration widgetConfiguration = new WidgetConfiguration(1, WidgetType.UPDATE_WIDGET, Optional.<String>absent(), Collections.<String>emptyList());
        given(sharedPreferences.getString("1", null)).willReturn(widgetConfiguration.toSaveString());

        // when
        Optional<WidgetConfiguration> result = holder.getWidgetConfiguration(1, context);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(widgetConfiguration);
    }

    @Test
    public void should_save_WidgetConfiguration_to_preferences() {
        // given
        WidgetConfiguration widgetConfiguration = new WidgetConfiguration(123, WidgetType.UPDATE_WIDGET, Optional.<String>absent(), Collections.<String>emptyList());

        // when
        holder.saveWidgetConfigurationToPreferences(widgetConfiguration, context);

        // then
        verify(sharedPreferencesEditor).putString("123", widgetConfiguration.toSaveString());
    }

    @Test
    public void should_delete_widgets() {
        // given
        given(sharedPreferences.contains("123")).willReturn(true);
        given(sharedPreferencesEditor.remove("123")).willReturn(sharedPreferencesEditor);
        doReturn(appWidgetHost).when(holder).getAppWidgetHost(context);

        // when
        holder.deleteWidget(context, 123);

        // then
        verify(sharedPreferencesEditor).remove("123");
        verify(appWidgetHost).deleteAppWidgetId(123);
    }

    @Test
    public void should_only_delete_widget_from_AppWidgetHost_if_contained_in_saved_preferences() {
        // given
        given(sharedPreferences.contains("123")).willReturn(false);
        given(sharedPreferencesEditor.remove("123")).willReturn(sharedPreferencesEditor);
        doReturn(appWidgetHost).when(holder).getAppWidgetHost(context);

        // when
        holder.deleteWidget(context, 123);

        // then
        verifyNoMoreInteractions(sharedPreferencesEditor);
        verifyNoMoreInteractions(appWidgetHost);
    }
}