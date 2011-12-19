package li.klass.fhem.adapter.devices;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import li.klass.fhem.R;
import li.klass.fhem.domain.Device;
import li.klass.fhem.domain.FS20Device;

public class FS20Adapter extends DeviceListOnlyAdapter<FS20Device> {

    @Override
    public View getDeviceView(LayoutInflater layoutInflater, FS20Device device) {
        if (device.isDimDevice()) {
            return getFS20SeekView(layoutInflater, device);
        } else {
            return getFS20ToggleView(layoutInflater, device);
        }
    }

    @Override
    public Class<? extends Device> getSupportedDeviceClass() {
        return FS20Device.class;
    }

    private View getFS20ToggleView(LayoutInflater layoutInflater, FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(child.getName());

        ToggleButton switchButton = (ToggleButton) view.findViewById(R.id.switchButton);
        switchButton.setChecked(child.isOn());
        switchButton.setTag(child);

        return view;
    }

    private View getFS20SeekView(LayoutInflater layoutInflater, final FS20Device child) {
        View view = layoutInflater.inflate(R.layout.room_detail_fs20_seek, null);

        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        deviceName.setText(child.getName());

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        final int initialProgress = child.getFS20DimState();
        seekBar.setProgress(initialProgress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress = initialProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                this.progress = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        child.dim(progress);
                        return null;
                    }
                }.execute(null);
            }
        });

        return view;
    }
}
