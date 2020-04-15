package li.klass.fhem.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import li.klass.fhem.activities.AndFHEMMainActivity
import li.klass.fhem.activities.StartupActivity
import li.klass.fhem.appwidget.ui.selection.BigWidgetSelectionActivity
import li.klass.fhem.appwidget.ui.selection.MediumWidgetSelectionActivity
import li.klass.fhem.appwidget.ui.selection.SmallWidgetSelectionActivity
import li.klass.fhem.graph.ui.GraphActivity

@Module
interface ActivityModule {
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun mainActivityInjector(): AndFHEMMainActivity
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun startupActivityInjector(): StartupActivity
    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun graphActivity(): GraphActivity

    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun smallWidgetSelectionActivity(): SmallWidgetSelectionActivity

    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun mediumWidgetSelectionActivity(): MediumWidgetSelectionActivity

    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    fun bigWidgetSelectionActivity(): BigWidgetSelectionActivity
}