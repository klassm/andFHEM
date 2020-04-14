package li.klass.fhem.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.settings.SettingsKeys
import li.klass.fhem.ui.FragmentType
import li.klass.fhem.util.ApplicationProperties
import org.slf4j.LoggerFactory
import javax.inject.Inject

class MainFragment @Inject constructor(
        private val applicationProperties: ApplicationProperties
): Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasFavorites = activity?.intent?.extras?.getBoolean(BundleExtraKeys.HAS_FAVORITES) ?: false
        val startupView = applicationProperties.getStringSharedPreference(SettingsKeys.STARTUP_VIEW,
                FragmentType.FAVORITES.name)
        var preferencesStartupFragment: FragmentType? = FragmentType.forEnumName(startupView) ?: FragmentType.ALL_DEVICES
        logger.debug("handleStartupFragment() : startup view is $preferencesStartupFragment")

        if (preferencesStartupFragment == null) {
            preferencesStartupFragment = FragmentType.ALL_DEVICES
        }

        var fragmentType: FragmentType = preferencesStartupFragment
        if (fragmentType == FragmentType.FAVORITES && !hasFavorites) {
            fragmentType = FragmentType.ALL_DEVICES
        }

        val action = when(fragmentType) {
            FragmentType.FAVORITES -> MainFragmentDirections.actionToFavorites()
            FragmentType.ROOM_LIST -> MainFragmentDirections.actionToRoomList()
            else -> MainFragmentDirections.actionToAllDevices()
        }

        findNavController().popBackStack()
        findNavController().navigate(action)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MainFragment::class.java)
    }
}