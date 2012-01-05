package li.klass.fhem.adapter.fhtControl;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import li.klass.fhem.R;
import li.klass.fhem.domain.fht.FHTDayControl;
import li.klass.fhem.util.DayUtil;
import li.klass.fhem.widget.NestedListViewAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHTControlListAdapter extends NestedListViewAdapter<Integer, FHTDayControl> {
    private Map<Integer, FHTDayControl> controlMap = new HashMap<Integer, FHTDayControl>();
    private Resources resources;

    public FHTControlListAdapter(Context context) {
        super(context);
        resources = context.getResources();
    }

    @Override
    protected FHTDayControl getChildForParentAndChildPosition(Integer parent, int childPosition) {
        return controlMap.get(parent);
    }

    @Override
    protected int getChildrenCountForParent(Integer parent) {
        if (controlMap.get(parent) != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    protected View getParentView(Integer parent, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.control_fht_list_parent, null);

        TextView parentTextView = (TextView) view.findViewById(R.id.parent);
        parentTextView.setText(resources.getText(parent));

        return view;
    }

    @Override
    protected View getChildView(FHTDayControl child, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.control_fht_list_item, null);

        setDetailTextView(view, R.id.from1, R.id.tableRowFrom1, child.getFrom1(), child.getFrom1Changed());
        setDetailTextView(view, R.id.from2, R.id.tableRowFrom2, child.getFrom2(), child.getFrom2Changed());
        setDetailTextView(view, R.id.to1, R.id.tableRowTo1, child.getTo1(), child.getTo1Changed());
        setDetailTextView(view, R.id.to2, R.id.tableRowTo2, child.getTo2(), child.getTo2Changed());

        return view;
    }

    private void setDetailTextView(View view, int layoutItemId, int tableRowId, String text, String changedText) {
        TextView layoutItem = (TextView) view.findViewById(layoutItemId);
        layoutItem.setText(text);
        if (changedText != null) {
            layoutItem.setTextColor(android.R.color.darker_gray);
        }

        if (text == null || text.equals("")) {
            view.findViewById(tableRowId).setVisibility(View.GONE);
        }
    }

    @Override
    protected List<Integer> getParents() {
        return DayUtil.getSortedDayStringIdList();
    }

    public void updateData(Map<Integer, FHTDayControl> fhtDayControlMap) {
        this.controlMap = fhtDayControlMap;
        super.updateData();
    }
}
