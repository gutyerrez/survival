package com.xspacy.survival.homes.schemas

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object UsersTable : Table("users") {
    val id = javaUUID("id")
    val username = varchar("username", 255)

    override val primaryKey = PrimaryKey(id)
}