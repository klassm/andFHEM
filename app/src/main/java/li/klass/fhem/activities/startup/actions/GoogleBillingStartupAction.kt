package li.klass.fhem.activities.startup.actions

import li.klass.fhem.R
import li.klass.fhem.billing.IsPremiumListener
import li.klass.fhem.billing.LicenseService
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GoogleBillingStartupAction @Inject constructor(
        private val licenseService: LicenseService
) : StartupAction(R.string.currentStatus_billing) {
    override fun run() {
        licenseService.isPremium(object : IsPremiumListener {
            override fun isPremium(isPremium: Boolean) {
                logger.info("initializeGoogleBilling() : connection was " + (if (isPremium) "successful" else "not successful"))
            }
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GoogleBillingStartupAction::class.java)
    }
}