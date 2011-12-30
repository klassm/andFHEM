package li.klass.fhem.adapter.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.activities.deviceDetail.FS20DeviceDetailActivity;
import li.klass.fhem.data.FHEMService;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;

import java.util.List;

public class FS20Adapter extends DeviceAdapter<FS20Device> {

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        public int progress;

        private SeekBarChangeListener(int progress) {
            this.progress = progress;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            this.progress = progress;

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    FS20Device device = FHEMService.INSTANCE.deviceListForAllRooms(false).getDeviceFor((String) seekBar.getTag());
                    device.dim(AndFHEMApplication.getContext(), progress);
                    return null;
                }
            }.execute(null);
        }
    }

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FS20Device device) {
        if (device.isDimDevice()) {
            return getFS20SeekView(layoutInflater, device);
        } else {
            return getFS20ToggleView(layoutInflater, device);
        }
    }

    @Override
    public int getDetailViewLayout() {
        return R.layout.device_detail_fs20;
    }

    @Override
    public boolean supportsDetailView() {
        return true;
    }

    @Override
    protected View getDeviceDetailView(final Context context, LayoutInflater layoutInflater, final FS20Device device) {
        View view = layoutInflater.inflate(getDetailViewLayout(), null);

        TableRow seekBarRow = (TableRow) view.findViewById(R.id.switchSeekBarRow);
        TableRow toggleButtonRow = (TableRow) view.findViewById(R.id.switchToggleButtonRow);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        
        if (device.isDimDevice()) {
            int initialProgress = device.getFS20DimState();
            seekBar.setProgress(initialProgress);
            seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(device.getFS20DimState()));
            seekBar.setTag(device.getName());

            toggleButtonRow.setVisibility(View.GONE);
        } else {
            switchButton.setChecked(device.isOn());
            switchButton.setTag(device.getName());

            seekBarRow.setVisibility(View.GONE);
        }

        TextView measured = (TextView) view.findViewById(R.id.measured);
        measured.setText(device.getMeasureDate());

        TextView state = (TextView) view.findViewById(R.id.state);
        state.setText(device.getState());

        Button switchSetOptions = (Button) view.findViewById(R.id.switchSetOptions);
        switchSetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder contextMenu = new AlertDialog.Builder(context);
                contextMenu.setTitle(context.getResources().getString(R.string.switchDevice));
                final List<String> setOptions = device.getSetOptions();

                contextMenu.setItems(setOptions.toArray(new CharSequence[setOptions.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String option = setOptions.get(item);
                        device.setState(AndFHEMApplication.getContext(), option);
                        dialog.dismiss();
                    }
                });
                contextMenu.show();

            }
        });

        return view;
    }

    @Override
    protected Intent onFillDeviceDetailIntent(Context context, Device device, Intent intent) {
        intent.setClass(context, FS20DeviceDetailActivity.class);
        return intent;
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FS20Device.class;
    }

    private View getFS20ToggleView(LayoutInflater layoutInflater, FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(child.getAliasOrName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(child.isOn());
        switchButton.setTag(child.getName());

        return view;
    }

    private View getFS20SeekView(LayoutInflater layoutInflater, final FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20_seek, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(child.getName());

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final int initialProgress = child.getFS20DimState();
        seekBar.setProgress(initialProgress);
        seekBar.setTag(child.getName());

        seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(child.getFS20DimState()));

        return view;
    }
}
