package eu.thesimplecloud.module.permission.service.spigot.placeholder

import eu.thesimplecloud.module.permission.PermissionPool
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

/**
 * Created by MrManHD
 * Class create at 25.06.2023 13:38
 */

class SimpleCloudPermissionExpansion : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "simplecloud-permission"
    }

    override fun getAuthor(): String {
        return "SimpleCloud"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String {
        if (player == null)
            return ""
        val permissionPlayer = PermissionPool.instance.getPermissionPlayerManager()
            .getPermissionPlayer(player.uniqueId).get()
        return when (params.uppercase()) {
            "RANK" -> permissionPlayer.getHighestPermissionGroup().getName()
            else -> ""
        }
    }
}