package com.xspacy.survival

import com.xspacy.core.CorePlugin
import com.xspacy.core.plugin.CustomPlugin
import com.xspacy.survival.chat.commands.GlobalCommand
import com.xspacy.survival.chat.listeners.PlayerListeners
import com.xspacy.survival.homes.commands.DelHomeCommand
import com.xspacy.survival.homes.commands.HomeCommand
import com.xspacy.survival.homes.commands.PrivateHomeCommand
import com.xspacy.survival.homes.commands.PublicHomeCommand
import com.xspacy.survival.homes.commands.SetHomeCommand
import com.xspacy.survival.homes.schemas.HomesTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils

class SurvivalSpigotPlugin : CustomPlugin() {
    companion object {
        lateinit var instance: CustomPlugin
    }

    override fun onEnable() {
        super.onEnable()

        instance = this

        registerCommand(GlobalCommand())
        registerCommand(HomeCommand())
        registerCommand(SetHomeCommand())
        registerCommand(DelHomeCommand())
        registerCommand(PrivateHomeCommand())
        registerCommand(PublicHomeCommand())
        registerListener(PlayerListeners(), this)

        transaction {
            SchemaUtils.create(HomesTable)

            MigrationUtils.statementsRequiredForDatabaseMigration(
                HomesTable
            )
        }
    }
}