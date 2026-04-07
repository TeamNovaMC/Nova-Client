package com.radiantbyte.novarelay.util

import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.config.ServerConfig
import java.net.InetAddress
import java.util.regex.Pattern

object ServerCompatUtils {

    private val PROTECTED_SERVER_PATTERNS = listOf(
        Pattern.compile(".*\\.aternos\\.me$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.org$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.net$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*aternos.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.nethergames\\.org$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*nethergames.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.cubecraft\\.net$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*cubecraft.*", Pattern.CASE_INSENSITIVE)
    )

    private val KNOWN_PROTECTED_IPS = setOf(
        "116.202.224.146",
        "135.181.42.192",
        "168.119.61.4",
        "95.217.163.246"
    )

    fun isProtectedServer(address: NovaAddress): Boolean =
        isProtectedHostname(address.hostName) || isProtectedIP(address.hostName)

    fun isProtectedHostname(hostname: String): Boolean =
        PROTECTED_SERVER_PATTERNS.any { it.matcher(hostname).matches() }

    fun isProtectedIP(hostname: String): Boolean {
        return try {
            val ip = InetAddress.getByName(hostname).hostAddress
            KNOWN_PROTECTED_IPS.contains(ip) || isInProtectedIPRange(ip)
        } catch (_: Exception) {
            false
        }
    }

    private fun isInProtectedIPRange(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return try {
            val first = parts[0].toInt()
            val second = parts[1].toInt()
            when (first) {
                116 -> second in 202..203
                135 -> second in 181..181
                168 -> second in 119..119
                95  -> second in 217..217
                else -> false
            }
        } catch (_: NumberFormatException) {
            false
        }
    }

    fun getRecommendedConfig(address: NovaAddress): ServerConfig {
        if (!isProtectedServer(address)) return ServerConfig.FAST
        val hostname = address.hostName.lowercase()
        return when {
            hostname.contains("nethergames") -> ServerConfig.AGGRESSIVE
            hostname.contains("cubecraft")   -> ServerConfig.AGGRESSIVE
            hostname.matches(Regex(".*\\d+\\.aternos\\.me")) -> ServerConfig.DEFAULT
            hostname.contains("aternos.me") && !hostname.matches(Regex(".*\\d+\\.aternos\\.me")) -> ServerConfig.FAST
            else -> ServerConfig.AGGRESSIVE
        }
    }
}