package com.tomatomediacenter.extensions

interface Plugin {
    fun onLoad()
    fun onUnload()
    fun getName(): String
    fun getVersion(): String
}
