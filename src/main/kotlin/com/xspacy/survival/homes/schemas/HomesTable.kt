package com.xspacy.survival.homes.schemas

import com.xspacy.core.CorePlugin
import org.bukkit.Location
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object HomesTable : Table("homes") {
    val id = integer("id").autoIncrement()
    val userId = javaUUID("user_id")
    val name = varchar("name", 255)
    val location = varchar("location", 255)
    val public = bool("public").default(false)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

fun String.asBukkitLocation(): Location {
    return CorePlugin.GSON_BUILDER.fromJson(this, Location::class.java)
}