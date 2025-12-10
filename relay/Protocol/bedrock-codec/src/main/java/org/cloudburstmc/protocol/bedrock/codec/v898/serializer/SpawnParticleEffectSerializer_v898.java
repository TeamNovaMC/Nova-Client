package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v332.serializer.SpawnParticleEffectSerializer_v332;
import org.cloudburstmc.protocol.bedrock.packet.SpawnParticleEffectPacket;

import java.util.Optional;

public class SpawnParticleEffectSerializer_v898 extends SpawnParticleEffectSerializer_v332 {

    public static final SpawnParticleEffectSerializer_v898 INSTANCE = new SpawnParticleEffectSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SpawnParticleEffectPacket packet) {
        super.serialize(buffer, helper, packet);
        helper.writeString(buffer, packet.getMolangVariablesJson().isPresent() ? packet.getMolangVariablesJson().get() : "");
    }

    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SpawnParticleEffectPacket packet) {
        super.deserialize(buffer, helper, packet);
        packet.setMolangVariablesJson(Optional.ofNullable(helper.readString(buffer)));
    }
}
