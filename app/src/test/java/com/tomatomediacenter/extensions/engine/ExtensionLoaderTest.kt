package com.tomatomediacenter.extensions.engine

import com.tomatomediacenter.extensions.Plugin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ExtensionLoaderTest {

    private lateinit var tempPluginDir: File
    private lateinit var extensionLoader: ExtensionLoader

    // Mock Plugin implementation for tests
    class TestPlugin(private val id: String) : Plugin {
        var loaded = false
        var unloaded = false
        override fun onLoad() { loaded = true }
        override fun onUnload() { unloaded = true }
        override fun getName(): String = "TestPlugin-$id"
        override fun getVersion(): String = "1.0.0"
    }

    @Before
    fun setUp() {
        // Create a temporary directory for plugins
        tempPluginDir = File.createTempFile("plugins", null)
        tempPluginDir.delete() // delete the file and create a directory
        tempPluginDir.mkdir()

        extensionLoader = ExtensionLoader(tempPluginDir)
    }

    @After
    fun tearDown() {
        // Clean up the temporary directory
        tempPluginDir.deleteRecursively()
    }

    @Test
    fun loadPlugins_noPluginsFound() {
        extensionLoader.loadPlugins()
        assertTrue(extensionLoader.getPlugins().isEmpty())
    }

    @Test
    fun loadPlugins_findsJarFilesButDoesNotLoadThem() {
        // Create dummy JAR files
        File(tempPluginDir, "plugin1.jar").createNewFile()
        File(tempPluginDir, "plugin2.jar").createNewFile()
        File(tempPluginDir, "not_a_plugin.txt").createNewFile()

        extensionLoader.loadPlugins()
        // Current implementation of loadPlugins only prints found files and doesn't load them.
        // So, the plugins list should be empty.
        // This test will need to be updated when actual JAR loading is implemented.
        assertTrue(extensionLoader.getPlugins().isEmpty())
    }

    // Further tests would require a more sophisticated setup to mock JAR loading
    // and plugin instantiation, which is beyond the current scope of ExtensionLoader.
    // For example, testing unloadPlugins would require plugins to be loaded first.
}
