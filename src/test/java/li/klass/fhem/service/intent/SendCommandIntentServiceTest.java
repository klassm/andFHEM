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

package li.klass.fhem.service.intent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.connection.ConnectionService;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.preferences.SharedPreferencesService;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static li.klass.fhem.service.intent.SendCommandIntentService.COMMANDS_JSON_PROPERTY;
import static li.klass.fhem.service.intent.SendCommandIntentService.COMMANDS_PROPERTY;
import static li.klass.fhem.service.intent.SendCommandIntentService.PREFERENCES_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(DataProviderRunner.class)
public class SendCommandIntentServiceTest {

    @Mock
    ConnectionService connectionService;
    @Mock
    CommandExecutionService commandExecutionService;
    @Mock
    SharedPreferencesService sharedPreferencesService;
    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor editor;

    @InjectMocks
    SendCommandIntentService intentService;

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();


    @DataProvider
    public static Object[][] recentCommandsProvider() {
        return $$(
                $(String.format("{%s: %s}", COMMANDS_JSON_PROPERTY, "['a', 'b', 'c']"), ImmutableList.of("a", "b", "c")),
                $(null, Collections.<String>emptyList()),
                $("{", Collections.<String>emptyList())
        );
    }

    @Test
    @UseDataProvider("recentCommandsProvider")
    public void should_get_recent_commands(String jsonInput, List<String> expectedCommands) {
        given(sharedPreferencesService.getPreferences(eq(PREFERENCES_NAME), any(Context.class)))
                .willReturn(sharedPreferences);
        given(sharedPreferences.getString(COMMANDS_PROPERTY, null)).willReturn(jsonInput);

        ArrayList<String> result = intentService.getRecentCommands();

        assertThat(result).containsExactlyElementsOf(expectedCommands);
        verifyZeroInteractions(commandExecutionService);
        verifyZeroInteractions(connectionService);
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    public void should_execute_command() {
        String command = "myCommmand";
        String connectionId = "myConnectionId";
        given(connectionService.exists(eq(connectionId), any(Context.class))).willReturn(true);
        given(sharedPreferencesService.getPreferences(eq(PREFERENCES_NAME), any(Context.class)))
                .willReturn(sharedPreferences);
        given(sharedPreferences.getString(COMMANDS_PROPERTY, null)).willReturn(String.format("{%s: %s}", COMMANDS_JSON_PROPERTY, "['a', 'b', 'c']"));
        given(sharedPreferences.edit()).willReturn(editor);
        given(editor.putString(anyString(), anyString())).willReturn(editor);

        intentService.executeCommand(command, Optional.of(connectionId));

        verify(commandExecutionService).executeSafely(eq(command), eq(Optional.of(connectionId)), any(Context.class));
        InOrder inOrder = Mockito.inOrder(editor);
        inOrder.verify(editor).putString(COMMANDS_PROPERTY, String.format("{\"%s\":%s}", COMMANDS_JSON_PROPERTY, "[\"" + command + "\",\"a\",\"b\",\"c\"]"));
        inOrder.verify(editor).apply();
    }
}