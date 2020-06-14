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

package li.klass.fhem.fragments.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.room_detail_page.view.*
import kotlinx.coroutines.*
import li.klass.fhem.R
import li.klass.fhem.activities.core.Updateable
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.*
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.service.ResendLastFailedCommandService
import li.klass.fhem.util.navigation.loadFragmentInto
import li.klass.fhem.widget.SwipeRefreshLayout
import org.jetbrains.anko.support.v4.onRefresh
import java.io.Serializable
import javax.inject.Inject

abstract class BaseFragment : Fragment(), Updateable, Serializable, SwipeRefreshLayout.ChildScrollDelegate {

    @Transient
    private var broadcastReceiver: UIBroadcastReceiver? = null

    @Inject
    lateinit var resendLastFailedCommandService: ResendLastFailedCommandService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retryButton = view.findViewById<Button?>(R.id.retry)
        val context = activity ?: return
        retryButton?.setOnClickListener {
            hideConnectionError()

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    resendLastFailedCommandService.resend(context)
                }
            }
        }

        view.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)?.let {
            it.onRefresh {
                updateAsync(true)
            }
            it.setChildScrollDelegate {
                canChildScrollUp()
            }
        }

        updateAsync(false)
    }

    override fun canChildScrollUp(): Boolean {
        if (!mayPullToRefresh()) {
            return true
        }
        return view?.canScrollVertically(-1) ?: false
    }

    override suspend fun update(refresh: Boolean) {
        hideConnectionError()
        updateInternal(canUpdateRemotely && refresh)
    }

    open val canUpdateRemotely = true

    fun updateAsync(refresh: Boolean = true) = GlobalScope.launch(Dispatchers.Main) {
        update(refresh)
    }

    private fun hideConnectionError() {
        val view = view ?: return

        val errorLayout = view.findViewById<RelativeLayout?>(R.id.errorLayout) ?: return
        errorLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        val myActivity = activity ?: return
        if (broadcastReceiver == null) {
            broadcastReceiver = UIBroadcastReceiver(myActivity)
        }
        broadcastReceiver!!.attach()

        view?.findViewById<FrameLayout>(R.id.navigation)?.navigation?.let {
            val navFragment = navigationFragment
            when (navFragment == null) {
                true -> it.visibility = View.GONE
                false -> loadFragmentInto(R.id.navigation, navFragment)
            }
            navFragment?.view?.invalidate()
        }

        val title = getTitle(myActivity)
        title?.let { setTitle(it) }
        updateAsync(false)
    }

    open val navigationFragment: Fragment? = null

    fun setTitle(title: String?) {
        activity?.title = title ?: ""
    }

    override fun onPause() {
        super.onPause()
        if (broadcastReceiver != null) {
            broadcastReceiver!!.detach()
            broadcastReceiver = null
        }
    }

    fun invalidate() {
        val view = view
        if (view != null) {
            view.invalidate()
            view.requestLayout()
        }
    }

    protected fun hideEmptyView() {
        val view = view
        if (view != null) {
            val emptyView = view.findViewById<LinearLayout?>(R.id.emptyView) ?: return
            emptyView.visibility = View.GONE
        }
    }

    protected fun showEmptyView() {
        val emptyView = view?.findViewById<LinearLayout?>(R.id.emptyView) ?: return
        emptyView.visibility = View.VISIBLE
    }

    protected fun fillEmptyView(view: LinearLayout, text: Int, container: ViewGroup) {
        if (text != 0) {
            val emptyView = LayoutInflater.from(activity).inflate(R.layout.empty_view, container, false)!!
            val emptyText = emptyView.findViewById(R.id.emptyText) as TextView
            emptyText.setText(text)

            view.addView(emptyView)
        }
    }

    private fun showConnectionError(content: String) {
        val view = view ?: return

        val errorLayout = view.findViewById<RelativeLayout?>(R.id.errorLayout) ?: return
        val activity = activity ?: return

        errorLayout.setOnLongClickListener {
            ErrorHolder.sendLastErrorAsMail(activity)

            true
        }

        errorLayout.visibility = View.VISIBLE

        val errorView = view.findViewById<TextView>(R.id.errorView)
        errorView.text = content
    }

    protected open fun mayUpdateFromBroadcast(): Boolean = true

    protected open fun mayPullToRefresh(): Boolean = true

    private fun updateInternal(doRefresh: Boolean) {
        if (mayUpdateFromBroadcast()) {
            updateAsync(doRefresh)
        }
    }

    inner class UIBroadcastReceiver(private val activity: androidx.fragment.app.FragmentActivity) : BroadcastReceiver() {

        private val intentFilter: IntentFilter = IntentFilter().apply {
            addAction(CONNECTION_ERROR)
            addAction(CONNECTION_ERROR_HIDE)
            addAction(DO_UPDATE)
            addAction(DISMISS_EXECUTING_DIALOG)
        }

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            activity.runOnUiThread(Runnable {
                Log.v(UIBroadcastReceiver::class.java.name, "received action " + action!!)

                try {
                    when (action) {
                        CONNECTION_ERROR -> {
                            val content = if (intent.hasExtra(STRING_ID)) {
                                context.getString(intent.getIntExtra(STRING_ID, -1))
                            } else {
                                intent.getStringExtra(STRING)
                            }
                            showConnectionError(content)
                        }
                        CONNECTION_ERROR_HIDE -> {
                            hideConnectionError()
                        }
                        DO_UPDATE -> {
                            updateAsync(intent.getBooleanExtra(DO_REFRESH, false))
                        }
                        DISMISS_EXECUTING_DIALOG -> {
                            view?.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
                                    ?.let {
                                        it.isRefreshing = false
                                    }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(UIBroadcastReceiver::class.java.name, "error occurred", e)
                }
            })
        }

        fun attach() {
            activity.registerReceiver(this, intentFilter)
        }

        fun detach() {
            try {
                activity.unregisterReceiver(this)
            } catch (e: IllegalArgumentException) {
                Log.e(UIBroadcastReceiver::class.java.name, "error while detaching", e)
            }

        }
    }

    open fun getTitle(context: Context): String? = null
}
