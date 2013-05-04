///*
// * AndFHEM - Open Source Android application to control a FHEM home automation
// * server.
// *
// * Copyright (c) 2011, Matthias Klass or third-party contributors as
// * indicated by the @author tags or express copyright attribution
// * statements applied by the authors.  All third-party contributions are
// * distributed under license by Red Hat Inc.
// *
// * This copyrighted material is made available to anyone wishing to use, modify,
// * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
// * for more details.
// *
// * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
// * along with this distribution; if not, write to:
// *   Free Software Foundation, Inc.
// *   51 Franklin Street, Fifth Floor
// *   Boston, MA  02110-1301  USA
// */
//
//package li.klass.fhem.activities.core;
//
//import android.content.Intent;
//import li.klass.fhem.activities.AndFHEMMainActivity;
//import li.klass.fhem.constants.Actions;
//import li.klass.fhem.constants.BundleExtraKeys;
//import li.klass.fhem.constants.PreferenceKeys;
//import li.klass.fhem.infra.AndFHEMRobolectricTestRunner;
//import li.klass.fhem.util.ApplicationProperties;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.*;
//
//@RunWith(AndFHEMRobolectricTestRunner.class)
//public class FragmentBaseActivityTest {
//    private FragmentBaseActivity activity;
//
//    @Before
//    public void setUp() {
//        activity = Mockito.spy(new AndFHEMMainActivity());
//        activity.applicationProperties = mock(ApplicationProperties.class);
//    }
//
//    @Test
//    public void testIsActivityStartIsSetToTrue() {
//        FragmentBaseActivity activity = new AndFHEMMainActivity();
//        assertThat(activity.isActivityStart, is(true));
//    }
//
//    @Test
//    public void testUpdateOnApplicationStartWithActivityStartAndDoUpdateProperty() {
//        setDoUpdateOnApplicationStart(true);
//        activity.onWindowFocusChanged(true);
//        activity.isActivityStart = true;
//
//        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
//        verify(activity).sendBroadcast(argumentCaptor.capture());
//
//        Intent intent = argumentCaptor.getValue();
//        assertThat(intent.getAction(), is(Actions.DO_UPDATE));
//        assertThat(intent.getBooleanExtra(BundleExtraKeys.DO_REFRESH, false), is(true));
//    }
//
//    @Test
//    public void testUpdateOnApplicationStartWithoutRequiredProperties() {
//        setDoUpdateOnApplicationStart(true);
//        activity.onWindowFocusChanged(false);
//
//        setDoUpdateOnApplicationStart(true);
//        activity.isActivityStart = false;
//        activity.onWindowFocusChanged(true);
//
//        setDoUpdateOnApplicationStart(false);
//        activity.onWindowFocusChanged(true);
//
//        verify(activity, never()).sendBroadcast(any(Intent.class));
//    }
//
//    private void setDoUpdateOnApplicationStart(boolean doUpdate) {
//        when(activity.applicationProperties
//                .getBooleanSharedPreference(PreferenceKeys.UPDATE_ON_APPLICATION_START, false)).thenReturn(doUpdate);
//    }
//}
