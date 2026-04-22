package lain.mods.cos.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.InventoryManager;
import lain.mods.cos.inventory.InventoryCosArmor;

public class InventoryManagerClient extends InventoryManager {

    private final PlayerRenderHandler renderHandler = new PlayerRenderHandler();
    private boolean isRenderActive;
    private final Map<UUID, InventoryCosArmor> cacheClient = new HashMap<>();

    @Override
    public void init(FMLPreInitializationEvent event) {
        super.init(event);
        this.toggleRenderer();
        FMLCommonHandler.instance()
            .bus()
            .register(CosmeticArmorReworked.keyHandler = new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
    }

    @Override
    public InventoryCosArmor getCosArmorInventoryClient(UUID uuid) {
        return cacheClient.computeIfAbsent(uuid, id -> new InventoryCosArmor());
    }

    @SubscribeEvent
    public void handleEvent(ClientDisconnectionFromServerEvent event) {
        if (!this.isRenderActive) {
            this.toggleRenderer();
        }
        cacheClient.clear();
    }

    public boolean isRenderActive() {
        return this.isRenderActive;
    }

    public void toggleRenderer() {
        this.isRenderActive = !this.isRenderActive;
        if (this.isRenderActive) {
            MinecraftForge.EVENT_BUS.register(this.renderHandler);
        } else {
            MinecraftForge.EVENT_BUS.unregister(this.renderHandler);
        }
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
