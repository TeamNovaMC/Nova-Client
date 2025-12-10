package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreChange;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreRemoval;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreUpdate;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataStorePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataStoreSerializer_v898 implements BedrockPacketSerializer<ClientboundDataStorePacket> {

    public static final ClientboundDataStoreSerializer_v898 INSTANCE = new ClientboundDataStoreSerializer_v898();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataStorePacket packet) {
        helper.writeArray(buffer, packet.getUpdates(), (buf, action) -> {
            VarInts.writeUnsignedInt(buffer, action.getType());

            switch (action.getType()) {
                case 0:
                    helper.writeString(buffer, ((DataStoreUpdate) action).getDataStoreName());
                    helper.writeString(buffer, ((DataStoreUpdate) action).getProperty());
                    helper.writeString(buffer, ((DataStoreUpdate) action).getPath());

                    Object value = ((DataStoreUpdate) action).getData();

                    int type = value instanceof Double ? 0 : value instanceof Boolean ? 1 : value instanceof String ? 2 : -1;

                    VarInts.writeUnsignedInt(buffer, type);

                    switch (type) {
                        case 0:
                            buffer.writeDoubleLE((double) value);
                            break;
                        case 1:
                            buffer.writeBoolean((boolean) value);
                            break;
                        case 2:
                            helper.writeString(buffer, (String) value);
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type");
                    }

                    buffer.writeIntLE(((DataStoreUpdate) action).getUpdateCount());
                    break;
                case 1:
                    helper.writeString(buffer, ((DataStoreChange) action).getDataStoreName());
                    helper.writeString(buffer, ((DataStoreChange) action).getProperty());
                    buffer.writeIntLE(((DataStoreChange) action).getUpdateCount());

                    value = ((DataStoreChange) action).getNewValue();

                    type = value instanceof Double ? 0 : value instanceof Boolean ? 1 : value instanceof String ? 2 : -1;

                    VarInts.writeUnsignedInt(buffer, type);

                    switch (type) {
                        case 0:
                            buffer.writeDoubleLE((double) value);
                            break;
                        case 1:
                            buffer.writeBoolean((boolean) value);
                            break;
                        case 2:
                            helper.writeString(buffer, (String) value);
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type");
                    }
                    break;
                case 2:
                    helper.writeString(buf, ((DataStoreRemoval) action).getDataStoreName());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown data store action");
            }
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataStorePacket packet) {
        helper.readArray(buffer, packet.getUpdates(), (buf -> {
            switch (VarInts.readUnsignedInt(buffer)) {
                case 0:
                    DataStoreUpdate update = new DataStoreUpdate();
                    update.setDataStoreName(helper.readString(buffer));
                    update.setProperty(helper.readString(buffer));
                    update.setPath(helper.readString(buffer));

                    int type = VarInts.readUnsignedInt(buffer);

                    switch (type) {
                        case 0:
                            update.setData(buffer.readDoubleLE());
                            break;
                        case 1:
                            update.setData(buffer.readBoolean());
                            break;
                        case 2:
                            update.setData(helper.readString(buffer));
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type: " + type);
                    }

                    update.setUpdateCount((int) buffer.readUnsignedIntLE());
                    return update;
                case 1:
                    DataStoreChange change = new DataStoreChange();
                    change.setDataStoreName(helper.readString(buf));
                    change.setProperty(helper.readString(buffer));
                    change.setUpdateCount((int) buffer.readUnsignedIntLE());

                    type = VarInts.readUnsignedInt(buffer);

                    switch (type) {
                        case 0:
                            change.setNewValue(buffer.readDoubleLE());
                            break;
                        case 1:
                            change.setNewValue(buffer.readBoolean());
                            break;
                        case 2:
                            change.setNewValue(helper.readString(buffer));
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type: " + type);
                    }
                    return change;
                case 2:
                    DataStoreRemoval removal = new DataStoreRemoval();
                    removal.setDataStoreName(helper.readString(buf));
                    return removal;
                default:
                    throw new UnsupportedOperationException("Unknown data store action");
            }
        }));
    }
}
