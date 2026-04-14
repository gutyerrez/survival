package com.xspacy.survival.chat.commands

import com.xspacy.core.commands.CustomCommand
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class GlobalCommand : CustomCommand("g") {
    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            return
        }

        if (args.isEmpty()) {
            return
        }

        val message = ComponentBuilder("[G]").color(ChatColor.GRAY).append(" ").append(commandSender.name).color(ChatColor.GRAY).append(":").color(ChatColor.GRAY).append(" ").append(args.joinToString(" ")).color(ChatColor.GRAY).build()

        Bukkit.getOnlinePlayers().forEach { player ->
            player.spigot().sendMessage(message)
        }
    }
}