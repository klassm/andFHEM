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

package li.klass.fhem.sendCommand.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.databinding.CommandExecutionBinding
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.service.intent.SendCommandService
import li.klass.fhem.util.DialogUtil
import li.klass.fhem.util.ListViewUtil
import org.apache.commons.lang3.StringUtils.isEmpty
import javax.inject.Inject

class SendCommandFragment @Inject constructor(
        private val sendCommandService: SendCommandService
) : BaseFragment() {

    val args: SendCommandFragmentArgs by navArgs()
    lateinit var binding: CommandExecutionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            binding = CommandExecutionBinding.bind(view)
            return view
        }

        binding = CommandExecutionBinding.inflate(inflater, container, false)
        val context = activity ?: return null
        binding.send.setOnClickListener {
            sendCommandIntent(binding.input.text.toString())
        }

        val recentCommandsAdapter =
            ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
        binding.commandHistory.adapter = recentCommandsAdapter
        binding.commandHistory.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val command = recentCommandsAdapter.getItem(position)
                sendCommandIntent(command)
            }
        binding.commandHistory.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val command = recentCommandsAdapter.getItem(position)
                showContextMenuFor(command)
                true
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCommandIntent(args.cmd)
    }

    override fun mayPullToRefresh(): Boolean = false

    private fun sendCommandIntent(command: String?) {
        command ?: return
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) {
                sendCommandService.executeCommand(command, connectionId = null)
            }
            if (!isEmpty(result?.replace("[\\r\\n]".toRegex(), ""))) {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.command_execution_result)
                        .setMessage(result)
                        .setPositiveButton(R.string.okButton) { dialogInterface, _ ->
                            dialogInterface.cancel()
                            updateAsync(false)
                        }.show()
            }
        }
    }

    override suspend fun update(refresh: Boolean) {
        val myActivity = activity ?: return
        myActivity.sendBroadcast(Intent(Actions.SHOW_EXECUTING_DIALOG).apply { setPackage(myActivity.packageName) })
        coroutineScope {
            val recentCommands = withContext(Dispatchers.IO) {
                sendCommandService.getRecentCommands()
            }
            Log.i("SCF", "got commands: " + recentCommands)

            @Suppress("UNCHECKED_CAST")
            val adapter: ArrayAdapter<String> =
                binding.commandHistory.adapter as ArrayAdapter<String>
            adapter.clear()

            adapter.addAll(recentCommands)
            adapter.notifyDataSetChanged()

            ListViewUtil.setHeightBasedOnChildren(binding.commandHistory)

            myActivity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG).apply { setPackage(myActivity.packageName) })
        }
    }


    private fun showContextMenuFor(command: String?) {
        command ?: return
        (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.sendcommand_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val safeContext = context ?: return false
                when (item.itemId) {
                    R.id.menu_delete -> GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            sendCommandService.deleteCommand(command)
                        }
                        update(false)
                    }
                    R.id.menu_edit -> DialogUtil.showInputBox(safeContext, getString(R.string.context_edit), command, object : DialogUtil.InputDialogListener {
                        override fun onClick(text: String) {
                            GlobalScope.launch(Dispatchers.Main) {
                                withContext(Dispatchers.IO) {
                                    sendCommandService.editCommand(command, text)
                                }
                                update(false)
                            }
                        }
                    })
                }
                mode.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateAsync(false)
    }

    override fun getTitle(context: Context) = context.getString(R.string.send_command)
}
