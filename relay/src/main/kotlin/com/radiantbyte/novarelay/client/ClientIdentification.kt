package com.radiantbyte.novarelay.client

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlin.random.Random

object ClientIdentification {

    fun generateGUID(): Long {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextLong(0, 0xFFFFFF)
        return (timestamp shl 24) or random
    }

    fun createUnconnectedMagic(): ByteBuf {
        val magic = byteArrayOf(
            0x00.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x00.toByte(),
            0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(), 0xFE.toByte(),
            0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(), 0xFD.toByte(),
            0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte()
        )
        return Unpooled.wrappedBuffer(magic)
    }

}