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

package li.klass.fhem.adapter.devices.genericui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.GroupSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.MultipleSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.NoArgSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SliderSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SpecialButtonHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.TextFieldTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.TimeTargetStateHandler;
import li.klass.fhem.adapter.uiservice.StateUiService;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListEntry;
import li.klass.fhem.domain.setlist.typeEntry.BaseGroupSetListEntry;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AvailableTargetStatesDialogUtil {

    private static final List<SetListTargetStateHandler<FhemDevice<?>>> HANDLERS = ImmutableList.of(
            new GroupSetListTargetStateHandler<>(),
            new SliderSetListTargetStateHandler<>(),
            new TimeTargetStateHandler<>(),
            new TextFieldTargetStateHandler<>(),
            new MultipleSetListTargetStateHandler<>(),
            new SpecialButtonHandler<>(),
            new NoArgSetListTargetStateHandler<>() // must be last entry!
    );

    public static <D extends FhemDevice<D>> void showSwitchOptionsMenu(final Context context, final D device) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final List<String> setOptions = device.getSetList().getSortedKeys();
        final String[] eventMapOptions = device.getAvailableTargetStatesEventMapTexts();

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int position) {
                final String option = setOptions.get(position);

                handleSelectedOption(context, device, device.getSetList().get(option));

                dialog.dismiss();
            }
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.list_item_with_arrow, eventMapOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.list_item_with_arrow, null);
                }
                assert convertView != null;
                TextView textView = (TextView) convertView.findViewById(R.id.text);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

                textView.setText(getItem(position));

                String setOption = setOptions.get(position);
                SetList setList = device.getSetList();
                final SetListEntry setListEntry = setList.get(setOption);

                imageView.setVisibility(setListEntry instanceof BaseGroupSetListEntry ? VISIBLE : GONE);

                return convertView;
            }
        };
        contextMenu.setAdapter(adapter, clickListener);

        contextMenu.show();
    }

    public static <D extends FhemDevice<D>> boolean handleSelectedOption(Context context, D device, SetListEntry option) {
        for (SetListTargetStateHandler<FhemDevice<?>> handler : HANDLERS) {
            if (handler.canHandle(option)) {
                handler.handle(option, context, device, new StateUiService());
                return true;
            }
        }
        return false;
    }
}
