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
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import li.klass.fhem.R
import li.klass.fhem.activities.locale.AttributeType
import li.klass.fhem.activities.locale.LocaleIntentConstants
import li.klass.fhem.constants.BundleExtraKeys
import li.klass.fhem.databinding.LocaleConditionQueryEditBinding
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.fragments.device.DeviceNameListFragment
import li.klass.fhem.util.getNavigationResult
import li.klass.fhem.util.view.updateIfChanged
import javax.inject.Inject

class ConditionQueryLocaleEditFragment @Inject constructor() : Fragment() {

    private val viewModel by navGraphViewModels<ConditionQueryLocaleEditViewModel>(R.id.nav_graph)
    private lateinit var binding: LocaleConditionQueryEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val superView = super.onCreateView(inflater, container, savedInstanceState)
        if (superView != null) {
            return superView
        }

        binding = LocaleConditionQueryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindSave()
        bindAttributeType(view)
        bindDeviceName()
        bindAttributeName()
        bindAttributeValue()

        activity?.intent?.let {
            setInitialDataFrom(it)
        }
    }

    private fun setInitialDataFrom(intent: Intent) {
        viewModel.deviceName.value = intent.getStringExtra(BundleExtraKeys.DEVICE_NAME)
        viewModel.attributeValue.value = intent.getStringExtra(BundleExtraKeys.DEVICE_TARGET_STATE)
        viewModel.attributeName.value = intent.getStringExtra(BundleExtraKeys.ATTRIBUTE_NAME)
        viewModel.attributeType.value = intent.getStringExtra(BundleExtraKeys.ATTRIBUTE_TYPE)
    }

    private fun bindAttributeValue() {
        binding.attributeValue.apply {
            doOnTextChanged { text, _, _, _ ->
                viewModel.attributeValue.value = text?.toString()
            }
            viewModel.attributeValue
                .observe(viewLifecycleOwner) {
                    updateIfChanged(it)
                }
        }
    }

    private fun bindAttributeName() {
        binding.attributeName.apply {

            doOnTextChanged { text, _, _, _ ->
                viewModel.attributeName.value = text?.toString()
            }
            viewModel.attributeName.observe(viewLifecycleOwner) {
                updateIfChanged(it)
            }
        }
    }

    private fun bindDeviceName() {
        binding.deviceName.apply {
            viewModel.deviceName.observe(viewLifecycleOwner) {
                text = it
            }
        }
        binding.setDevice.setOnClickListener { handleDeviceSelection() }
        getNavigationResult<FhemDevice>()?.observe(viewLifecycleOwner) { device ->
            viewModel.deviceName.value = device.name
        }
    }

    private fun bindAttributeType(view: View) {
        binding.attributeType.apply {
            adapter = ArrayAdapter(
                view.context,
                android.R.layout.simple_dropdown_item_1line,
                AttributeType.values()
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val type = AttributeType.atPosition(position).description
                    viewModel.attributeType.value = type
                }
            }

            viewModel.attributeType.observe(viewLifecycleOwner) { type ->
                type?.let { setSelection(AttributeType.positionFor(it)) }
            }
        }
    }

    private fun bindSave() {
        binding.save.apply {
            isEnabled = false
            viewModel.allAttributesSet.observe(viewLifecycleOwner) {
                isEnabled = it
            }
            setOnClickListener { handleSave() }
        }
    }

    private fun handleSave() {
        activity?.apply {
            val deviceName = viewModel.deviceName.value
            val type = viewModel.attributeType.value
            val name = viewModel.attributeName.value
            val value = viewModel.attributeValue.value
            setResult(Activity.RESULT_OK, Intent()
                .putExtra(BundleExtraKeys.DEVICE_TARGET_STATE, value)
                .putExtra(BundleExtraKeys.DEVICE_NAME, deviceName)
                .putExtra(BundleExtraKeys.ATTRIBUTE_NAME, name)
                .putExtra(BundleExtraKeys.ATTRIBUTE_TYPE, type)
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