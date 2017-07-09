package li.klass.fhem.fragments.core

import android.content.Context
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import li.klass.fhem.R
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.service.room.FavoritesService
import li.klass.fhem.util.device.DeviceActionUtil
import li.klass.fhem.widget.notification.NotificationSettingView
import org.jetbrains.anko.coroutines.experimental.bg

class DeviceListActionModeCallback constructor(
        val favoritesService: FavoritesService,
        val device: FhemDevice,
        val isFavorite: Boolean,
        val activityContext: Context,
        val updateListener: () -> Unit
) : ActionMode.Callback {

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater = actionMode.menuInflater
        inflater.inflate(R.menu.device_menu, menu)
        if (isFavorite) {
            menu.removeItem(R.id.menu_favorites_add)
        } else {
            menu.removeItem(R.id.menu_favorites_remove)
        }
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_favorites_add -> {
                async(UI) {
                    bg {
                        favoritesService.addFavorite(activityContext, device.name)
                    }.await()
                    Toast.makeText(activityContext, R.string.context_favoriteadded, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_favorites_remove -> {
                async(UI) {
                    bg {
                        favoritesService.removeFavorite(activityContext, device.name)
                    }.await()
                    Toast.makeText(activityContext, R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_rename -> DeviceActionUtil.renameDevice(activityContext, device)
            R.id.menu_delete -> DeviceActionUtil.deleteDevice(activityContext, device)
            R.id.menu_room -> DeviceActionUtil.moveDevice(activityContext, device)
            R.id.menu_alias -> DeviceActionUtil.setAlias(activityContext, device)
            R.id.menu_notification -> handleNotifications(device.name)
            else -> return false
        }
        actionMode.finish()
        updateListener()
        return true
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
    }

    private fun handleNotifications(deviceName: String) {
        NotificationSettingView(activityContext, deviceName).show(activityContext)
    }
}
