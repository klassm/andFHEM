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

package li.klass.fhem.fragments.connection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.*
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.Lists.newArrayList
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fhem.connection.FHEMServerSpec
import li.klass.fhem.fhem.connection.ServerType
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.service.connection.ConnectionService
import li.klass.fhem.util.PermissionUtil
import org.jetbrains.anko.coroutines.experimental.bg
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import javax.inject.Inject

class ConnectionDetailFragment : BaseFragment() {
    private var connectionId: String? = null
    private var isModify = false
    private var connectionType: ServerType? = null
    private var detailChangedListener: ConnectionTypeDetailChangedListener? = null

    @Inject
    lateinit var connectionService: ConnectionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setArguments(args: Bundle) {
        super.setArguments(args)
        if (args.containsKey(BundleExtraKeys.CONNECTION_ID)) {
            connectionId = args.getString(BundleExtraKeys.CONNECTION_ID)
            isModify = true
        }
    }

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = super.onCreateView(inflater, container, savedInstanceState)
        if (view != null) {
            return view
        }

        view = inflater!!.inflate(R.layout.connection_detail, container, false)

        val connectionTypeSpinner = view!!.findViewById(R.id.connectionType) as Spinner
        if (isModify) {
            connectionTypeSpinner.isEnabled = false
        }

        val connectionTypes = serverTypes

        val adapter = ArrayAdapter(activity,
                android.R.layout.simple_spinner_dropdown_item, connectionTypes)
        connectionTypeSpinner.adapter = adapter

        connectionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                handleConnectionTypeChange(connectionTypes[position])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.connection_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.save) {
            handleSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun mayPullToRefresh(): Boolean = false

    private val serverTypes: List<ServerType>
        get() {
            val connectionTypes = newArrayList<ServerType>()
            connectionTypes.addAll(Arrays.asList(*ServerType.values()))
            connectionTypes.remove(ServerType.DUMMY)
            return connectionTypes
        }

    @SuppressLint("InflateParams")
    private fun handleConnectionTypeChange(connectionType: ServerType) {
        if (view == null) return

        this.connectionType = checkNotNull(connectionType)
        val activity = checkNotNull(activity)
        val inflater = checkNotNull(activity.layoutInflater)

        val view: View?
        when (connectionType) {
            ServerType.FHEMWEB -> {
                view = inflater.inflate(R.layout.connection_fhemweb, null)
                handleFHEMWEBView(view)
            }
            ServerType.TELNET -> view = inflater.inflate(R.layout.connection_telnet, null)
            else -> throw IllegalArgumentException("cannot handle connection type " + connectionType)
        }

        assert(view != null)

        val showPasswordCheckbox = view!!.findViewById(R.id.showPasswordCheckbox) as CheckBox?
        val passwordView = view.findViewById(R.id.password) as EditText?
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

        val showCertificatePasswordCheckbox = view.findViewById(R.id.showCertificatePasswordCheckbox) as CheckBox?
        val passwordClientCertificateView = view.findViewById(R.id.clientCertificatePassword) as EditText?
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

        val connectionPreferences = checkNotNull<View>(getView()).findViewById(R.id.connectionPreferences) as ViewGroup
        checkNotNull(connectionPreferences)
        connectionPreferences.removeAllViews()
        connectionPreferences.addView(view)

        if (detailChangedListener != null) detailChangedListener!!.onChanged()
    }

    private fun handleSave() {
        view ?: return

        val saveStrategy = strategyFor(connectionType ?: ServerType.FHEMWEB)
        val saveData = saveStrategy.saveDataFor(view!!) ?: return

        val myContext = context
        async(UI) {
            bg {
                if (isModify) {
                    connectionService.update(connectionId!!, saveData, myContext)
                } else {
                    connectionService.create(saveData, myContext)
                }
            }.await()
            activity.sendBroadcast(Intent(Actions.BACK))
        }
    }

    private fun strategyFor(connectionType: ServerType?): ConnectionStrategy {
        val type = connectionType ?: ServerType.FHEMWEB
        return when (type) {
            ServerType.FHEMWEB -> FhemWebStrategy(context)
            ServerType.TELNET -> TelnetStrategy(context)
            else -> throw IllegalArgumentException("don't know what to do with " + type)
        }
    }

    private fun handleFHEMWEBView(view: View?) {
        val setClientCertificate = view!!.findViewById(R.id.setClientCertificatePath) as ImageButton
        setClientCertificate.setOnClickListener(View.OnClickListener {
            if (getView() == null) return@OnClickListener

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                PermissionUtil.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            val clientCertificatePath = getView()!!.findViewById(R.id.clientCertificatePath) as TextView
            val initialPath = File(clientCertificatePath.text.toString())

            val properties = DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            properties.root = initialPath

            val dialog = FilePickerDialog(activity, properties)
            dialog.setTitle(R.string.selectFile)
            dialog.setDialogSelectionListener { files ->
                if (files.isNotEmpty()) {
                    clientCertificatePath.text = files[0]
                }
            }
            dialog.show()
        })
    }

    override fun getTitle(context: Context): CharSequence? =
            context.getString(R.string.connectionManageTitle)

    override fun update(refresh: Boolean) {
        if (!isModify) {
            LOG.info("I can only update if a connection is being modified!")
            activity.sendBroadcast(Intent(Actions.DISMISS_EXECUTING_DIALOG))
            return
        }

        async(UI) {
            val result = bg {
                connectionService.forId(connectionId!!, getContext())
            }.await()
            if (result == null) {
                LOG.error("update - cannot find server with ID $connectionId")
                activity.sendBroadcast(Intent(Actions.BACK))
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
            val connectionTypeSpinner = view.findViewById(R.id.connectionType) as Spinner
            connectionTypeSpinner.setSelection(selectionIndexFor(connection.serverType), true)
        }
    }

    private fun fillDetail(fhemServerSpec: FHEMServerSpec) {
        view ?: return
        strategyFor(connectionType).fillView(view!!, fhemServerSpec)
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
