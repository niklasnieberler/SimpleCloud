package eu.thesimplecloud.plugin.server

import com.sun.media.jfxmediaimpl.platform.java.JavaPlatform
import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.plugin.listener.CloudListener
import eu.thesimplecloud.plugin.proxy.CloudProxyPlugin
import eu.thesimplecloud.plugin.startup.CloudPlugin
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.net.URLClassLoader

class CloudSpigotPlugin : JavaPlugin(), ICloudServerPlugin {

    companion object {
        @JvmStatic
        lateinit var instance: CloudSpigotPlugin
    }

    init {
        instance = this
    }

    override fun onLoad() {
        CloudPlugin(this, URLClassLoader(arrayOf(this.file.toURI().toURL())))
    }

    override fun onEnable() {
        CloudPlugin.instance.onEnable()
        CloudAPI.instance.getEventManager().registerListener(CloudPlugin.instance, CloudListener())
    }

    override fun onDisable() {
        CloudPlugin.instance.onDisable()
    }

    override fun shutdown() {
        Bukkit.getServer().shutdown()
    }
}