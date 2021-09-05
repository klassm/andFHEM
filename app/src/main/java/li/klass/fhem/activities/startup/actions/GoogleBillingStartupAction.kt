package li.klass.fhem.activities.startup.actions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.billing.LicenseService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GoogleBillingStartupAction @Inject constructor(
        private val licenseService: LicenseService
) : StartupAction(R.string.currentStatus_billing) {
    override suspend fun run() {
        GlobalScope.launch(Dispatchers.Main) {
            val premiumStatus = licenseService.premiumStatus()
            logger.info("initializeGoogleBilling() : premium=$premiumStatus")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GoogleBillingStartupAction::class.java)
    }
}