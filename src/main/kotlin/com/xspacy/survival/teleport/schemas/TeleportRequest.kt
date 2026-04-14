package com.xspacy.survival.teleport.schemas

import com.xspacy.survival.SurvivalSpigotPlugin
import com.xspacy.survival.teleport.cache.TeleportRequestCache
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

data class TeleportRequest(
    val player: Player,
    val to: Location,
    var canTeleport: Boolean = true
) {
    fun start() {
        TeleportRequestCache.CACHE.put(player.uniqueId, this)

        Bukkit.getScheduler().scheduleSyncDelayedTask(SurvivalSpigotPlugin.instance, {
            Bukkit.broadcastMessage("checking if can teleport")

            if (!canTeleport) {
                Bukkit.broadcastMessage("can't teleport")

                return@scheduleSyncDelayedTask
            }

            Bukkit.broadcastMessage("can teleport, teleporting...")

            player.teleport(to)
        }, 3_000)
    }
}
