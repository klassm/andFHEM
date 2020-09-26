package li.klass.fhem.adapter.devices.core.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToAllDevicesFragment
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToDeviceDetailFragment
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToFloorplanFragment
import li.klass.fhem.adapter.devices.core.detail.DeviceDetailRedirectFragmentDirections.Companion.actionDeviceDetailRedirectFragmentToWebViewFragment
import li.klass.fhem.update.backend.DeviceListService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class DeviceDetailRedirectFragment @Inject constructor(
        private val deviceListService: DeviceListService
) : Fragment() {

    val args: DeviceDetailRedirectFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device = deviceListService.getDeviceForName(args.deviceName, args.connectionId)

        val action = when (device?.xmlListDevice?.type) {
            null -> {
                logger.warn("onCreate - cannot find device ${args.deviceName} in connection ${args.connectionId} - redirecting to all devices")
                actionDeviceDetailRedirectFragmentToAllDevicesFragment()
            }
            "weblink" -> {
                actionDeviceDetailRedirectFragmentToWebViewFragment(
                        device.xmlListDevice.getInternal("LINK")!!
                )
            }
            "FLOORPLAN" -> {
                actionDeviceDetailRedirectFragmentToFloorplanFragment(
                        args.deviceName
                )
            }
            else -> {
                actionDeviceDetailRedirectFragmentToDeviceDetailFragment(
                        deviceName = device.name,
                        connectionId = args.connectionId
                )
            }
        }

        val navController = findNavController()
        navController.navigate(action)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DeviceDetailRedirectFragment::class.java)
    }
}