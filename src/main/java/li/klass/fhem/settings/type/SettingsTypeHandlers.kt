package li.klass.fhem.settings.type

import javax.inject.Inject

class SettingsTypeHandlers @Inject constructor(
        private val appearance: AppearanceTypeHandler,
        private val errors: ErrorsTypeHandler,
        private val widget: WidgetTypeHandler,
        private val others: OthersTypeHandler,
        private val cloudMessaging: CloudMessagingTypeHandler,
        private val autoUpdate: AutoUpdateTypeHandler,
        private val connections: ConnectionsTypeHandler,
        private val backup: BackupTypeHandler
) {
    fun handlerFor(fragmentKey: String): SettingsTypeHandler {
        return listOf(appearance, errors, widget, others, cloudMessaging, autoUpdate,
                connections, backup)
                .first { it.canHandle(fragmentKey) }
    }
}