package lain.mods.cos.client;

import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.io.Charsets;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.InventoryManager;
import lain.mods.cos.inventory.InventoryCosArmor;

public class InventoryManagerClient extends InventoryManager {

    LoadingCache<UUID, InventoryCosArmor> cacheClient = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {

            @Override
            public InventoryCosArmor load(UUID owner) {
                return new InventoryCosArmor();
            }

        });

    Map<UUID, UUID> map = Maps.newHashMap();

    @Override
    public void init(FMLPreInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new PlayerRenderHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(CosmeticArmorReworked.keyHandler = new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
    }

    @Override
    public InventoryCosArmor getCosArmorInventoryClient(UUID uuid) {
        if (map.isEmpty()) {
            Minecraft mc = FMLClientHandler.instance()
                .getClient();
            if (mc.thePlayer != null) map.put(
                UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + mc.thePlayer.getGameProfile()
                        .getName()).getBytes(Charsets.UTF_8)),
                mc.thePlayer.getUniqueID());
        }
        if (map.containsKey(uuid)) uuid = map.get(uuid);
        return cacheClient.getUnchecked(uuid);
    }

    @SubscribeEvent
    public void handleEvent(ClientDisconnectionFromServerEvent event) {
        PlayerRenderHandler.HideCosArmor = false;
        cacheClient.invalidateAll();
        map.clear();
    }

}
