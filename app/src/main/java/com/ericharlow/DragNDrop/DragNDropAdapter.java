/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ericharlow.DragNDrop;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;

public abstract class DragNDropAdapter<T extends Comparable<T>> extends ListDataAdapter<T> implements RemoveListener, DropListener, DragListener {

    public DragNDropAdapter(Context context, ArrayList<T> data) {
        super(context, data);
    }

    public void onRemove(int which) {
        if (which < 0 || which > data.size()) return;
        data.remove(which);
    }

    public void onDrop(int from, int to) {
        Log.e(DragNDropAdapter.class.getName(), "drop from " + from + " to " + to);
        T temp = data.get(from);
        data.remove(from);
        data.add(to,temp);

        updateData(data);
    }

    @Override
    public void onStartDrag(View itemView) {
        itemView.setBackgroundColor(context.getResources().getColor(R.color.focusedColor));
    }

    @Override
    public void onDrag(int x, int y, ListView listView) {
    }

    @Override
    public void onStopDrag(View itemView) {
        if (itemView == null) return;
        itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
    }
}