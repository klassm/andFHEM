package li.klass.fhem.fcm.history.view

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import li.klass.fhem.R
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.DateFormatUtil
import org.joda.time.LocalDate

abstract class FcmHistoryBaseFragment<out ADAPTER : RecyclerView.Adapter<*>>() : BaseFragment() {

    fun fillView() {
        val today = LocalDate.now()

        val context = activity ?: return

        recyclerView.layoutManager = LinearLayoutManager(activity as Context)
        recyclerView.adapter = getAdapter()

        val dateFormat = DateFormatUtil.ANDFHEM_DATE_FORMAT
        selectedDateTextView.text = dateFormat.print(today)
        changeDateButton.setOnClickListener {
            val lastDate = dateFormat.parseLocalDate(selectedDateTextView.text.toString())
            DatePickerDialog(context, R.style.alertDialog, { _, year, month, day ->
                selectedDateTextView.text = dateFormat.print(LocalDate(year, month + 1, day))
                updateAsync(false)
            }, lastDate.year, lastDate.monthOfYear - 1, lastDate.dayOfMonth).show()
        }
    }

    abstract fun getAdapter(): ADAPTER

    abstract val selectedDateTextView: TextView
    abstract val changeDateButton: ImageButton
    abstract val recyclerView: RecyclerView

    abstract suspend fun doUpdateView(localDate: LocalDate, view: View)


    override suspend fun update(refresh: Boolean) {
        val myView = view
        myView ?: return
        val selectedDate =
            DateFormatUtil.ANDFHEM_DATE_FORMAT.parseLocalDate(selectedDateTextView.text.toString())

        doUpdateView(selectedDate, myView)
    }

    fun showEmptyViewIfRequired(isEmpty: Boolean, view: View, emptyView: View) {
        if (isEmpty) {
            emptyView.visibility = View.VISIBLE
            view.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            view.visibility = View.VISIBLE
        }
    }
}