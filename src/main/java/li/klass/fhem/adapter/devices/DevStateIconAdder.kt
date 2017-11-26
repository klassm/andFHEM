package li.klass.fhem.adapter.devices

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import li.klass.fhem.GlideApp
import li.klass.fhem.R
import li.klass.fhem.billing.IsPremiumListener
import li.klass.fhem.billing.LicenseService
import li.klass.fhem.connection.backend.DataConnectionSwitch
import li.klass.fhem.connection.backend.FHEMWEBConnection
import li.klass.fhem.domain.core.FhemDevice
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
        imageView.visibility = View.VISIBLE

        licenseService.isPremium(object : IsPremiumListener {
            override fun isPremium(isPremium: Boolean) {
                if (isPremium) {
                    val connection = currentProvider as FHEMWEBConnection
                    val url = "${connection.server.url}/images/default/${icon.image}.png"
                    val authHeader = connection.basicAuthHeaders.authorization
                    val glideUrl = GlideUrl(url, LazyHeaders.Builder()
                            .addHeader("Authorization", authHeader)
                            .build())

                    GlideApp.with(imageView.context)
                            .load(glideUrl)
                            .error(R.drawable.empty)
                            .into(imageView)
                }
            }
        })
    }
}