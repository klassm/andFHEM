package li.klass.fhem.fcm.history.view

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fcm_history_updates.view.*
import li.klass.fhem.fcm.history.data.FcmHistoryService
import li.klass.fhem.fragments.core.BaseFragment
import li.klass.fhem.util.DateFormatUtil
import org.joda.time.LocalDate
import javax.inject.Inject

abstract class FcmHistoryBaseFragment<out ADAPTER : RecyclerView.Adapter<*>>(val layoutId: Int) : BaseFragment() {

    @Inject
    lateinit var fcmHistoryService: FcmHistoryService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutId, container, false)

        val today = LocalDate.now()

        view ?: return view

        val recyclerView = getRecyclerViewFrom(view)
        recyclerView.layoutManager = LinearLayoutManager(activity as Context)
        recyclerView.adapter = getAdapter()

        val dateFormat = DateFormatUtil.ANDFHEM_DATE_FORMAT
        view.selectedDate.text = dateFormat.print(today)
        view.changeDateButton.setOnClickListener { _ ->
            val lastDate = dateFormat.parseLocalDate(view.selectedDate.text.toString())
            DatePickerDialog(context, { _, year, month, day ->
                view.selectedDate.text = dateFormat.print(LocalDate(year, month + 1, day))
                update(false)
            }, lastDate.year, lastDate.monthOfYear - 1, lastDate.dayOfMonth).show()
        }
        return view
    }

    abstract fun getAdapter(): ADAPTER

    abstract fun doUpdateView(localDate: LocalDate, view: View)

    abstract fun getRecyclerViewFrom(view: View): RecyclerView

    override fun update(refresh: Boolean) {
        val myView = view
        myView ?: return
        val selectedDate = DateFormatUtil.ANDFHEM_DATE_FORMAT.parseLocalDate(myView.selectedDate.text.toString())

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