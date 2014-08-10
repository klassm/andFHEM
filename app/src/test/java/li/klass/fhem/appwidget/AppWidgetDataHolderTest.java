package li.klass.fhem.appwidget;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Map;
import java.util.Set;

import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.appwidget.view.widget.base.AppWidgetView;
import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
import li.klass.fhem.testutil.MockitoTestRule;
import li.klass.fhem.util.ApplicationProperties;
import li.klass.fhem.util.SharedPreferencesUtil;

import static li.klass.fhem.appwidget.AppWidgetDataHolder.SAVE_PREFERENCE_NAME;
import static li.klass.fhem.appwidget.WidgetConfiguration.fromSaveString;
import static li.klass.fhem.constants.PreferenceKeys.ALLOW_REMOTE_UPDATE;
import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(AndFHEMRobolectricTestRunner.class)
public class AppWidgetDataHolderTest {
    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences.Editor sharedPreferencesEditor;
    @Mock
    private SharedPreferencesUtil sharedPreferencesUtil;
    @Mock
    private Context context;
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
    @InjectMocks
    @Spy
    private AppWidgetDataHolder holder;

    @SuppressLint("CommitPrefEdits")
    @Before
    public void before() {
        given(sharedPreferencesUtil.getSharedPreferences(SAVE_PREFERENCE_NAME))
                .willReturn(sharedPreferences);
        given(sharedPreferencesUtil.getSharedPreferencesEditor(SAVE_PREFERENCE_NAME))
                .willReturn(sharedPreferencesEditor);
        given(sharedPreferences.edit()).willReturn(sharedPreferencesEditor);
        given(context.getSystemService(Context.ALARM_SERVICE)).willReturn(alarmManager);
    }

    @Test
    public void should_find_all_widget_ids() {
        // given
        BDDMockito.<Map<String, ?>>given(sharedPreferences.getAll()).willReturn(ImmutableMap.of("a", 1, "b", 2));

        // when
        Set<String> ids = holder.getAllAppWidgetIds();

        // then
        assertThat(ids).containsExactly("a", "b");
    }

    @Test
    public void should_return_absent_for_unsaved_widget_id() {
        // given
        BDDMockito.<Map<String, ?>>given(sharedPreferences.getAll()).willReturn(ImmutableMap.of("1", 1, "2", 2));

        // when
        Optional<WidgetConfiguration> widgetConfiguration = holder.getWidgetConfiguration(3);

        // then
        assertThat(widgetConfiguration).isEqualTo(Optional.<WidgetConfiguration>absent());
    }

    @Test
    public void should_return_WidgetConfiguration() {
        // given
        String saveString = "123#" + WidgetType.STATUS.name();
        WidgetConfiguration widgetConfiguration = fromSaveString(saveString);
        given(sharedPreferences.getString("1", null)).willReturn(saveString);

        // when
        Optional<WidgetConfiguration> result = holder.getWidgetConfiguration(1);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(widgetConfiguration);
    }

    @Test
    public void should_update_old_WidgetConfigurations() {
        // given
        String saveString = "123#myDevice#" + WidgetType.STATUS.name();
        WidgetConfiguration widgetConfiguration = fromSaveString(saveString);
        given(sharedPreferences.getString("123", null)).willReturn(saveString);

        // when
        Optional<WidgetConfiguration> result = holder.getWidgetConfiguration(123);

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(widgetConfiguration);
        verify(sharedPreferencesEditor).putString("123", widgetConfiguration.toSaveString());
        verify(sharedPreferencesEditor).apply();
    }

    @Test
    public void should_save_WidgetConfiguration_to_preferences() {
        // given
        String saveString = "123#" + WidgetType.STATUS.name();
        WidgetConfiguration widgetConfiguration = fromSaveString(saveString);

        // when
        holder.saveWidgetConfigurationToPreferences(widgetConfiguration);

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
        verify(alarmManager).cancel(any(PendingIntent.class));
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
        verify(alarmManager).cancel(any(PendingIntent.class));
    }

    @Test
    public void should_update_widget() {
        // given
        given(sharedPreferences.getString("123", null)).willReturn("123#" + WidgetType.STATUS.name());
        doReturn(appWidgetView).when(holder).getAppWidgetView(any(WidgetConfiguration.class));
        doReturn(456L).when(holder).getConnectionDependentUpdateInterval(context);
        given(applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true)).willReturn(true);
        given(appWidgetView.createView(any(Context.class), any(WidgetConfiguration.class), anyLong()))
                .willReturn(remoteViews);

        // when
        holder.updateWidgetInCurrentThread(appWidgetManager, context, 123, true);

        // then
        verify(appWidgetView).createView(eq(context), any(WidgetConfiguration.class), eq(456L));
        verify(appWidgetManager).updateAppWidget(123, remoteViews);
    }

    @Test
    public void should_set_update_interval_to_NEVER_UPDATE_if_remote_calls_are_disabled_in_method_call_parameter() {
        // given
        given(sharedPreferences.getString("123", null)).willReturn("123#" + WidgetType.STATUS.name());
        doReturn(appWidgetView).when(holder).getAppWidgetView(any(WidgetConfiguration.class));
        doReturn(456L).when(holder).getConnectionDependentUpdateInterval(context);
        given(applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true)).willReturn(true);
        given(appWidgetView.createView(any(Context.class), any(WidgetConfiguration.class), anyLong()))
                .willReturn(remoteViews);

        // when
        holder.updateWidgetInCurrentThread(appWidgetManager, context, 123, false);

        // then
        verify(appWidgetView).createView(eq(context), any(WidgetConfiguration.class), eq(NEVER_UPDATE_PERIOD));
        verify(appWidgetManager).updateAppWidget(123, remoteViews);
    }

    @Test
    public void should_set_update_interval_to_NEVER_UPDATE_if_remote_calls_are_disabled_in_preferences() {
        // given
        given(sharedPreferences.getString("123", null)).willReturn("123#" + WidgetType.STATUS.name());
        doReturn(appWidgetView).when(holder).getAppWidgetView(any(WidgetConfiguration.class));
        doReturn(456L).when(holder).getConnectionDependentUpdateInterval(context);
        given(applicationProperties.getBooleanSharedPreference(ALLOW_REMOTE_UPDATE, true)).willReturn(false);
        given(appWidgetView.createView(any(Context.class), any(WidgetConfiguration.class), anyLong()))
                .willReturn(remoteViews);

        // when
        holder.updateWidgetInCurrentThread(appWidgetManager, context, 123, true);

        // then
        verify(appWidgetView).createView(eq(context), any(WidgetConfiguration.class), eq(NEVER_UPDATE_PERIOD));
        verify(appWidgetManager).updateAppWidget(123, remoteViews);
    }

    @Test
    public void should_delete_widget_during_update_widget_if_configuration_cannot_be_found() {
        // given
        doNothing().when(holder).deleteWidget(context, 123);
        doReturn(Optional.<WidgetConfiguration>absent()).when(holder).getWidgetConfiguration(123);

        // when
        holder.updateWidgetInCurrentThread(appWidgetManager, context, 123, true);

        // then
        verify(holder).deleteWidget(context, 123);
    }
}