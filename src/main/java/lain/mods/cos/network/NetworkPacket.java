package lain.mods.cos.network;

import net.minecraft.entity.player.EntityPlayerMP;

import io.netty.buffer.ByteBuf;

public abstract class NetworkPacket {

    public abstract void handlePacketClient();

    public abstract void handlePacketServer(EntityPlayerMP player);

    public abstract void readFromBuffer(ByteBuf buf);

    public abstract void writeToBuffer(ByteBuf buf);

}
