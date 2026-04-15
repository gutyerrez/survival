package com.xspacy.survival.homes.commands

import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.homes.schemas.HomesTable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DelHomeCommand : CustomCommand("delhome") {
    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            throw IllegalArgumentException("Only players can execute this command")
        }

        commandSender as Player

        if (args.isEmpty()) {
            return commandSender.spigot().sendMessage(ComponentBuilder("Utilize /delhome <nome>.").color(ChatColor.RED).build())
        }

        val homeName = args[0]

        val home = transaction {
            HomesTable.select(
                HomesTable.id
            ).where {
                (HomesTable.name eq homeName) and (HomesTable.userId eq commandSender.uniqueId)
            }.singleOrNull()
        }

        if (home == null) {
            return commandSender.spigot().sendMessage(ComponentBuilder("Esta home não existe.").color(ChatColor.RED).build())
        }

        transaction {
            HomesTable.deleteWhere { HomesTable.id eq home[HomesTable.id] }
        }

        return commandSender.spigot().sendMessage(ComponentBuilder("A home \"${homeName}\" foi deletada com sucesso.").color(ChatColor.GREEN).build())
    }
}