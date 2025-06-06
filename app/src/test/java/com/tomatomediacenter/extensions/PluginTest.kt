package com.tomatomediacenter.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class PluginTest {

    // Mock implementation for testing
    class MockPlugin(
        private val name: String,
        private val version: String
    ) : Plugin {
        var loaded = false
        var unloaded = false

        override fun onLoad() {
            loaded = true
        }

        override fun onUnload() {
            unloaded = true
        }

        override fun getName(): String = name
        override fun getVersion(): String = version
    }

    @Test
    fun pluginLifecycle() {
        val plugin = MockPlugin("TestPlugin", "1.0")
        assertEquals("TestPlugin", plugin.getName())
        assertEquals("1.0", plugin.getVersion())

        plugin.onLoad()
        assertEquals(true, plugin.loaded)

        plugin.onUnload()
        assertEquals(true, plugin.unloaded)
    }
}
