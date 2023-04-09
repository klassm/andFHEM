package li.klass.fhem.device.control

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import androidx.annotation.RequiresApi
import androidx.navigation.NavDeepLinkBuilder
import io.reactivex.Flowable
import io.reactivex.processors.ReplayProcessor
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentArgs
import li.klass.fhem.behavior.dim.DimmableBehavior
import li.klass.fhem.behavior.toggle.OnOffBehavior
import li.klass.fhem.connection.backend.ConnectionService
import li.klass.fhem.connection.backend.DummyServerSpec
import li.klass.fhem.connection.backend.FHEMServerSpec
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.devices.backend.GenericDeviceService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.update.backend.DeviceListService
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.R)
@Singleton
class AndroidControlsProviderService : ControlsProviderService() {
    private var updatePublisher: ReplayProcessor<Control>? = null

    @Inject
    lateinit var deviceListService: DeviceListService

    @Inject
    lateinit var genericDeviceService: GenericDeviceService

    @Inject
    lateinit var connectionService: ConnectionService

    @Inject
    lateinit var onOffBehavior: OnOffBehavior

    lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        val daggerComponent = AndFHEMApplication.application?.daggerComponent
        daggerComponent?.inject(this)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                val deviceNames = intent.getStringArrayListExtra(BundleExtraKeys.UPDATED_DEVICE_NAMES)
                val connectionId = intent.getStringExtra(BundleExtraKeys.CONNECTION_ID)

                if (deviceNames == null || connectionId == null) {
                    return
                }

                handleDeviceUpdate(deviceNames, connectionId)
            }

        }
        registerReceiver(broadcastReceiver, IntentFilter(Actions.DEVICES_UPDATED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val context: Context = baseContext
        val intent = Intent()
        val pendingIntent =
                PendingIntent.getActivity(
                        context, 1, intent,
                        PendingIntent.FLAG_IMMUTABLE
                )

        val controls = connectionService.listAll()
                .filterNot { it is DummyServerSpec }
                .flatMap {
                    deviceListService.getAllRoomsDeviceList(it.id).allDevices.map { device ->
                        device.toControl(controlContext, it)
                                ?.toStatelessControl(pendingIntent)
                    }
                }
                .filterNotNull()

        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls))
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {

        updatePublisher = ReplayProcessor.create()
        controlIds
                .map {
                    ControlId.fromAndroid(it)
                }
                .mapNotNull {
                    val device = deviceListService.getDeviceForName(it.deviceName, it.connectionId)
                    val connection = connectionService.getServerFor(it.connectionId)
                    if (device == null || connection == null) null else device to connection
                }
                .mapNotNull { (device, connection) -> device.toStatefulControl(connection) }
                .forEach { updatePublisher?.onNext(it) }

        return FlowAdapters.toFlowPublisher(updatePublisher)
    }

    override fun performControlAction(androidControlId: String, action: ControlAction, consumer: Consumer<Int>) {
        val controlId = ControlId.fromAndroid(androidControlId)
        val device = deviceListService.getDeviceForName(controlId.deviceName, controlId.connectionId)
                ?: return
        when (action) {
            is BooleanAction -> {
                val newState = if (action.newState) "on" else "off"
                genericDeviceService.setState(device.xmlListDevice, newState, controlId.connectionId, true)
            }
            is FloatAction -> {
                if (action.templateId.startsWith("temperature_")) {
                    val state = device.setList.getFirstPresentStateOf("desired-temp", "desiredTemp")
                            ?: return
                    genericDeviceService.setSubState(device.xmlListDevice, state, action.newValue.toString(), controlId.connectionId, true)
                } else {
                    val dimmable = DimmableBehavior.behaviorFor(device, connectionId = null)
                            ?: return
                    val stateName = dimmable.behavior.getStateName()
                    genericDeviceService.setSubState(device.xmlListDevice, stateName, action.newValue.toString(), controlId.connectionId, true)
                }
            }
        }
        consumer.accept(ControlAction.RESPONSE_OK)
    }

    fun handleDeviceUpdate(deviceNames: List<String>, connectionId: String) {
        val publisher = updatePublisher ?: return
        val connection = connectionService.getServerFor(connectionId) ?: return
        val controls = allWatchedControls()
                .map { it.controlId }
                .distinct()
                .map { ControlId.fromAndroid(it) }

        controls
                .filter { deviceNames.contains(it.deviceName) && connectionId == it.connectionId }
                .mapNotNull { deviceListService.getDeviceForName(it.deviceName, connectionId) }
                .mapNotNull { it.toStatefulControl(connection) }
                .forEach { publisher.onNext(it) }
    }

    private fun FhemDevice.toStatefulControl(connection: FHEMServerSpec): Control? {
        val pendingIntent = NavDeepLinkBuilder(baseContext)
                .setComponentName(AndFHEMMainActivity::class.java)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.deviceDetailRedirectFragment)
                .setArguments(DeviceDetailRedirectFragmentArgs(name, null).toBundle())
                .createPendingIntent()

        return toControl(controlContext, connection)?.toStatefulControl(pendingIntent, controlContext)
    }

    private fun allWatchedControls() = updatePublisher?.values?.mapNotNull { it as? Control }
            ?: emptyList()

    private val controlContext by lazy {
        ControlContext(onOffBehavior)
    }
}