package com.xspacy.survival.store.commands

import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.teleport.schemas.TeleportRequest
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StoreCommand : CustomCommand("loja") {
    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            throw IllegalArgumentException("Only players can execute this command")
        }

        commandSender as Player

        val world = Bukkit.getWorld("world")

        TeleportRequest(
            commandSender,
            Location(world, -26.5, 75.0, -12.5, 90F, 1F)
        ).start()

        commandSender.spigot().sendMessage(ComponentBuilder("Teletransportando para à loja.").color(ChatColor.GREEN).build())
    }
}