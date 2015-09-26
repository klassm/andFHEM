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

package li.klass.fhem.adapter.devices.core;

import android.content.Context;

import com.google.common.base.Optional;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.testutil.MockitoRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(DataProviderRunner.class)
public class DimUpDownRowTest {
    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Mock
    StateUiService stateUiService;

    @Mock
    DimmableDevice dimmableDevice;

    @Mock
    Context context;

    DimmableAdapter.DimUpDownRow dimUpDownRow;

    @Before
    public void setUp() throws Exception {
        dimUpDownRow = new DimmableAdapter.DimUpDownRow(stateUiService);
    }

    @DataProvider
    public static Object[][] UP_DOWN_PROVIDER() {
        return new Object[][]{
                {0f, 0f, 1f, Optional.absent(), Optional.of(1)},
                {49f, 48f, 50f, Optional.of(48), Optional.of(50)},
                {100f, 99f, 100f, Optional.of(99), Optional.absent()}
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    @UseDataProvider("UP_DOWN_PROVIDER")
    public void should_dim_up(float currentProgress, float dimDownProgress, float dimUpProgress,
                              @SuppressWarnings("unused") Optional<Integer> expectedDimDownCommand,
                              Optional<Integer> expectedDimUpCommand) {
        // given
        given(dimmableDevice.getDimPosition()).willReturn(currentProgress);
        given(dimmableDevice.getDimDownPosition()).willReturn(dimDownProgress);
        given(dimmableDevice.getDimUpPosition()).willReturn(dimUpProgress);

        // when
        dimUpDownRow.onUpButtonClick(context, dimmableDevice);

        // then
        if (expectedDimUpCommand.isPresent()) {
            verify(stateUiService).setDim(dimmableDevice, expectedDimUpCommand.get(), context);
        } else {
            verify(stateUiService, never()).setDim(any(DimmableDevice.class), anyInt(), any(Context.class));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @UseDataProvider("UP_DOWN_PROVIDER")
    public void should_dim_down(float currentProgress, float dimDownProgress, float dimUpProgress,
                                Optional<Integer> expectedDimDownCommand,
                                @SuppressWarnings("unused") Optional<Integer> expectedDimUpCommand) {
        // given
        given(dimmableDevice.getDimPosition()).willReturn(currentProgress);
        given(dimmableDevice.getDimDownPosition()).willReturn(dimDownProgress);
        given(dimmableDevice.getDimUpPosition()).willReturn(dimUpProgress);

        // when
        dimUpDownRow.onDownButtonClick(context, dimmableDevice);

        // then
        if (expectedDimDownCommand.isPresent()) {
            verify(stateUiService).setDim(dimmableDevice, expectedDimDownCommand.get(), context);
        } else {
            verify(stateUiService, never()).setDim(any(DimmableDevice.class), anyInt(), any(Context.class));
        }
    }
}