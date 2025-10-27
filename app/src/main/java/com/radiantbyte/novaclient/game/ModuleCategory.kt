package com.radiantbyte.novaclient.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.radiantbyte.novaclient.R

enum class ModuleCategory(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val displayName: String
) {

    Combat(
        iconResId = R.drawable.swords_24px,
        labelResId = R.string.combat,
        displayName = "Combat"
    ),
    Motion(
        iconResId = R.drawable.sprint_24px,
        labelResId = R.string.motion,
        displayName = "Motion"
    ),
    Visual(
        iconResId = R.drawable.view_in_ar_24px,
        labelResId = R.string.visual,
        displayName = "Visual"
    ),
    Effect(
        iconResId = R.drawable.masked_transitions_24px,
        labelResId = R.string.effect,
        displayName = "Effect"
    ),
    Particle(
        iconResId = R.drawable.particles_24px,
        labelResId = R.string.particle,
        displayName = "Particle"
    ),
    Misc(
        iconResId = R.drawable.toc_24px,
        labelResId = R.string.misc,
        displayName = "Misc"
    ),
}