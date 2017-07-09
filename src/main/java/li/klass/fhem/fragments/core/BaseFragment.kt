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
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import li.klass.fhem.AndFHEMApplication
import li.klass.fhem.R
import li.klass.fhem.activities.core.Updateable
import li.klass.fhem.constants.Actions
import li.klass.fhem.constants.Actions.*
import li.klass.fhem.constants.BundleExtraKeys.STRING
import li.klass.fhem.constants.BundleExtraKeys.STRING_ID
import li.klass.fhem.dagger.ApplicationComponent
import li.klass.fhem.error.ErrorHolder
import li.klass.fhem.service.intent.DeviceIntentService
import li.klass.fhem.widget.SwipeRefreshLayout
import java.io.Serializable

abstract class BaseFragment : Fragment(), Updateable, Serializable, SwipeRefreshLayout.ChildScrollDelegate {

    var isNavigation = false
    @Transient private var broadcastReceiver: UIBroadcastReceiver? = null
    @Transient private var contentView: View? = null
    private var backPressCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject((activity.application as AndFHEMApplication).daggerComponent)
    }

    protected abstract fun inject(applicationComponent: ApplicationComponent)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retryButton = view!!.findViewById(R.id.retry) as Button
        retryButton.setOnClickListener {
            hideConnectionError()

            val resendIntent = Intent(RESEND_LAST_FAILED_COMMAND)
            resendIntent.setClass(activity, DeviceIntentService::class.java)
            activity.startService(resendIntent)
        }
    }

    override fun canChildScrollUp(): Boolean {
        if (!mayPullToRefresh()) {
            return true
        }
        return ViewCompat.canScrollVertically(view, -1)
    }

    override fun update(refresh: Boolean) {
        hideConnectionError()
        updateInternal(refresh)
    }

    private fun hideConnectionError() {
        if (isNavigation) return

        val view = view ?: return

        val errorLayout = view.findViewById(R.id.errorLayout) ?: return
        errorLayout.visibility = View.GONE
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return contentView
    }

    override fun onResume() {
        super.onResume()
        if (broadcastReceiver == null) {
            broadcastReceiver = UIBroadcastReceiver(activity)
        }
        broadcastReceiver!!.attach()

        if (contentView != null) {
            contentView!!.clearFocus()
        }
        backPressCalled = false

        update(false)
    }

    override fun onPause() {
        contentView = view
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()

        if (broadcastReceiver != null) {
            broadcastReceiver!!.detach()
            broadcastReceiver = null
        }
    }

    fun onBackPressResult() {
        update(false)
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
            val emptyView = view.findViewById(R.id.emptyView) ?: return
            emptyView.visibility = View.GONE
        }
    }

    protected fun showEmptyView() {
        if (isNavigation || view == null) return

        val emptyView = view!!.findViewById(R.id.emptyView) ?: return
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
        if (isNavigation) return

        val view = view ?: return

        val errorLayout = view.findViewById(R.id.errorLayout) ?: return

        errorLayout.setOnLongClickListener {
            ErrorHolder.sendLastErrorAsMail(activity)

            true
        }

        errorLayout.visibility = View.VISIBLE

        val errorView = view.findViewById(R.id.errorView) as TextView
        errorView.text = content
    }

    protected open fun mayUpdateFromBroadcast(): Boolean {
        return true
    }

    protected open fun mayPullToRefresh(): Boolean {
        return true
    }

    private fun updateInternal(doRefresh: Boolean) {
        if (mayUpdateFromBroadcast()) {
            update(doRefresh)
        }
    }

    inner class UIBroadcastReceiver(private val activity: FragmentActivity) : BroadcastReceiver() {

        private val intentFilter: IntentFilter

        init {

            intentFilter = IntentFilter()
            intentFilter.addAction(TOP_LEVEL_BACK)
            intentFilter.addAction(CONNECTION_ERROR)
            intentFilter.addAction(CONNECTION_ERROR_HIDE)
        }

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            activity.runOnUiThread(Runnable {
                Log.v(UIBroadcastReceiver::class.java.name, "received action " + action!!)

                if (action == null) return@Runnable

                try {
                    if (action == TOP_LEVEL_BACK) {
                        if (!isVisible) return@Runnable
                        if (!backPressCalled) {
                            backPressCalled = true
                            onBackPressResult()
                        }
                    } else if (action == CONNECTION_ERROR) {
                        val content: String
                        if (intent.hasExtra(STRING_ID)) {
                            content = context.getString(intent.getIntExtra(STRING_ID, -1))
                        } else {
                            content = intent.getStringExtra(STRING)
                        }
                        showConnectionError(content)
                    } else if (action == CONNECTION_ERROR_HIDE) {
                        hideConnectionError()
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

    open fun getTitle(context: Context): CharSequence? {
        return null
    }

    protected fun back() {
        val intent = Intent(Actions.BACK)
        activity.sendBroadcast(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        update(false)
    }
}
