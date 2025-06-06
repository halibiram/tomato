package com.tomatomediacenter.extensions.ui

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class ExtensionManagementActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "Extension Management UI (Placeholder)"
        setContentView(textView)
    }
}
