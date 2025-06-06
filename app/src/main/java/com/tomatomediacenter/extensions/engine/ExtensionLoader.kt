package com.tomatomediacenter.extensions.engine

import com.tomatomediacenter.extensions.Plugin
import java.io.File
import java.net.URLClassLoader

class ExtensionLoader(private val pluginDirectory: File) {

    private val plugins = mutableListOf<Plugin>()

    fun loadPlugins() {
        if (!pluginDirectory.exists() || !pluginDirectory.isDirectory) {
            println("Plugin directory does not exist or is not a directory.")
            return
        }

        val pluginFiles = pluginDirectory.listFiles { file ->
            file.isFile && file.name.endsWith(".jar")
        } ?: return

        for (file in pluginFiles) {
            try {
                val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), this.javaClass.classLoader)
                // This is a simplified way to load plugins.
                // A real implementation would scan the JAR for classes implementing the Plugin interface.
                // For now, we'll assume a convention for the main plugin class name.
                // Example: com.example.MyPlugin
                // This part needs a proper implementation for discovering Plugin classes within the JAR.
                // For this placeholder, we'll skip actual class loading from JAR.
                // val mainClass = classLoader.loadClass("com.example.MyPlugin") // Placeholder
                // val plugin = mainClass.getDeclaredConstructor().newInstance() as Plugin
                // plugins.add(plugin)
                // plugin.onLoad()
                println("Found plugin file: ${file.name} (Actual loading not implemented in this step)")
            } catch (e: Exception) {
                println("Error loading plugin from ${file.name}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun unloadPlugins() {
        plugins.forEach { plugin ->
            try {
                plugin.onUnload()
            } catch (e: Exception) {
                println("Error unloading plugin ${plugin.getName()}: ${e.message}")
            }
        }
        plugins.clear()
    }

    fun getPlugins(): List<Plugin> {
        return plugins.toList()
    }
}
