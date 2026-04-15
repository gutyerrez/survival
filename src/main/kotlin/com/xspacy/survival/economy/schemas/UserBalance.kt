package com.xspacy.survival.economy.schemas

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object UserBalance : Table("user_balances"){
    val userId = javaUUID("user_id")
    val balance = double("balance")

    override val primaryKey = PrimaryKey(userId)
}