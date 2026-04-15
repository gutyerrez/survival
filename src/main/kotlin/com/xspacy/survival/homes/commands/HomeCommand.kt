package com.xspacy.survival.homes.commands

import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.homes.schemas.HomesTable
import com.xspacy.survival.homes.schemas.UsersTable
import com.xspacy.survival.homes.schemas.asBukkitLocation
import com.xspacy.survival.teleport.schemas.TeleportRequest
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class HomeCommand : CustomCommand("home") {
    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            return
        }

        commandSender as Player

        when (args.size) {
            1 -> {
                if (args[0].contains(":")) {
                    val ( ownerName, homeName ) = args[0].split(":")

                    val ownerUser = transaction {
                        UsersTable.select(
                            UsersTable.id
                        ).where {
                            UsersTable.username eq ownerName
                        }.singleOrNull()
                    }

                    if (ownerUser == null) {
                        return commandSender.spigot().sendMessage(ComponentBuilder("Este usuário não existe.").color(ChatColor.RED).build())
                    }

                    if (homeName == "*") {
                        val homes = transaction {
                            HomesTable.select(
                                HomesTable.name,
                                HomesTable.public
                            ).where {
                                HomesTable.userId eq ownerUser[UsersTable.id]
                            }.toList()
                        }

                        return commandSender.spigot().sendMessage(buildComponent(homes))
                    }

                    val home = transaction {
                        HomesTable.select(
                            HomesTable.location,
                            HomesTable.public
                        ).where {
                            (HomesTable.userId eq ownerUser[UsersTable.id]) and (HomesTable.name eq homeName)
                        }.singleOrNull()
                    }

                    if (home == null) {
                        return commandSender.spigot().sendMessage(ComponentBuilder("Esta home não existe.").color(ChatColor.RED).build())
                    }

                    if (ownerUser[UsersTable.id] != commandSender.uniqueId && !home[HomesTable.public]) {
                        return commandSender.spigot().sendMessage(ComponentBuilder("Esta não é uma home pública.").color(ChatColor.RED).build())
                    }

                    return this.teleport(commandSender, home)
                }

                val home = transaction {
                    HomesTable.select(
                        HomesTable.name,
                        HomesTable.location
                    ).where {
                        (HomesTable.name eq args[0]) and (HomesTable.userId eq commandSender.uniqueId)
                    }.singleOrNull()
                }

                if (home == null) {
                    return commandSender.spigot().sendMessage(ComponentBuilder("Esta home não existe.").color(ChatColor.RED).build())
                }

                return this.teleport(commandSender, home)
            }
            else -> {
                val homes = transaction {
                    HomesTable.select(
                        HomesTable.name,
                        HomesTable.public
                    ).where {
                        HomesTable.userId eq commandSender.uniqueId
                    }.toList()
                }

                commandSender.spigot().sendMessage(buildComponent(homes, true))
            }
        }
    }

    private fun buildComponent(homes: List<ResultRow>, showPrivateHomes: Boolean = false): BaseComponent {
        val component = ComponentBuilder("Homes públicas (${homes.count { home -> home[HomesTable.public] == true }}): ").color(ChatColor.GOLD)

        homes.filter { home -> home[HomesTable.public] }.forEachIndexed { index, resultRow ->
            val textComponent = TextComponent(resultRow[HomesTable.name])

            textComponent.color = ChatColor.WHITE
            textComponent.clickEvent = ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND,
                "/home ${resultRow[HomesTable.name]}"
            )

            component.append(textComponent)

            if (index + 1 < homes.size) {
                component.append(", ").color(ChatColor.WHITE)
            }
        }

        if (homes.none { home -> home[HomesTable.public] }) {
            component.append("Nenhuma.").color(ChatColor.WHITE)
        }

        if (!showPrivateHomes) {
            return component.build()
        }

        component.append("\n").append("Homes privadas (${homes.count{ home -> home[HomesTable.public] == false}}): ").color(ChatColor.GOLD)

        homes.filter { home -> !home[HomesTable.public] }.forEachIndexed { index, resultRow ->
            val textComponent = TextComponent(resultRow[HomesTable.name])

            textComponent.color = ChatColor.WHITE
            textComponent.clickEvent = ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND,
                "/home ${resultRow[HomesTable.name]}"
            )

            component.append(textComponent)

            if (index + 1 < homes.size) {
                component.append(", ").color(ChatColor.WHITE)
            }
        }

        if (homes.none { home -> !home[HomesTable.public] }) {
            component.append("Nenhuma.").color(ChatColor.WHITE)
        }

        return component.build()
    }

    private fun teleport(commandSender: Player, home: ResultRow) {
        val location = home[HomesTable.location].asBukkitLocation()

        val teleportRequest = TeleportRequest(
            commandSender,
            location
        )

        teleportRequest.start()

        commandSender.spigot().sendMessage(ComponentBuilder("Teletransportando para \"${home[HomesTable.name]}\" em 3 segundos...").color(ChatColor.GREEN).build())
    }
}