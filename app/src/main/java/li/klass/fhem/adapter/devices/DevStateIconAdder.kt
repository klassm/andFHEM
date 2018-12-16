package li.klass.fhem.adapter.devices

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.domain.core.FhemDevice
import org.slf4j.LoggerFactory
import javax.inject.Inject

class DevStateIconAdder @Inject constructor(val dataConnectionSwitch: DataConnectionSwitch, val licenseService: LicenseService) {

    fun addDevStateIconIfRequired(value: String?, device: FhemDevice, imageView: ImageView?) {
        imageView ?: return

        val currentProvider = dataConnectionSwitch.getProviderFor()
        val isFhemweb = currentProvider is FHEMWEBConnection
        val icon = device.devStateIcons.iconFor(value ?: "")
        if (!isFhemweb || icon == null) {
            imageView.visibility = View.GONE
            return
        }
        logger.info("addDevStateIconIfRequired(icon=$icon, device=${device.name})")
        imageView.visibility = View.VISIBLE

        GlobalScope.launch(Dispatchers.Main) {
            val isPremium = licenseService.isPremium()
            if (isPremium && currentProvider is FHEMWEBConnection) {
                val url = "${currentProvider.server.url}/images/default/${icon.image}.png"
                val authHeader = currentProvider.basicAuthHeaders.authorization
                val glideUrl = GlideUrl(url, LazyHeaders.Builder()
                        .addHeader("Authorization", authHeader)
                        .build())

                logger.info("addDevStateIconIfRequired - loading icon from $url")
                GlideApp.with(imageView.context)
                        .load(glideUrl)
                        .error(R.drawable.empty)
                        .into(imageView)
            }

        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(DevStateIconAdder::class.java)!!
    }
}