package com.radiantbyte.novaclient.util

import com.radiantbyte.novarelay.codec.CodecRegistry

object MinecraftUtils {
    val RECOMMENDED_VERSION: String
        get() = "v${CodecRegistry.CURRENT_VERSION}"
}
