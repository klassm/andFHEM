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

package li.klass.fhem.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import li.klass.fhem.R;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fragments.core.BaseFragment;
import li.klass.fhem.service.intent.SendCommandIntentService;
import li.klass.fhem.util.ListViewUtil;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class SendCommandFragment extends BaseFragment {

    private transient ArrayAdapter<String> recentCommandsAdapter;
    private ArrayList<String> recentCommands;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.command_execution, container, false);
        Button sendButton = (Button) view.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                EditText editText = (EditText) view.findViewById(R.id.input);
                String command = editText.getText().toString();

                sendCommandIntent(command);
            }
        });

        ListView recentCommandsList = (ListView) view.findViewById(R.id.command_history);
        recentCommandsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        recentCommandsList.setAdapter(recentCommandsAdapter);
        recentCommandsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String command = recentCommands.get(position);
                sendCommandIntent(command);
            }
        });

        return view;
    }

    private void sendCommandIntent(String command) {
        final Context context = getActivity();
        Intent intent = new Intent(Actions.EXECUTE_COMMAND);
        intent.setClass(getActivity(), SendCommandIntentService.class);
        intent.putExtra(BundleExtraKeys.COMMAND, command);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultData != null && resultCode == ResultCodes.SUCCESS && resultData.containsKey(BundleExtraKeys.COMMAND_RESULT)) {
                    String result = resultData.getString(BundleExtraKeys.COMMAND_RESULT);
                    if (result == null || result.equals("")) {
                        update(false);
                        return;
                    }

                    if (isEmpty(result.replaceAll("[\\r\\n]", ""))) return;
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.command_execution_result)
                            .setMessage(result)
                            .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    update(false);
                                }
                            }).show();
                }
            }
        });
        getActivity().startService(intent);
    }

    @Override
    public void update(boolean doUpdate) {
        Intent intent = new Intent(Actions.RECENT_COMMAND_LIST);
        intent.setClass(getActivity(), SendCommandIntentService.class);
        intent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode != ResultCodes.SUCCESS || resultData == null || !resultData.containsKey(BundleExtraKeys.RECENT_COMMANDS)) {
                    return;
                }

                View view = getView();
                if (view == null) return;

                recentCommands = resultData.getStringArrayList(BundleExtraKeys.RECENT_COMMANDS);
                recentCommandsAdapter.clear();

                // careful: addAll method is only available since API level 11 (Android 3.0)
                for (String recentCommand : recentCommands) {
                    recentCommandsAdapter.add(recentCommand);
                }
                recentCommandsAdapter.notifyDataSetChanged();

                ListViewUtil.setHeightBasedOnChildren((ListView) view.findViewById(R.id.command_history));

                getActivity().sendBroadcast(new Intent(Actions.DISMISS_EXECUTING_DIALOG));
            }
        });
        getActivity().startService(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update(false);
    }
}
