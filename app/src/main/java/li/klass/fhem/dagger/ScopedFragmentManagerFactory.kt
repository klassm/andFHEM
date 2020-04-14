package li.klass.fhem.dagger


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject
import javax.inject.Provider

// see https://github.com/Lingviston/dagger-android-fragment-factory-scope/blob/master/app/src/main/java/by/ve/demo/di/ScopedFragmentFactory.kt

/**
 * This [FragmentFactory] is going to be injected into activities. Injection will be done using
 * target activity's [dagger.Subcomponent] thus it will have access to all unscoped dependencies
 * together with the ones of [javax.inject.Singleton] and [by.ve.demo.di.scopes.ActivityScope] scopes.
 */
class ScopedFragmentFactory @Inject constructor(
        private val androidInjector: DispatchingAndroidInjector<Any>
) : FragmentFactory() {

    private val providers = FragmentProviders()

    /**
     * We need to inject new set of dependencies during every fragment creation because different
     * because each fragment needs it's own set of dependencies. I.e. [by.ve.demo.di.scopes.FragmentScope]
     * dependencies must not be shared across different instances of fragments.
     *
     * Calling [androidInjector#inject] every time during fragment instantiation we guarantee the
     * creation of the fragment's [dagger.Subcomponent] for every instance of fragment. Just like it
     * works with members injection.
     */
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        androidInjector.inject(providers)

        val clazz = loadFragmentClass(classLoader, className)
        return providers[clazz]?.get() ?: super.instantiate(classLoader, className)
    }

    /**
     * We can't inject collection of fragment [Provider]s directly into [ScopedFragmentFactory]
     * because that would require declaring an injectable field in it:
     *
     * ```
     * class ScopedFragmentFactory @Inject constructor(
     *     private val androidInjector: DispatchingAndroidInjector<Any>
     * ) : FragmentFactory() {
     *
     *     @Inject
     *     lateinit var fragmentProviders: MutableMap<Class<out Fragment>, Provider<Fragment>>
     * }
     * ```
     *
     * If we do so then during injection of the factory into the target activity [dagger.android.AndroidInjector]
     * associated with that activity will try to inject into field of [ScopedFragmentFactory] as well.
     * And this will fail, because fragments and their dependencies are not available to the target
     * activity's [dagger.Subcomponent].
     *
     * Thus we need to postpone injection of the factory dependencies somehow. To do that the [FragmentProviders]
     * class is introduced. Whenever we want to instantiate the fragment we must inject the fresh set
     * of dependencies into [FragmentProviders] instance. That injection will happen using the
     * [dagger.Subcomponent] of target activity's [dagger.Subcomponent] and will give us access to
     * the fragment scoped dependencies.
     */
    class FragmentProviders {

        @Inject
        lateinit var fragmentProviders: MutableMap<Class<out Fragment>, Provider<Fragment>>

        operator fun get(clazz: Class<out Fragment>) = fragmentProviders[clazz]
    }
}