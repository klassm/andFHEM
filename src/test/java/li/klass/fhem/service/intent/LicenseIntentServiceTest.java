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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import li.klass.fhem.billing.BillingService;
import li.klass.fhem.testutil.MockitoRule;
import li.klass.fhem.util.ApplicationProperties;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LicenseIntentServiceTest {

    @Mock
    BillingService billingService;

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    Context applicationContext;

    @InjectMocks
    LicenseIntentService licenseIntentService;

    @Rule
    public MockitoRule mockitoRule = new MockitoRule();

    @Test
    public void should_handle_is_premium_even_if_inventory_loading_was_not_successful() throws Exception {
        // given
        PackageManager packageManager = mock(PackageManager.class);
        given(packageManager.getPackageInfo(anyString(), anyInt())).willReturn(new PackageInfo());
        given(applicationContext.getPackageName()).willReturn("li.klass.fhem");
        given(applicationContext.getPackageManager()).willReturn(packageManager);
        LicenseIntentService.IsPremiumListener listener = mock(LicenseIntentService.IsPremiumListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BillingService.OnLoadInventoryFinishedListener finishedListener = (BillingService.OnLoadInventoryFinishedListener) invocation.getArguments()[0];
                finishedListener.onInventoryLoadFinished(false);
                return null;
            }
        }).when(billingService).loadInventory(any(BillingService.OnLoadInventoryFinishedListener.class), eq(applicationContext));

        // when
        licenseIntentService.isPremium(listener);

        // then
        verify(billingService, times(0)).contains(anyString());
    }
}