package li.klass.fhem.adapter.devices.genericui.availableTargetStates

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import li.klass.fhem.R
import li.klass.fhem.databinding.SetlistDatepickerBinding
import li.klass.fhem.domain.core.FhemDevice
import li.klass.fhem.domain.setlist.SetListEntry
import li.klass.fhem.domain.setlist.typeEntry.DateTimeSetListEntry
import org.joda.time.DateTime

class DateTimeSetListEntryTargetStateHandler : SetListTargetStateHandler<FhemDevice> {

    override fun canHandle(entry: SetListEntry) = entry is DateTimeSetListEntry

    private fun calculateDisplayedMinutes(stepSize: Int): List<Int> {
        val iterations = 60 / stepSize
        return (0 until iterations).map { it * stepSize }
    }

    override fun handle(entry: SetListEntry, context: Context, device: FhemDevice, callback: OnTargetStateSelectedCallback) {
        val config = (entry as DateTimeSetListEntry).config
        val binding = SetlistDatepickerBinding.inflate(LayoutInflater.from(context), null, false)
        binding.timePicker.setMinutesDisplayedValues(calculateDisplayedMinutes(config.step))
        binding.datePicker.visibility = if (config.datePicker) View.VISIBLE else View.GONE

        AlertDialog.Builder(context)
            .setTitle(device.aliasOrName + " " + entry.key)
            .setView(binding.root)
            .setNegativeButton(R.string.cancelButton) { dialog, _ ->
                GlobalScope.launch(Dispatchers.Main) {
                    callback.onNothingSelected(device)
                }
                dialog.dismiss()
            }
                .setPositiveButton(R.string.okButton) { dialog, _ ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val dateTime = DateTime(
                            binding.datePicker.year,
                            binding.datePicker.month + 1,
                            binding.datePicker.dayOfMonth,
                            binding.timePicker.hours,
                            binding.timePicker.minutes
                        )

                        val text = dateTime.toString(config.format)
                        callback.onSubStateSelected(device, entry.key, text)
                    }
                    dialog.dismiss()
                }
                .show()
    }
}