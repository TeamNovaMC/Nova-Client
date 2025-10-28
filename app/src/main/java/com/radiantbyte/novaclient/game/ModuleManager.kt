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
import com.radiantbyte.novaclient.game.module.misc.ArrayListModule
import com.radiantbyte.novaclient.game.module.misc.BaritoneModule
import com.radiantbyte.novaclient.game.module.misc.CommandHandlerModule
import com.radiantbyte.novaclient.game.module.misc.CoordinatesModule
import com.radiantbyte.novaclient.game.module.misc.DesyncModule
import com.radiantbyte.novaclient.game.module.misc.FakeDeathModule
import com.radiantbyte.novaclient.game.module.misc.FakeXPModule
import com.radiantbyte.novaclient.game.module.misc.KeyStrokesModule
import com.radiantbyte.novaclient.game.module.misc.MinerModule
import com.radiantbyte.novaclient.game.module.misc.NoChatModule
import com.radiantbyte.novaclient.game.module.motion.NoClipModule
import com.radiantbyte.novaclient.game.module.misc.PieChartModule
import com.radiantbyte.novaclient.game.module.misc.PositionLoggerModule
import com.radiantbyte.novaclient.game.module.misc.ReplayModule
import com.radiantbyte.novaclient.game.module.misc.WaterMarkModule
import com.radiantbyte.novaclient.game.module.misc.ChestStealerModule
import com.radiantbyte.novaclient.game.module.world.AntiDebuffModule
import com.radiantbyte.novaclient.game.module.world.EffectsModule
import com.radiantbyte.novaclient.game.module.world.ParticlesModule
import com.radiantbyte.novaclient.game.module.world.TimeShiftModule
import com.radiantbyte.novaclient.game.module.world.WeatherControllerModule
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
import com.radiantbyte.novaclient.game.module.visual.CrosshairModule
import com.radiantbyte.novaclient.game.module.visual.ESPModule
import com.radiantbyte.novaclient.game.module.visual.FreeCameraModule
import com.radiantbyte.novaclient.game.module.visual.FullbrightModule
import com.radiantbyte.novaclient.game.module.visual.MinimapModule
import com.radiantbyte.novaclient.game.module.visual.NetworkInfoModule
import com.radiantbyte.novaclient.game.module.visual.NoHurtCameraModule
import com.radiantbyte.novaclient.game.module.visual.PositionDisplayModule
import com.radiantbyte.novaclient.game.module.visual.SpeedDisplayModule
import com.radiantbyte.novaclient.game.module.visual.WorldStateModule
import com.radiantbyte.novaclient.game.module.visual.ZoomModule
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
            // Combat
            add(KillauraModule())
            add(AntiKnockbackModule())
            add(AntiCrystalModule())
            add(HitAndRunModule())
            add(HitboxModule())
            add(CrystalSmashModule())
            add(TriggerBotModule())
            
            // Motion
            add(UnifiedFlyModule())
            add(FlyModule())
            add(SpeedModule())
            add(AirJumpModule())
            add(NoClipModule())
            add(JetPackModule())
            add(HighJumpModule())
            add(BhopModule())
            add(SprintModule())
            add(AutoWalkModule())
            add(AntiAFKModule())
            add(MotionFlyModule())
            add(SpiderModule())
            
            // Visual
            add(ESPModule())
            add(ZoomModule())
            add(NoHurtCameraModule())
            add(FreeCameraModule())
            add(SpeedDisplayModule())
            add(PositionDisplayModule())
            add(NetworkInfoModule())
            add(WorldStateModule())
            add(MinimapModule())
            add(CrosshairModule())
            add(TargetHudModule())
            add(FullbrightModule())
            
            // World
            add(TimeShiftModule())
            add(WeatherControllerModule())
            add(EffectsModule())
            add(ParticlesModule())
            add(AntiDebuffModule())
            add(ChestStealerModule())
            
            // Misc
            add(DesyncModule())
            add(PositionLoggerModule())
            add(NoChatModule())
            add(CommandHandlerModule())
            add(ReplayModule())
            add(BaritoneModule())
            add(ArrayListModule())
            add(WaterMarkModule())
            add(KeyStrokesModule())
            add(CoordinatesModule())
            add(PieChartModule())
            add(FakeDeathModule())
            add(FakeXPModule())
            add(MinerModule())
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