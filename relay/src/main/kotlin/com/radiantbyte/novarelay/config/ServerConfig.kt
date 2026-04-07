package com.radiantbyte.novarelay.config

data class ServerConfig(
    val maxRetryAttempts: Int = 3,
    val retryDelay: Long = 2000L,
    val connectionTimeout: Long = 15000L,
    val sessionTimeout: Long = 30000L
)
