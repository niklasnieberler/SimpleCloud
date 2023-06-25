package eu.thesimplecloud.plugin.server.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

/**
 * Created by MrManHD
 * Class create at 25.06.2023 13:53
 */

class SimpleCloudPluginExpansion : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "simplecloud-plugin"
    }

    override fun getAuthor(): String {
        return "SimpleCloud"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String {
        return ""
    }
}