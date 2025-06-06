package com.tomatomediacenter.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =getViewBinding()
        setContentView(binding.root)
        setupViews()
        observeViewModel()
    }

    abstract fun getViewBinding(): VB
    abstract fun setupViews()
    abstract fun observeViewModel()
}
