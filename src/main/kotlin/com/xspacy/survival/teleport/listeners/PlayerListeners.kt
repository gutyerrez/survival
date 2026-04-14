package com.xspacy.survival.teleport.listeners

import com.xspacy.survival.teleport.cache.TeleportRequestCache
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PlayerListeners : Listener {
    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        val entity = event.entity

        if (entity !is Player) {
            return
        }

        val teleportRequest = TeleportRequestCache.CACHE.getIfPresent(entity.uniqueId) ?: return

        teleportRequest.canTeleport = false
    }
}