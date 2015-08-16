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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

import javax.inject.Inject;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.core.ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow;
import li.klass.fhem.adapter.devices.core.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.genericui.HolderActionRow;
import li.klass.fhem.adapter.devices.strategy.LightSceneDeviceViewStrategy;
import li.klass.fhem.adapter.devices.strategy.ViewStrategy;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.dagger.ApplicationComponent;
import li.klass.fhem.domain.LightSceneDevice;
import li.klass.fhem.domain.core.FhemDevice;

public class LightSceneAdapter extends ExplicitOverviewDetailDeviceAdapterWithSwitchActionRow {
    @Inject
    StateUiService stateUiService;

    @Inject
    LightSceneDeviceViewStrategy lightSceneDeviceOverviewStrategy;

    public LightSceneAdapter() {
        super();
    }

    @Override
    public Class<? extends FhemDevice> getSupportedDeviceClass() {
        return LightSceneDevice.class;
    }

    @Override
    protected void inject(ApplicationComponent daggerComponent) {
        daggerComponent.inject(this);
    }

    @Override
    protected void afterPropertiesSet() {
        super.afterPropertiesSet();
        registerFieldListener("state", new FieldNameAddedToDetailListener() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, FhemDevice device, TableRow fieldTableRow) {
                tableLayout.addView(new HolderActionRow<String>(context.getString(R.string.scene), HolderActionRow.LAYOUT_DETAIL) {

                    @Override
                    public List<String> getItems(FhemDevice device) {
                        return ((LightSceneDevice) device).getScenes();
                    }

                    @Override
                    public View viewFor(String scene, FhemDevice device, LayoutInflater inflater, Context context, ViewGroup viewGroup) {
                        Button button = (Button) inflater.inflate(R.layout.lightscene_button, viewGroup, false);
                        setSceneButtonProperties(device, scene, button);
                        return button;
                    }
                }.createRow(context, getInflater(), tableLayout, device));
            }
        });
    }

    private void setSceneButtonProperties(final FhemDevice device, final String scene, Button button) {
        button.setText(scene);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateScene(device, scene);
            }
        });
    }

    private void activateScene(FhemDevice device, String scene) {
        stateUiService.setSubState(device, "scene", scene, getContext());
    }

    @Override
    protected void fillOverviewStrategies(List<ViewStrategy> overviewStrategies) {
        super.fillOverviewStrategies(overviewStrategies);
        overviewStrategies.add(lightSceneDeviceOverviewStrategy);
    }
}
