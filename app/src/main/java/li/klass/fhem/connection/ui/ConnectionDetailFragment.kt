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

package li.klass.fhem.connection.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.Lists.newArrayList
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.connection.backend.ServerType
import li.klass.fhem.constants.Actions
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.PermissionUtil
import org.slf4j.LoggerFactory
import java.io.File
import javax.inject.Inject

class ConnectionDetailFragment @Inject constructor(
        private val connectionService: ConnectionService
) : BaseFragment() {
    private var connectionType: ServerType? = null
    private var detailChangedListener: ConnectionTypeDetailChangedListener? = null

    private val args: ConnectionDetailFragmentArgs by navArgs()

    private val isModify
        get() = args.connectionId != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            return view
        }
        val context: Context = activity ?: return null

        view = inflater.inflate(R.layout.connection_detail, container, false)!!

        val connectionTypeSpinner = view.findViewById<Spinner>(R.id.connectionType)
        connectionTypeSpinner.isEnabled = !isModify

        val connectionTypes = serverTypes

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_dropdown_item, connectionTypes)
        connectionTypeSpinner.adapter = adapter

        connectionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, id: Long) {
                handleConnectionTypeChange(connectionTypes[position])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.connection_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            GlobalScope.launch(Dispatchers.Main) {
                handleSave()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun mayPullToRefresh(): Boolean = false

    private val serverTypes: List<ServerType>
        get() {
            val connectionTypes = newArrayList<ServerType>()
            connectionTypes.addAll(ServerType.values().toList())
            connectionTypes.remove(ServerType.DUMMY)
            return connectionTypes
        }

    @SuppressLint("InflateParams")
    private fun handleConnectionTypeChange(connectionType: ServerType) {
        this.connectionType = checkNotNull(connectionType)
        val activity = activity ?: return

        val view = when (connectionType) {
            ServerType.FHEMWEB -> {
                activity.layoutInflater.inflate(R.layout.connection_fhemweb, null).apply {
                    handleFHEMWEBView(this)
                }
            }
            ServerType.TELNET -> activity.layoutInflater.inflate(R.layout.connection_telnet, null)
            else -> throw IllegalArgumentException("cannot handle connection type $connectionType")
        }

        assert(view != null)

        val showPasswordCheckbox = view.findViewById<CheckBox?>(R.id.showPasswordCheckbox)
        val passwordView = view.findViewById<EditText?>(R.id.password)
        if (showPasswordCheckbox != null && passwordView != null) {
            showPasswordCheckbox.setOnClickListener { myView ->
                val radio = myView as CheckBox
                val checked = radio.isChecked
                if (checked) {
                    passwordView.transformationMethod = null
                } else {
                    passwordView.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }

            if (isModify) showPasswordCheckbox.isEnabled = false
        }

        val showCertificatePasswordCheckbox = view.findViewById<CheckBox?>(R.id.showCertificatePasswordCheckbox)
        val passwordClientCertificateView = view.findViewById<EditText?>(R.id.clientCertificatePassword)
        if (showCertificatePasswordCheckbox != null && passwordClientCertificateView != null) {
            showCertificatePasswordCheckbox.setOnClickListener { myView ->
                val radio = myView as CheckBox
                val checked = radio.isChecked
                if (checked) {
                    passwordClientCertificateView.transformationMethod = null
                } else {
                    passwordClientCertificateView.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }

            if (isModify) showCertificatePasswordCheckbox.isEnabled = false
        }

        val connectionPreferences = getView()?.findViewById<ViewGroup>(R.id.connectionPreferences)
        connectionPreferences?.removeAllViews()
        connectionPreferences?.addView(view)

        if (detailChangedListener != null) detailChangedListener!!.onChanged()
    }

    private suspend fun handleSave() {
        val view = view ?: return

        val myContext = context ?: return
        val saveStrategy = strategyFor(connectionType ?: ServerType.FHEMWEB, myContext)
        val saveData = saveStrategy.saveDataFor(view) ?: return

        coroutineScope {
            withContext(Dispatchers.IO) {
                if (isModify) {
                    connectionService.update(args.connectionId!!, saveData)
                } else {
                    connectionService.create(saveData)
                }
            }

            findNavController().popBackStack()
        }
    }

    private fun strategyFor(connectionType: ServerType?, context: Context): ConnectionStrategy {
        return when (val type = connectionType ?: ServerType.FHEMWEB) {
            ServerType.FHEMWEB -> FhemWebStrategy(context)
            ServerType.TELNET -> TelnetStrategy(context)
            else -> throw IllegalArgumentException("don't know what to do with $type")
        }
    }

    private fun handleFHEMWEBView(view: View) {
        val setClientCertificate = view.findViewById<ImageButton>(R.id.setClientCertificatePath)
        setClientCertificate.setOnClickListener(View.OnClickListener { innerView ->
            if (innerView == null) return@OnClickListener

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                PermissionUtil.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            val clientCertificatePath = innerView.findViewById<TextView>(R.id.clientCertificatePath)
            val initialPath = File(clientCertificatePath.text.toString())

            val properties = DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root = initialPath

            val dialog = FilePickerDialog(activity, properties)
            dialog.setTitle(R.string.selectFile)
            dialog.setDialogSelectionListener { files ->
                val selection = files.joinToString(separator = ", ")
                LOG.info("handleFHEMWEBView - selected '$selection' as client certificate")
                if (files.isNotEmpty()) {
                    clientCertificatePath.text = files[0]
                }
            }
            dialog.show()
        })
    }

    override fun getTitle(context: Context) = context.getString(R.string.connectionManageTitle)

    override suspend fun update(refresh: Boolean) {
        if (!isModify) {
            LOG.info("I can only update if a connection is being modified!")
            activity?.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            return
        }

        val myContext = context ?: return
        coroutineScope {
            val result = withContext(Dispatchers.Default) {
                connectionService.forId(args.connectionId!!)
            }
            if (result == null) {
                LOG.error("update - cannot find server with ID ${args.connectionId}")
                myContext.sendBroadcast(Intent(Actions.BACK))
            } else {
                setValuesForCurrentConnection(result)
            }
        }
    }

    private fun setValuesForCurrentConnection(connection: FHEMServerSpec) {
        val view = view ?: return

        // We do not need to change the type selector here, as the right one is already selected.
        // We just overwrite values within the edit fields.
        if (connection.serverType == connectionType) {
            fillDetail(connection)
        } else {
            // We have to change the detail view to the one which is right for the current
            // connection type. However, we do not know when the selection changed listener
            // of the combo box fires. This is why we register a global listener, which is called
            // when the new view has been attached to the root view.
            // Afterwards we can continue with filling the fields with the respective values
            // of the current connection!
            detailChangedListener = object : ConnectionTypeDetailChangedListener {
                override fun onChanged() {
                    detailChangedListener = null

                    fillDetail(connection)
                }
            }
            val connectionTypeSpinner = view.findViewById<Spinner>(R.id.connectionType)
            connectionTypeSpinner.setSelection(selectionIndexFor(connection.serverType), true)
        }
    }

    private fun fillDetail(fhemServerSpec: FHEMServerSpec) {
        val v = view ?: return
        val myContext = context ?: return
        strategyFor(connectionType, myContext).fillView(v, fhemServerSpec)
    }

    private fun selectionIndexFor(serverType: ServerType): Int {
        val serverTypes = serverTypes
        return serverTypes.indices.firstOrNull { serverType == serverTypes[it] } ?: -1
    }

    private interface ConnectionTypeDetailChangedListener {
        fun onChanged()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ConnectionDetailFragment::class.java)
    }
}
