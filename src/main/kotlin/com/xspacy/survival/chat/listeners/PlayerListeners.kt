package com.xspacy.survival.chat.listeners

import com.xspacy.survival.SurvivalSpigotPlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class PlayerListeners : Listener {
    @EventHandler
    fun on(event: AsyncPlayerChatEvent) {
        val player = event.player

        event.isCancelled = true

        val message = ComponentBuilder("[L]").color(ChatColor.YELLOW).append(" ").append(player.name).color(ChatColor.GRAY).append(":").color(ChatColor.YELLOW).append(" ").append(event.message).color(ChatColor.YELLOW).build()

        Bukkit.getScheduler().runTask(SurvivalSpigotPlugin.instance) { _ ->
            player.getNearbyEntities(25.0, 25.0, 25.0).filterIsInstance<Player>().forEach { nearby ->
                nearby.spigot().sendMessage(message)
            }

            player.spigot().sendMessage(message)
        }
    }
}