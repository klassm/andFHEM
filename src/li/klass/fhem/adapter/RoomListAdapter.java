package li.klass.fhem.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.R;

import java.util.ArrayList;
import java.util.List;

public class RoomListAdapter extends ListDataAdapter<String> {
    public RoomListAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String roomName = (String) getItem(position);

        convertView = inflater.inflate(resource, null);

        TextView roomNameTextView = (TextView) convertView.findViewById(R.id.roomName);
        roomNameTextView.setText(roomName);

        convertView.setTag(roomName);

        return convertView;
    }

    @Override
    public void updateData(List<String> newData) {

        boolean showHiddenDevices = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("prefShowHiddenDevices", false);
        if (! showHiddenDevices) {
            for (String roomName : new ArrayList<String>(newData)) {
                if (roomName.equalsIgnoreCase("hidden")) {
                    newData.remove(roomName);
                }
            }
        }

        super.updateData(newData);
    }
}
