package com.xspacy.survival.homes.commands

import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.homes.schemas.HomesTable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class PrivateHomeCommand : CustomCommand("privada") {
    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            return
        }

        commandSender as Player

        val homeName = args[0]

        val home = transaction {
            HomesTable.select(
                HomesTable.id,
                HomesTable.public
            ).where {
                (HomesTable.userId eq commandSender.uniqueId) and (HomesTable.name eq homeName)
            }.singleOrNull()
        }

        if (home == null) {
            return commandSender.spigot().sendMessage(ComponentBuilder("Esta home não existe.").color(ChatColor.RED).build())
        }

        if (!home[HomesTable.public]) {
            return commandSender.spigot().sendMessage(ComponentBuilder("Esta home já é privada.").color(ChatColor.RED).build())
        }

        transaction {
            HomesTable.update({ HomesTable.id eq home[HomesTable.id]}) {
                it[HomesTable.public] = false
            }
        }

        return commandSender.spigot().sendMessage(ComponentBuilder("Privada \"${homeName}\" adicionada com sucesso.").color(ChatColor.GREEN).build())
    }
}