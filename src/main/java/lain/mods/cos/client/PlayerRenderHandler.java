package lain.mods.cos.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.inventory.InventoryCosArmor;

public class PlayerRenderHandler {

    private final Map<EntityPlayer, ItemStack[]> realArmorsCache = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void handleCanceledEvent(RenderHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        restorePlayersRealArmor(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void handleCanceledEvent(RenderPlayerEvent.Pre event) {
        if (event.isCanceled()) {
            restorePlayersRealArmor(event.entityPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void handleEvent(RenderHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        restorePlayersRealArmor(player);
        swapForCosmeticArmor(player);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void handleEvent(RenderPlayerEvent.Post event) {
        restorePlayersRealArmor(event.entityPlayer);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void handleEvent(RenderPlayerEvent.Pre event) {
        restorePlayersRealArmor(event.entityPlayer);
        swapForCosmeticArmor(event.entityPlayer);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) {
            // this shouldn't be needed since the cache
            // gets emptied every render tick
            this.realArmorsCache.clear();
        }
    }

    private void swapForCosmeticArmor(EntityPlayer player) {
        ItemStack[] armor = player.inventory.armorInventory;
        realArmorsCache.put(player, Arrays.copyOf(armor, armor.length));

        final InventoryCosArmor invCosArmor = CosmeticArmorReworked.invMan
            .getCosArmorInventoryClient(player.getUniqueID());
        final ItemStack[] cosArmor = invCosArmor.getInventory();

        if (cosArmor != null) {
            for (int i = 0; i < cosArmor.length; i++) {
                if (invCosArmor.isSkinArmor(i)) {
                    armor[i] = null;
                } else if (cosArmor[i] != null) {
                    armor[i] = cosArmor[i];
                }
            }
        }
    }

    private void restorePlayersRealArmor(EntityPlayer player) {
        ItemStack[] cachedArmor = realArmorsCache.remove(player);
        if (cachedArmor != null) {
            ItemStack[] armor = player.inventory.armorInventory;
            if (armor != null && cachedArmor.length == armor.length) {
                System.arraycopy(cachedArmor, 0, armor, 0, cachedArmor.length);
            }
        }
    }

}
