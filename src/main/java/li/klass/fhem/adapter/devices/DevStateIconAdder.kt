package li.klass.fhem.adapter.devices

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import li.klass.fhem.R
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fhem.DataConnectionSwitch
import li.klass.fhem.fhem.FHEMWEBConnection
import javax.inject.Inject

class DevStateIconAdder @Inject constructor(val dataConnectionSwitch: DataConnectionSwitch, val licenseService: LicenseService) {

    fun addDevStateIconIfRequired(context: Context, value: String?, device: FhemDevice, imageView: ImageView?) {
        imageView ?: return

        val currentProvider = dataConnectionSwitch.getProviderFor(context)
        val isFhemweb = currentProvider is FHEMWEBConnection
        val icon = device.devStateIcons.iconFor(value ?: "")
        if (!isFhemweb || icon == null) {
            imageView.visibility = View.GONE
            return
        }
        imageView.visibility = View.VISIBLE

        licenseService.isPremium({
            val connection = currentProvider as FHEMWEBConnection
            val url = connection.server.url + "/images/default/" + icon + ".png"
            val authHeader = connection.basicAuthHeaders.authorization
            val glideUrl = GlideUrl(url, LazyHeaders.Builder()
                    .addHeader("Authorization", authHeader)
                    .build())

            Glide.with(context)
                    .load(glideUrl)
                    .error(R.drawable.empty)
                    .crossFade()
                    .into(imageView)
        }, context)
    }
}