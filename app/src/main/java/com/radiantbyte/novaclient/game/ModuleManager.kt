package com.radiantbyte.novaclient.game


import android.content.Context
import android.net.Uri
import com.radiantbyte.novaclient.application.AppContext
import com.radiantbyte.novaclient.game.module.combat.AntiCrystalModule
import com.radiantbyte.novaclient.game.module.combat.AntiKnockbackModule
import com.radiantbyte.novaclient.game.module.combat.CrystalSmashModule
import com.radiantbyte.novaclient.game.module.combat.HitAndRunModule
import com.radiantbyte.novaclient.game.module.combat.HitboxModule
import com.radiantbyte.novaclient.game.module.combat.KillauraModule
import com.radiantbyte.novaclient.game.module.combat.TriggerBotModule
import com.radiantbyte.novaclient.game.module.effect.AbsorptionModule
import com.radiantbyte.novaclient.game.module.effect.BadOmenModule
import com.radiantbyte.novaclient.game.module.effect.BlindnessModule
import com.radiantbyte.novaclient.game.module.effect.ConduitPowerModule
import com.radiantbyte.novaclient.game.module.effect.DarknessModule
import com.radiantbyte.novaclient.game.module.effect.FatalPoisonModule
import com.radiantbyte.novaclient.game.module.effect.FireResistanceModule
import com.radiantbyte.novaclient.game.module.effect.HasteModule
import com.radiantbyte.novaclient.game.module.effect.HealthBoostModule
import com.radiantbyte.novaclient.game.module.effect.HungerModule
import com.radiantbyte.novaclient.game.module.effect.InstantDamageModule
import com.radiantbyte.novaclient.game.module.effect.InstantHealthModule
import com.radiantbyte.novaclient.game.module.effect.InvisibilityModule
import com.radiantbyte.novaclient.game.module.effect.JumpBoostModule
import com.radiantbyte.novaclient.game.module.effect.LevitationModule
import com.radiantbyte.novaclient.game.module.effect.MiningFatigueModule
import com.radiantbyte.novaclient.game.module.effect.NauseaModule
import com.radiantbyte.novaclient.game.module.effect.NightVisionModule
import com.radiantbyte.novaclient.game.module.effect.PoisonModule
import com.radiantbyte.novaclient.game.module.effect.PoseidonModule
import com.radiantbyte.novaclient.game.module.effect.RegenerationModule
import com.radiantbyte.novaclient.game.module.effect.ResistanceModule
import com.radiantbyte.novaclient.game.module.effect.SaturationModule
import com.radiantbyte.novaclient.game.module.effect.SlowFallingModule
import com.radiantbyte.novaclient.game.module.effect.StrengthModule
import com.radiantbyte.novaclient.game.module.effect.SwiftnessModule
import com.radiantbyte.novaclient.game.module.effect.VillageHeroModule
import com.radiantbyte.novaclient.game.module.effect.WeaknessModule
import com.radiantbyte.novaclient.game.module.effect.WitherModule
import com.radiantbyte.novaclient.game.module.misc.ArrayListModule
import com.radiantbyte.novaclient.game.module.misc.BaritoneModule
import com.radiantbyte.novaclient.game.module.misc.CommandHandlerModule
import com.radiantbyte.novaclient.game.module.misc.CoordinatesModule
import com.radiantbyte.novaclient.game.module.misc.DesyncModule
import com.radiantbyte.novaclient.game.module.misc.FakeDeathModule
import com.radiantbyte.novaclient.game.module.misc.FakeXPModule
import com.radiantbyte.novaclient.game.module.misc.KeyStrokesModule
import com.radiantbyte.novaclient.game.module.misc.NoChatModule
import com.radiantbyte.novaclient.game.module.motion.NoClipModule
import com.radiantbyte.novaclient.game.module.misc.PieChartModule
import com.radiantbyte.novaclient.game.module.misc.PositionLoggerModule
import com.radiantbyte.novaclient.game.module.misc.ReplayModule
import com.radiantbyte.novaclient.game.module.misc.WaterMarkModule
import com.radiantbyte.novaclient.game.module.visual.TimeShiftModule
import com.radiantbyte.novaclient.game.module.visual.WeatherControllerModule
import com.radiantbyte.novaclient.game.module.motion.AirJumpModule
import com.radiantbyte.novaclient.game.module.motion.AntiAFKModule
import com.radiantbyte.novaclient.game.module.motion.AutoWalkModule
import com.radiantbyte.novaclient.game.module.motion.BhopModule
import com.radiantbyte.novaclient.game.module.motion.FlyModule
import com.radiantbyte.novaclient.game.module.motion.HighJumpModule
import com.radiantbyte.novaclient.game.module.motion.JetPackModule
import com.radiantbyte.novaclient.game.module.motion.MotionFlyModule
import com.radiantbyte.novaclient.game.module.motion.SpeedModule
import com.radiantbyte.novaclient.game.module.motion.SpiderModule
import com.radiantbyte.novaclient.game.module.motion.SprintModule
import com.radiantbyte.novaclient.game.module.motion.UnifiedFlyModule
import com.radiantbyte.novaclient.game.module.particle.BreezeWindExplosionParticleModule
import com.radiantbyte.novaclient.game.module.particle.BubbleParticleModule
import com.radiantbyte.novaclient.game.module.particle.DustParticleModule
import com.radiantbyte.novaclient.game.module.particle.ExplosionParticleModule
import com.radiantbyte.novaclient.game.module.particle.EyeOfEnderDeathParticleModule
import com.radiantbyte.novaclient.game.module.particle.FizzParticleModule
import com.radiantbyte.novaclient.game.module.particle.HeartParticleModule
import com.radiantbyte.novaclient.game.module.visual.CrosshairModule
import com.radiantbyte.novaclient.game.module.visual.ESPModule
import com.radiantbyte.novaclient.game.module.visual.FreeCameraModule
import com.radiantbyte.novaclient.game.module.visual.MinimapModule
import com.radiantbyte.novaclient.game.module.visual.NetworkInfoModule
import com.radiantbyte.novaclient.game.module.visual.NoHurtCameraModule
import com.radiantbyte.novaclient.game.module.visual.PositionDisplayModule
import com.radiantbyte.novaclient.game.module.visual.SpeedDisplayModule
import com.radiantbyte.novaclient.game.module.visual.WorldStateModule
import com.radiantbyte.novaclient.game.module.visual.ZoomModule
import com.radiantbyte.novaclient.game.module.misc.ChestStealerModule
import com.radiantbyte.novaclient.game.module.visual.TargetHudModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File

object ModuleManager {

    private val _modules: MutableList<Module> = ArrayList()

    val modules: List<Module> = _modules

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        with(_modules) {
            add(UnifiedFlyModule())
            add(FlyModule())
            add(ESPModule())
            add(ZoomModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(NightVisionModule())
            add(HasteModule())
            add(SpeedModule())
            add(JetPackModule())
            add(LevitationModule())
            add(HighJumpModule())
            add(SlowFallingModule())
            add(PoseidonModule())
            add(AntiKnockbackModule())
            add(RegenerationModule())
            add(BhopModule())
            add(SprintModule())
            add(NoHurtCameraModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(DesyncModule())
            add(PositionLoggerModule())
            add(MotionFlyModule())
            add(FreeCameraModule())
            add(KillauraModule())
            add(NauseaModule())
            add(HealthBoostModule())
            add(JumpBoostModule())
            add(ResistanceModule())
            add(FireResistanceModule())
            add(SwiftnessModule())
            add(InstantHealthModule())
            add(StrengthModule())
            add(InstantDamageModule())
            add(InvisibilityModule())
            add(SaturationModule())
            add(AbsorptionModule())
            add(BlindnessModule())
            add(AntiCrystalModule())
            add(HungerModule())
            add(WeaknessModule())
            add(PoisonModule())
            add(WitherModule())
            add(FatalPoisonModule())
            add(ConduitPowerModule())
            add(BadOmenModule())
            add(VillageHeroModule())
            add(DarknessModule())
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(FakeDeathModule())
            add(ExplosionParticleModule())
            add(BubbleParticleModule())
            add(HeartParticleModule())
            add(FakeXPModule())
            add(DustParticleModule())
            add(EyeOfEnderDeathParticleModule())
            add(FizzParticleModule())
            add(BreezeWindExplosionParticleModule())
            add(HitAndRunModule())
            add(HitboxModule())
            add(CrystalSmashModule())
            add(TriggerBotModule())
            add(NoChatModule())
            add(SpeedDisplayModule())
            add(PositionDisplayModule())
            add(CommandHandlerModule())
            add(NetworkInfoModule())
            add(MiningFatigueModule())
            add(WorldStateModule())
            add(ReplayModule())
            add(BaritoneModule())
            add(ArrayListModule())
            add(MinimapModule())
            add(WaterMarkModule())
            add(SpiderModule())
            add(KeyStrokesModule())
            add(CrosshairModule())
            add(CoordinatesModule())
            add(PieChartModule())
            add(ChestStealerModule())
            add(TargetHudModule())
        }
    }

    fun saveConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }

        config.writeText(json.encodeToString(jsonObject))
    }

    fun loadConfig() {
        val configsDir = AppContext.instance.filesDir.resolve("configs")
        configsDir.mkdirs()

        val config = configsDir.resolve("UserConfig.json")
        if (!config.exists()) {
            return
        }

        val jsonString = config.readText()
        if (jsonString.isEmpty()) {
            return
        }

        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val modules = jsonObject["modules"]!!.jsonObject
        _modules.forEach { module ->
            (modules[module.name] as? JsonObject)?.let {
                module.fromJson(it)
            }
        }
    }

    fun exportConfig(): String {
        val jsonObject = buildJsonObject {
            put("modules", buildJsonObject {
                _modules.forEach {
                    if (it.private) {
                        return@forEach
                    }
                    put(it.name, it.toJson())
                }
            })
        }
        return json.encodeToString(jsonObject)
    }

    fun importConfig(configStr: String) {
        try {
            val jsonObject = json.parseToJsonElement(configStr).jsonObject
            val modules = jsonObject["modules"]?.jsonObject ?: return

            _modules.forEach { module ->
                modules[module.name]?.let {
                    if (it is JsonObject) {
                        module.fromJson(it)
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid config format")
        }
    }

    fun exportConfigToFile(context: Context, fileName: String): Boolean {
        return try {
            val configsDir = context.getExternalFilesDir("configs")
            configsDir?.mkdirs()

            val configFile = File(configsDir, "$fileName.json")
            configFile.writeText(exportConfig())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importConfigFromFile(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val configStr = input.bufferedReader().readText()
                importConfig(configStr)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}