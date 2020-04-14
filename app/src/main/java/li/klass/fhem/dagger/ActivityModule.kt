package li.klass.fhem.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.activities.StartupActivity
import li.klass.fhem.graph.ui.GraphActivity

@Module
interface ActivityModule {
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun mainActivityInjector(): AndFHEMMainActivity
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun startupActivityInjector(): StartupActivity
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun graphActivity(): GraphActivity
}