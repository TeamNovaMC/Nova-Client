package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.StartGameSerializer_v924;
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket;

public class StartGameSerializer_v944 extends StartGameSerializer_v924 {

    public static final StartGameSerializer_v944 INSTANCE = new StartGameSerializer_v944();

    @Override
    protected void writeServerJoinInformation(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        buffer.writeBoolean(packet.isHasServerJoinInformation());

        if (packet.isHasServerJoinInformation()) {
            buffer.writeBoolean(false); // TODO
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
        }
    }

    @Override
    protected void readServerJoinInformation(ByteBuf buffer, BedrockCodecHelper helper, StartGamePacket packet) {
        packet.setHasServerJoinInformation(buffer.readBoolean());

        if (packet.isHasServerJoinInformation()) {
            buffer.readBoolean(); // TODO
            buffer.readBoolean();
            buffer.readBoolean();
        }
    }
}
