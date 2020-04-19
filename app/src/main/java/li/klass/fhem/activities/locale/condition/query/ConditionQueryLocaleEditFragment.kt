package li.klass.fhem.activities.locale.condition.query

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.android.synthetic.main.locale_condition_query_edit.*
import kotlinx.android.synthetic.main.locale_condition_query_edit.view.*
import li.klass.fhem.R
import li.klass.fhem.activities.locale.AttributeType
import li.klass.fhem.activities.locale.LocaleIntentConstants
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.util.getNavigationResult
import li.klass.fhem.util.view.updateIfChanged
import org.jetbrains.anko.sdk25.coroutines.onClick
import javax.inject.Inject

class ConditionQueryLocaleEditFragment @Inject constructor() : Fragment() {

    private val viewModel by navGraphViewModels<ConditionQueryLocaleEditViewModel>(R.id.nav_graph)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            super.onCreateView(inflater, container, savedInstanceState)
                    ?: inflater.inflate(R.layout.locale_condition_query_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindSave(view)
        bindAttributeType(view)
        bindDeviceName(view)
        bindAttributeName(view)
        bindAttributeValue()

        activity?.intent?.let {
            setInitialDataFrom(it)
        }
    }

    private fun setInitialDataFrom(intent: Intent) {
        viewModel.deviceName.value = intent.getStringExtra(DEVICE_NAME)
        viewModel.attributeValue.value = intent.getStringExtra(DEVICE_TARGET_STATE)
        viewModel.attributeName.value = intent.getStringExtra(ATTRIBUTE_NAME)
        viewModel.attributeType.value = intent.getStringExtra(ATTRIBUTE_TYPE)
    }

    private fun bindAttributeValue() {
        attributeValue.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.attributeValue.value = text?.toString()
            }
            viewModel.attributeValue
                    .observe(viewLifecycleOwner, Observer {
                        updateIfChanged(it)
                    })
        }
    }

    private fun bindAttributeName(view: View) {
        view.attributeName.apply {

            doOnTextChanged { text, _, _, _ ->
                viewModel.attributeName.value = text?.toString()
            }
            viewModel.attributeName.observe(viewLifecycleOwner, Observer {
                updateIfChanged(it)
            })
        }
    }

    private fun bindDeviceName(view: View) {
        view.deviceName.apply {
            viewModel.deviceName.observe(viewLifecycleOwner, Observer {
                text = it
            })
        }
        view.setDevice.onClick { handleDeviceSelection() }
        getNavigationResult<FhemDevice>()?.observe(viewLifecycleOwner, Observer { device ->
            viewModel.deviceName.value = device.name
        })
    }

    private fun bindAttributeType(view: View) {
        view.attributeType.apply {
            adapter = ArrayAdapter(view.context, android.R.layout.simple_dropdown_item_1line, AttributeType.values())

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val type = AttributeType.atPosition(position).description
                    viewModel.attributeType.value = type
                }
            }

            viewModel.attributeType.observe(viewLifecycleOwner, Observer {
                setSelection(AttributeType.positionFor(it))
            })
        }
    }

    private fun bindSave(view: View) {
        view.save.apply {
            isEnabled = false
            viewModel.allAttributesSet.observe(viewLifecycleOwner, Observer {
                isEnabled = it
            })
            onClick { handleSave() }
        }
    }

    private fun handleSave() {
        activity?.apply {
            val deviceName = viewModel.deviceName.value
            val type = viewModel.attributeType.value
            val name = viewModel.attributeName.value
            val value = viewModel.attributeValue.value
            setResult(Activity.RESULT_OK, Intent()
                    .putExtra(DEVICE_TARGET_STATE, value)
                    .putExtra(DEVICE_NAME, deviceName)
                    .putExtra(ATTRIBUTE_NAME, name)
                    .putExtra(ATTRIBUTE_TYPE, type)
                    .putExtra(LocaleIntentConstants.EXTRA_STRING_BLURB, "$deviceName ($type $name=$value)"))
            finish()
        }

    }

    private fun handleDeviceSelection() {
        findNavController()
                .navigate(ConditionQueryLocaleEditFragmentDirections.actionConditionQueryLocaleEditFragmentToDeviceNameSelectionFragment(
                        object : DeviceNameListFragment.DeviceFilter {
                            override fun isSelectable(device: FhemDevice): Boolean = true
                        }, null
                ))
    }
}