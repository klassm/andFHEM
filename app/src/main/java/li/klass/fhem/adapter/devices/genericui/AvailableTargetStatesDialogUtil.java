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
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.GroupSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.MultipleSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.NoArgSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.OnTargetStateSelectedCallback;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.RGBTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SliderSetListTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SpecialButtonHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.SpecialButtonSecondsHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.TextFieldTargetStateHandler;
import li.klass.fhem.adapter.devices.genericui.availableTargetStates.TimeTargetStateHandler;
import li.klass.fhem.domain.core.FhemDevice;
import li.klass.fhem.domain.setlist.SetList;
import li.klass.fhem.domain.setlist.SetListEntry;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.collect.FluentIterable.from;

public class AvailableTargetStatesDialogUtil {

    private static final List<SetListTargetStateHandler<FhemDevice>> HANDLERS_WITHOUT_NO_ARG = ImmutableList.of(
            new RGBTargetStateHandler<>(),
            new GroupSetListTargetStateHandler<>(),
            new SliderSetListTargetStateHandler(),
            new TimeTargetStateHandler<>(),
            new TextFieldTargetStateHandler<>(),
            new MultipleSetListTargetStateHandler<>(),
            new SpecialButtonSecondsHandler<>(),
            new SpecialButtonHandler<>()
    );
    private static final List<SetListTargetStateHandler<FhemDevice>> HANDLERS = ImmutableList.<SetListTargetStateHandler<FhemDevice>>builder()
            .addAll(HANDLERS_WITHOUT_NO_ARG)
            .add(new NoArgSetListTargetStateHandler<>()) // must be last entry!
            .build();

    public static <D extends FhemDevice> void showSwitchOptionsMenu(final Context context, final D device, final OnTargetStateSelectedCallback callback) {
        AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
        contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
        final SetList setList = device.getSetList();
        final List<String> setOptions = setList.getSortedKeys();
        final String[] eventMapOptions = device.getAvailableTargetStatesEventMapTexts();

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int position) {
                final String option = setOptions.get(position);

                handleSelectedOption(context, device, setList.get(option, true), callback);

                dialog.dismiss();
            }
        };
        ArrayAdapter<String> adapter = new SetListArrayAdapter<>(context, eventMapOptions, setOptions, device);
        contextMenu.setAdapter(adapter, clickListener);

        contextMenu.show();
    }

    public static <D extends FhemDevice> void showSwitchOptionsMenuFor(final Context context, final D device, final OnTargetStateSelectedCallback callback) {
        SetListEntry entry = device.getSetList().get("state", true);
        handleSelectedOption(context, device, entry, callback);
    }

    static <D extends FhemDevice> boolean handleSelectedOption(Context context, D device, SetListEntry option, OnTargetStateSelectedCallback callback) {
        for (SetListTargetStateHandler<FhemDevice> handler : HANDLERS) {
            if (handler.canHandle(option)) {
                handler.handle(option, context, device, callback);
                return true;
            }
        }
        return false;
    }

    private static class SetListArrayAdapter<D extends FhemDevice> extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> setOptions;
        private final D device;

        SetListArrayAdapter(Context context, String[] eventMapOptions, List<String> setOptions, D device) {
            super(context, R.layout.list_item_with_arrow, eventMapOptions);
            this.context = context;
            this.setOptions = setOptions;
            this.device = device;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item_with_arrow, null);
            }
            assert convertView != null;
            TextView textView = convertView.findViewById(R.id.text);
            ImageView imageView = convertView.findViewById(R.id.image);

            textView.setText(getItem(position));

            String setOption = setOptions.get(position);
            SetList setList = device.getSetList();
            final SetListEntry setListEntry = setList.get(setOption, true);

            imageView.setVisibility(requiresAdditionalInformation(setListEntry) ? VISIBLE : GONE);

            return convertView;
        }

        private boolean requiresAdditionalInformation(final SetListEntry entry) {
            return from(HANDLERS_WITHOUT_NO_ARG).anyMatch(canHandle(entry));
        }

        @NonNull
        private Predicate<SetListTargetStateHandler<FhemDevice>> canHandle(final SetListEntry entry) {
            return new Predicate<SetListTargetStateHandler<FhemDevice>>() {
                @Override
                public boolean apply(SetListTargetStateHandler<FhemDevice> input) {
                    return input.canHandle(entry);
                }
            };
        }
    }
}
