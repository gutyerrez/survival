package com.xspacy.survival.teleport.cache

import com.github.benmanes.caffeine.cache.Caffeine
import com.xspacy.survival.teleport.schemas.TeleportRequest
import java.util.UUID
import java.util.concurrent.TimeUnit

object TeleportRequestCache {
    val CACHE = Caffeine.newBuilder()
        .expireAfterWrite(3, TimeUnit.SECONDS)
        .build<UUID, TeleportRequest>()
}