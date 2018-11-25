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

package li.klass.fhem.conversion.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.conversion.view.*
import li.klass.fhem.R
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.NumberSystemUtil

class ConversionFragment : BaseFragment() {

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.conversion, container, false)

        val inputField = view.input
        val hexToQuatButton = view.hexToQuat
        val quatToHexButton = view.quatToHex

        hexToQuatButton.setOnClickListener {
            try {
                val input = inputField.text.toString()
                val result = NumberSystemUtil.hexToQuaternary(input, 0)
                setResult(result)
            } catch (e: IllegalArgumentException) {
                setResult(activity!!.getString(R.string.error))
            }
        }
        quatToHexButton.setOnClickListener {
            try {
                val input = inputField.text.toString()
                val result = NumberSystemUtil.quaternaryToHex(input)
                setResult(result)
            } catch (e: IllegalArgumentException) {
                setResult(activity!!.getString(R.string.error))
            }
        }
        return view
    }

    private fun setResult(result: String) {
        view?.result?.text = result
    }

    override fun mayPullToRefresh(): Boolean = false

    override fun getTitle(context: Context): CharSequence? = context.getString(R.string.conversion)

    override suspend fun update(refresh: Boolean) {}
}
