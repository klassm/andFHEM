package li.klass.fhem.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.R;

import java.util.Comparator;
import java.util.List;

public class RoomListAdapter extends ListDataAdapter<String> {
    public RoomListAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
    }

    public RoomListAdapter(Context context, int resource, List<String> data, Comparator<String> stringComparator) {
        super(context, resource, data, stringComparator);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String item = (String) getItem(position);
        Log.e(RoomListAdapter.class.getName(), position + " : " + item);

        convertView = inflater.inflate(resource, null);

        Holder roomHolder = new Holder();
        roomHolder.roomName = (TextView) convertView.findViewById(R.id.roomName);
        roomHolder.roomName.setText(item);

        return convertView;
    }

    private static class Holder {
        TextView roomName;
    }
}
