package li.klass.fhem.dagger

import dagger.Module
import dagger.android.ContributesAndroidInjector
import li.klass.fhem.fragments.MainFragment

/**
 * This module most be included into the method, which [ContributesAndroidInjector] into an activity,
 * which will show the fragments.
 */
@Module
interface FragmentContributorModule {
    @ContributesAndroidInjector(modules = [FragmentsBindingModule::class])
    fun contributeFragmentFactory(): ScopedFragmentFactory.FragmentProviders
}