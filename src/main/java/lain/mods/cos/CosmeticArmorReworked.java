package lain.mods.cos;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lain.mods.cos.client.InventoryManagerClient;
import lain.mods.cos.client.KeyHandler;
import lain.mods.cos.network.NetworkManager;
import lain.mods.cos.network.packet.PacketOpenCosArmorInventory;
import lain.mods.cos.network.packet.PacketOpenNormalInventory;
import lain.mods.cos.network.packet.PacketSetSkinArmor;
import lain.mods.cos.network.packet.PacketSyncCosArmor;

@Mod(
    modid = CosmeticArmorReworked.MOD_ID,
    name = CosmeticArmorReworked.MOD_NAME,
    acceptedMinecraftVersions = "[1.7.10]",
    useMetadata = true)

public class CosmeticArmorReworked {

    public static final String MOD_ID = "cosmeticarmorreworked";
    public static final String MOD_NAME = "CosmeticArmorReworked";
    public static final String MOD_GROUP = "lain.mods.cos";

    @Mod.Instance("cosmeticarmorreworked")
    public static CosmeticArmorReworked instance;

    @SideOnly(Side.CLIENT)
    public static KeyHandler keyHandler;

    @SidedProxy(
        serverSide = "lain.mods.cos.InventoryManager",
        clientSide = "lain.mods.cos.client.InventoryManagerClient")
    public static InventoryManager invMan;

    public static final NetworkManager network = new NetworkManager("lain|nm|cos");

    public static InventoryManagerClient getClient() {
        if (invMan.isClient()) {
            return ((InventoryManagerClient) invMan);
        }
        throw new IllegalStateException("Client proxy accessed from dedicated server");
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        network.registerPacket(1, PacketSyncCosArmor.class);
        network.registerPacket(2, PacketSetSkinArmor.class);
        network.registerPacket(3, PacketOpenCosArmorInventory.class);
        network.registerPacket(4, PacketOpenNormalInventory.class);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        invMan.init(event);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        invMan.onServerStarting();
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        invMan.onServerStopping();
    }

}
