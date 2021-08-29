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

package li.klass.fhem.widget.deviceFunctionality

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import com.ericharlow.DragNDrop.DragNDropAdapter
import li.klass.fhem.R
import li.klass.fhem.databinding.DeviceTypeListItemBinding
import org.slf4j.LoggerFactory

class DeviceFunctionalityOrderAdapter(
    context: Context,
    data: ArrayList<DeviceFunctionalityPreferenceWrapper>,
    val listener: OrderActionListener
) : DragNDropAdapter<DeviceFunctionalityPreferenceWrapper>(context, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) as DeviceFunctionalityPreferenceWrapper
        val binding =
            if (convertView != null) DeviceTypeListItemBinding.bind(convertView) else DeviceTypeListItemBinding.inflate(
                inflater
            )

        updateContent(item, binding)

        return binding.root
    }

    private fun updateContent(
        item: DeviceFunctionalityPreferenceWrapper,
        binding: DeviceTypeListItemBinding
    ) {

        binding.changeVisibility.setOnClickListener {
            listener.deviceTypeReordered(item, OrderAction.VISIBILITY_CHANGE)
            notifyDataSetChanged()
        }

        val nameView = binding.name
        nameView.text = item.deviceFunctionality.getCaptionText(context)

        logger.debug(
            "updateContent() - drawing content for {}, visibility is {}",
            item.deviceFunctionality.name,
            item.isVisible
        )

        if (item.isVisible) {
            nameView.paintFlags = Paint.FAKE_BOLD_TEXT_FLAG
            binding.changeVisibility.setImageResource(R.drawable.visible)
        } else {
            nameView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.FAKE_BOLD_TEXT_FLAG
            binding.changeVisibility.setImageResource(R.drawable.invisible)
        }
    }

    override fun doSort(): Boolean = false

    enum class OrderAction {
        UP, DOWN, VISIBILITY_CHANGE
    }

    interface OrderActionListener {
        fun deviceTypeReordered(wrapper: DeviceFunctionalityPreferenceWrapper, action: OrderAction)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeviceFunctionalityOrderAdapter::class.java)
    }
}
