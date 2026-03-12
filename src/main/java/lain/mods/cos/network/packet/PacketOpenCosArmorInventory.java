package lain.mods.cos.network.packet;

import net.minecraft.entity.player.EntityPlayerMP;

import io.netty.buffer.ByteBuf;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.network.NetworkPacket;

public class PacketOpenCosArmorInventory extends NetworkPacket {

    @Override
    public void handlePacketClient() {}

    @Override
    public void handlePacketServer(EntityPlayerMP player) {
        player.openGui(
            CosmeticArmorReworked.instance,
            1,
            player.worldObj,
            (int) player.posX,
            (int) player.posY,
            (int) player.posZ);
    }

    @Override
    public void readFromBuffer(ByteBuf buf) {}

    @Override
    public void writeToBuffer(ByteBuf buf) {}

}
