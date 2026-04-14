package com.xspacy.survival.homes.commands

import com.xspacy.core.CorePlugin
import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.homes.schemas.HomesTable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class SetHomeCommand : CustomCommand("sethome") {
    private val MIN_HOME_NAME_LENGTH = 3
    private val MAX_HOME_NAME_LENGHT = 32

    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            throw IllegalArgumentException("Only players can execute this command")
        }

        commandSender as Player

        if (args.isEmpty()) {
            return commandSender.sendMessage(usage)
        }

        val homeName = args[0]

        if (homeName.length !in MIN_HOME_NAME_LENGTH..MAX_HOME_NAME_LENGHT) {
            return commandSender.spigot().sendMessage(ComponentBuilder("O nome da home deve ter entre ${MIN_HOME_NAME_LENGTH} a ${MAX_HOME_NAME_LENGHT} caracteres.").color(ChatColor.RED).build())
        }

        val home = transaction {
            HomesTable.select(
                HomesTable.id
            ).where {
                (HomesTable.userId eq commandSender.uniqueId) and (HomesTable.name eq homeName)
            }.singleOrNull()
        }

        if (home != null) {
            transaction {
                HomesTable.update({ HomesTable.id eq home[HomesTable.id]}) {
                    it[HomesTable.location] = CorePlugin.GSON_BUILDER.toJson(commandSender.location)
                }
            }

            return commandSender.spigot().sendMessage(ComponentBuilder("Home '${homeName}' criada com sucesso.").color(ChatColor.GREEN).build())
        }

        val homes = transaction {
            HomesTable.select(
                HomesTable.id.count()
            ).where {
                HomesTable.userId eq commandSender.uniqueId
            }.single()[HomesTable.id.count()]
        }

        if (homes.toInt() >= 8) {
            return commandSender.spigot().sendMessage(ComponentBuilder("Você atingiu o limite de 8 homes definidas.").color(ChatColor.RED).build())
        }

        transaction {
            HomesTable.insert {
                it[HomesTable.userId] = commandSender.uniqueId
                it[HomesTable.name] = homeName
                it[HomesTable.location] = CorePlugin.GSON_BUILDER.toJson(commandSender.location)
            }
        }

        return commandSender.spigot().sendMessage(ComponentBuilder("Home '${homeName}' criada com sucesso.").color(ChatColor.GREEN).build())
    }
}