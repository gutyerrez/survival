package com.xspacy.survival.economy.commands

import com.github.benmanes.caffeine.cache.Caffeine
import com.xspacy.core.commands.CustomCommand
import com.xspacy.survival.economy.schemas.UserBalance
import com.xspacy.survival.homes.schemas.UsersTable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.core.minus
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.exposed.v1.jdbc.upsertReturning
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoneyCommand : CustomCommand("money") {
    private val TOP_LIST = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .buildAsync<Unit, List<ResultRow>> {
            transaction {
                UserBalance.innerJoin(UsersTable, { UserBalance.userId }, { UsersTable.id })
                    .select(UserBalance.balance, UsersTable.username)
                    .orderBy(UserBalance.balance, SortOrder.DESC)
                    .limit(10)
                    .toList()
            }
        }

    val numberFormat = run {
        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"))

        numberFormat.maximumFractionDigits = 2

        numberFormat
    }


    override fun onCommand(commandSender: CommandSender, args: Array<out String>) {
        if (!isPlayer(commandSender)) {
            throw IllegalArgumentException("Only players can execute this command")
        }

        commandSender as Player

        when (args.size) {
            1 -> {
                when (args[0]) {
                    "top" -> {
                        val componentBuilder = ComponentBuilder()
                            .append("\n")
                            .append("Rank com os usuários mais ricos")
                            .color(ChatColor.DARK_GREEN)
                            .append(" ")
                            .append("(atualizado a cada 5 minutos)")
                            .color(ChatColor.GRAY)
                            .append("\n\n")

                        TOP_LIST.get(Unit).thenAccept { topList ->
                            topList.forEachIndexed { index, row ->
                                componentBuilder.append(" - ")
                                    .color(ChatColor.DARK_GRAY)
                                    .append("${index + 1}º ${row[UsersTable.username]} ")
                                    .color(ChatColor.WHITE)
                                    .append("(${numberFormat.format(row[UserBalance.balance])} coins)")
                                    .color(ChatColor.GRAY)
                                    .append("\n")
                            }

                            commandSender.spigot().sendMessage(componentBuilder.build())
                        }
                        return

                    }
                    else -> {
                        val user = transaction {
                            UsersTable.select(
                                UsersTable.id,
                                UsersTable.username
                            ).where {
                                UsersTable.username eq args[0]
                            }.singleOrNull()
                        }

                        if (user == null) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Este usuário não existe.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        val userBalance = transaction {
                            UserBalance.select(
                                UserBalance.balance
                            ).where {
                                UserBalance.userId eq user[UsersTable.id]
                            }.singleOrNull()
                        }

                        commandSender.spigot().sendMessage(
                            ComponentBuilder("Carteira de ")
                                .color(ChatColor.GREEN)
                                .append(user[UsersTable.username])
                                .color(ChatColor.WHITE)
                                .append(": ")
                                .color(ChatColor.GREEN)
                                .append(numberFormat.format(userBalance?.get(UserBalance.balance) ?: 0))
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                }
            }
            3 -> {
                val username = args[1]

                val user = transaction {
                    UsersTable.select(
                        UsersTable.id,
                        UsersTable.username
                    ).where {
                        UsersTable.username eq username
                    }.singleOrNull()
                }

                if (user == null) {
                    return commandSender.spigot().sendMessage(
                        ComponentBuilder("Este usuário não existe.")
                            .color(ChatColor.RED)
                            .build()
                    )
                }

                val userBalance = transaction {
                    UserBalance.select(
                        UserBalance.balance
                    ).where {
                        UserBalance.userId eq user[UsersTable.id]
                    }.singleOrNull()
                }

                val value = args[2].toDouble()

                if (value.isNaN() || value < 0) {
                    return commandSender.spigot().sendMessage(
                        ComponentBuilder("Você inseriu um valor inválido.")
                            .color(ChatColor.RED)
                            .build()
                    )
                }

                when (args[0]) {
                    "pay" -> {
                        val ownBalance = transaction {
                            UserBalance.select(
                                UserBalance.balance
                            ).where {
                                UserBalance.userId eq commandSender.uniqueId
                            }.singleOrNull()
                        }

                        if ((ownBalance?.get(UserBalance.balance) ?: 0.0) < value) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Você não possui saldo suficiente.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        if (user[UsersTable.id] == commandSender.uniqueId) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Você não pode enviar dinheiro para si mesmo.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        transaction {
                            UserBalance.update({ UserBalance.userId eq commandSender.uniqueId }) {
                                it[UserBalance.balance] = UserBalance.balance.minus(value)
                            }

                            UserBalance.upsert(onUpdate = {
                                it[UserBalance.balance] = UserBalance.balance.plus(value)
                            }) {
                                it[UserBalance.userId] = user[UsersTable.id]
                                it[UserBalance.balance] = value
                            }
                        }

                        Bukkit.getPlayer(user[UsersTable.id])?.spigot()?.sendMessage(
                            ComponentBuilder("Você recebeu ")
                                .color(ChatColor.GREEN)
                                .append("\"${numberFormat.format(value)}\"")
                                .color(ChatColor.WHITE)
                                .append(" de ")
                                .color(ChatColor.GREEN)
                                .append("\"${commandSender.name}\"")
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )

                        commandSender.spigot().sendMessage(
                            ComponentBuilder("Você enviou ")
                                .color(ChatColor.GREEN)
                                .append("\"${numberFormat.format(value)}\"")
                                .color(ChatColor.WHITE)
                                .append(" para ")
                                .color(ChatColor.GREEN)
                                .append("\"${user[UsersTable.username]}\"")
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                    "give" -> {
                        if (!commandSender.hasPermission("survival.economy.admin")) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Você não possui permissão suficiente.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        val newUserBalance = transaction {
                            UserBalance.upsertReturning(onUpdate = {
                                it[UserBalance.balance] = UserBalance.balance.plus(value)
                            }) {
                                it[UserBalance.userId] = user[UsersTable.id]
                                it[UserBalance.balance] = value
                            }.single()
                        }

                        commandSender.spigot().sendMessage(
                            ComponentBuilder("O saldo de ")
                                .color(ChatColor.GREEN)
                                .append("\"${user[UsersTable.username]}\"")
                                .color(ChatColor.WHITE)
                                .append(" foi atualizado para ")
                                .color(ChatColor.GREEN)
                                .append("\"${numberFormat.format(newUserBalance[UserBalance.balance])}\"")
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                    "remove" -> {
                        if (!commandSender.hasPermission("survival.economy.admin")) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Você não possui permissão suficiente.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        if ((userBalance?.get(UserBalance.balance) ?: 0.0) < value) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Este usuário não possui saldo suficiente.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        val newUserBalance = transaction {
                            UserBalance.upsertReturning(onUpdate = {
                                it[UserBalance.balance] = UserBalance.balance.minus(value)
                            }) {
                                it[UserBalance.userId] = user[UsersTable.id]
                                it[UserBalance.balance] = -value
                            }.single()
                        }

                        commandSender.spigot().sendMessage(
                            ComponentBuilder("O saldo de ")
                                .color(ChatColor.GREEN)
                                .append("\"${user[UsersTable.username]}\"")
                                .color(ChatColor.WHITE)
                                .append(" foi atualizado para ")
                                .color(ChatColor.GREEN)
                                .append("\"${numberFormat.format(newUserBalance[UserBalance.balance])}\"")
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                    "set" -> {
                        if (!commandSender.hasPermission("survival.economy.admin")) {
                            return commandSender.spigot().sendMessage(
                                ComponentBuilder("Você não possui permissão suficiente.")
                                    .color(ChatColor.RED)
                                    .build()
                            )
                        }

                        transaction {
                            UserBalance.upsert {
                                it[UserBalance.userId] = user[UsersTable.id]
                                it[UserBalance.balance] = value
                            }
                        }

                        commandSender.spigot().sendMessage(
                            ComponentBuilder("O saldo de ")
                                .color(ChatColor.GREEN)
                                .append("\"${user[UsersTable.username]}\"")
                                .color(ChatColor.WHITE)
                                .append(" foi atualizado para ")
                                .color(ChatColor.GREEN)
                                .append("\"${numberFormat.format(value)}\"")
                                .color(ChatColor.WHITE)
                                .append(".")
                                .color(ChatColor.GREEN)
                                .build()
                        )
                    }
                }
            }
            else -> {
                val userBalance = transaction {
                    UserBalance.select(
                        UserBalance.balance
                    ).where {
                        UserBalance.userId eq commandSender.uniqueId
                    }.singleOrNull()
                }

                commandSender.spigot().sendMessage(ComponentBuilder("Sua carteira: ").color(ChatColor.GREEN).append(numberFormat.format(userBalance?.get(UserBalance.balance) ?: 0)).color(ChatColor.WHITE).build())
            }
        }
    }
}