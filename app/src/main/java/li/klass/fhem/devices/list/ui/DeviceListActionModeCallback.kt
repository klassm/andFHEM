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

package li.klass.fhem.devices.list.ui

import android.content.Context
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import li.klass.fhem.R
import li.klass.fhem.devices.list.favorites.backend.FavoritesService
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.util.device.DeviceActionUIService
import li.klass.fhem.widget.notification.NotificationSettingView
import org.jetbrains.anko.coroutines.experimental.bg

class DeviceListActionModeCallback constructor(
        private val favoritesService: FavoritesService,
        private val deviceActionUIService: DeviceActionUIService,
        private val device: FhemDevice,
        private val isFavorite: Boolean,
        private val activityContext: Context,
        private val updateListener: () -> Unit
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

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_favorites_add -> {
                runBlocking {
                    async {
                        favoritesService.addFavorite(device.name)
                    }.await()
                    Toast.makeText(activityContext, R.string.context_favoriteadded, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_favorites_remove -> {
                runBlocking {
                    async {
                        favoritesService.removeFavorite(device.name)
                    }.await()
                    Toast.makeText(activityContext, R.string.context_favoriteremoved, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.menu_rename -> deviceActionUIService.renameDevice(activityContext, device)
            R.id.menu_delete -> deviceActionUIService.deleteDevice(activityContext, device)
            R.id.menu_room -> deviceActionUIService.moveDevice(activityContext, device)
            R.id.menu_alias -> deviceActionUIService.setAlias(activityContext, device)
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
