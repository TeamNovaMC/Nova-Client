package com.radiantbyte.novaclient.util

import com.radiantbyte.novaclient.game.TranslationManager
import java.util.Locale

inline val String.translatedSelf: String
    get() {
        return TranslationManager.getTranslationMap(Locale.getDefault().language)[this]
            ?: this
    }