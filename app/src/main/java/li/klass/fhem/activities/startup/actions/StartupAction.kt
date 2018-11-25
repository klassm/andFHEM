package li.klass.fhem.activities.startup.actions

abstract class StartupAction(val statusText: Int) {
    abstract suspend fun run()
}