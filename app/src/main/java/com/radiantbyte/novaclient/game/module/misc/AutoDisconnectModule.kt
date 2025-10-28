package com.radiantbyte.novaclient.game.module.misc

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoDisconnectModule : Module("auto_disconnect", ModuleCategory.Misc) {

    private val delaySeconds by intValue("delay", 0, 0..60)
    private val showMessage by boolValue("show_message", true)
    private val customMessage by stringValue("message", "Disconnected", listOf())

    override fun onEnabled() {
        super.onEnabled()
        
        if (!isSessionCreated) {
            isEnabled = false
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (delaySeconds > 0) {
                if (showMessage) {
                    session.displayClientMessage("§l§b[AutoDisconnect] §r§7Disconnecting in §c$delaySeconds §7seconds...")
                }
                delay(delaySeconds * 1000L)
            }

            if (isEnabled && isSessionCreated) {
                if (showMessage) {
                    session.displayClientMessage("§l§b[AutoDisconnect] §r§c$customMessage")
                }
                
                try {
                    session.novaRelaySession.client?.disconnect()
                    session.novaRelaySession.server.disconnect()
                } catch (e: Exception) {
                }
                
                isEnabled = false
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
    }
}
