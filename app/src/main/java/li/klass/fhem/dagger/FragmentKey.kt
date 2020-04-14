package li.klass.fhem.dagger

import androidx.fragment.app.Fragment
import dagger.MapKey
import kotlin.reflect.KClass

// see https://github.com/Lingviston/dagger-android-fragment-factory-scope/blob/master/app/src/main/java/by/ve/demo/di/ScopedFragmentFactory.kt

@MapKey
annotation class FragmentKey(val value: KClass<out Fragment>)