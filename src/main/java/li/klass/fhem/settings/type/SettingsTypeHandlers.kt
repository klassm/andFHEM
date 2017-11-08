package li.klass.fhem.settings.type

import javax.inject.Inject

class SettingsTypeHandlers @Inject constructor(
        private val appearance: AppearanceTypeHandler,
        private val errors: ErrorsTypeHandler,
        private val widget: WidgetTypeHandler,
        private val others: OthersTypeHandler
) {
    fun handlerFor(fragmentKey: String): SettingsTypeHandler {
        return listOf(appearance, errors, widget, others)
                .first { it.canHandle(fragmentKey) }
    }
}