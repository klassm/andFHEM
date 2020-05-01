package com.google.android.material.navigation

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener

class NavigationViewWithMultipleListeners : NavigationView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setNavigationItemSelectedListener(newListener: OnNavigationItemSelectedListener?) {
        val existingListener = listener
        if (existingListener == null || newListener == null) {
            super.setNavigationItemSelectedListener(newListener)
            return
        }

        val wrappedListener = OnNavigationItemSelectedListener { item ->
            newListener.onNavigationItemSelected(item) || existingListener.onNavigationItemSelected(item)
        }

        super.setNavigationItemSelectedListener(wrappedListener)
    }
}