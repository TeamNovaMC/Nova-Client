package com.radiantbyte.novarelay.client

import kotlin.random.Random

object ClientIdentification {

    fun generateGUID(): Long {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextLong(0, 0xFFFFFF)
        return (timestamp shl 24) or random
    }
}