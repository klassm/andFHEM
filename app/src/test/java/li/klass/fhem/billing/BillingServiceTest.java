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

package li.klass.fhem.billing;

import android.content.Context;

import com.android.vending.billing.IabException;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import li.klass.fhem.infra.basetest.RobolectricBaseTestCase;
import li.klass.fhem.testutil.MockitoTestRule;

import static li.klass.fhem.billing.BillingService.OnLoadInventoryFinishedListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BillingServiceTest extends RobolectricBaseTestCase {

    @Rule
    public MockitoTestRule mockitoTestRule = new MockitoTestRule();

    @InjectMocks
    @Spy
    private BillingService billingService;

    @Mock
    private IabHelper iabHelper;

    @Mock
    private Context applicationContext;

    @Before
    public void setUp() {
        given(applicationContext.getApplicationContext()).willReturn(applicationContext);
        doReturn(iabHelper).when(billingService).createIabHelper();
    }

    @Test
    public void should_dispose_IabHelper_onStop() {
        // when
        billingService.stop();

        // then
        verify(iabHelper).dispose();
        assertThat(billingService.getIabHelper()).isNull();
    }

    @Test
    public void should_dispose_null_IABHelper() {
        // given
        billingService.setIabHelper(null);

        // when
        billingService.stop();

        // then
        // verify no exception
    }

    @Test
    public void should_call_setup_before_loading_the_inventory() {
        // given
        given(iabHelper.isSetupDone()).willReturn(false);

        // when
        billingService.loadInventory(mock(OnLoadInventoryFinishedListener.class));

        // then
        verify(iabHelper).startSetup(any(IabHelper.OnIabSetupFinishedListener.class));
    }

    @Test
    public void should_call_Listener_if_Exception_occurs_during_setup() {
        // given
        BillingService.SetupFinishedListener listener = mock(BillingService.SetupFinishedListener.class);
        doThrow(new IllegalStateException("some IAB Exception")).when(iabHelper).startSetup(any(IabHelper.OnIabSetupFinishedListener.class));

        // when
        billingService.setup(listener);

        // then
        verify(iabHelper).startSetup(any(IabHelper.OnIabSetupFinishedListener.class));
        verify(listener).onSetupFinished(false);
    }

    @Test
    public void should_call_Listener_if_Setup_was_successful() {
        // given
        BillingService.SetupFinishedListener listener = mock(BillingService.SetupFinishedListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                IabHelper.OnIabSetupFinishedListener listener = (IabHelper.OnIabSetupFinishedListener) invocationOnMock.getArguments()[0];
                listener.onIabSetupFinished(new IabResult(IabHelper.BILLING_RESPONSE_RESULT_OK, "ok"));
                return null;
            }
        }).when(iabHelper).startSetup(any(IabHelper.OnIabSetupFinishedListener.class));

        // when
        billingService.setup(listener);

        // then
        verify(iabHelper).startSetup(any(IabHelper.OnIabSetupFinishedListener.class));
        verify(listener).onSetupFinished(true);
    }

    @Test
    public void should_handle_Exception_during_load_Inventory() throws IabException {
        // given
        given(iabHelper.isSetupDone()).willReturn(true);
        doThrow(new IllegalStateException("IAB Helper is not setup"))
                .when(iabHelper).queryInventory(false, null);
        OnLoadInventoryFinishedListener listener = mock(OnLoadInventoryFinishedListener.class);

        // when
        billingService.loadInventory(listener);

        // then
        verify(iabHelper).queryInventory(false, null);
        verify(listener).onInventoryLoadFinished(false);
    }

    @Test
    public void should_handle_successful_query_Inventory() throws IabException {
        // given
        given(iabHelper.isSetupDone()).willReturn(true);
        OnLoadInventoryFinishedListener listener = mock(OnLoadInventoryFinishedListener.class);
        IabResult result = mock(IabResult.class);
        given(result.isSuccess()).willReturn(true);
        Inventory inventory = mock(Inventory.class);

        given(iabHelper.queryInventory(false, null)).willReturn(inventory);

        // when
        billingService.loadInventory(listener);

        // then
        verify(iabHelper).queryInventory(false, null);
        verify(listener).onInventoryLoadFinished(true);
        assertThat(billingService.getInventory()).isEqualTo(inventory);
    }
}