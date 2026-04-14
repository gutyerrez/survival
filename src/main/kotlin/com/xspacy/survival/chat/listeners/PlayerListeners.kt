package com.xspacy.survival.chat.listeners

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class PlayerListeners : Listener {
    @EventHandler
    fun on(event: AsyncPlayerChatEvent) {
        val player = event.player

        val message = ComponentBuilder("[L]").color(ChatColor.YELLOW).append(" ").append(player.name).color(ChatColor.GRAY).append(":").color(ChatColor.YELLOW).append(" ").append(event.message).color(ChatColor.YELLOW).build()

        player.getNearbyEntities(25.0, 25.0, 25.0).filterIsInstance<Player>().forEach { player ->
            player.spigot().sendMessage(message)
        }
    }
}