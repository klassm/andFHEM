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
import li.klass.fhem.R
import li.klass.fhem.databinding.ConversionBinding
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.NumberSystemUtil
import javax.inject.Inject

class ConversionFragment @Inject constructor(): BaseFragment() {

    private lateinit var binding: ConversionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ConversionBinding.inflate(inflater, container, false)

        val inputField = binding.input
        val hexToQuatButton = binding.hexToQuat
        val quadToHexButton = binding.quatToHex

        hexToQuatButton.setOnClickListener {
            try {
                val input = inputField.text.toString()
                val result = NumberSystemUtil.hexToQuaternary(input, 0)
                setResult(result)
            } catch (e: IllegalArgumentException) {
                setResult(requireActivity().getString(R.string.error))
            }
        }
        quadToHexButton.setOnClickListener {
            try {
                val input = inputField.text.toString()
                val result = NumberSystemUtil.quaternaryToHex(input)
                setResult(result)
            } catch (e: IllegalArgumentException) {
                setResult(requireActivity().getString(R.string.error))
            }
        }
        return binding.root
    }

    private fun setResult(result: String) {
        binding.result.text = result
    }

    override fun mayPullToRefresh(): Boolean = false

    override fun getTitle(context: Context) = context.getString(R.string.conversion)

    override suspend fun update(refresh: Boolean) {}
}
