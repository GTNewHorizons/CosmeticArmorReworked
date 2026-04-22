package lain.mods.cos.client;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.inventory.InventoryCosArmor;

@SuppressWarnings("UnstableApiUsage")
public class PlayerRenderHandler {

    private static final ItemStack[] EMPTY = new ItemStack[0];

    private final LoadingCache<EntityPlayer, ItemStack[]> cache = CacheBuilder.newBuilder()
        .expireAfterAccess(60, TimeUnit.SECONDS)
        .build(new CacheLoader<>() {

            @Override
            public ItemStack[] load(EntityPlayer owner) {
                return EMPTY;
            }

        });

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void handleCanceledEvent(RenderHandEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        restorePlayersRealArmor(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void handleCanceledEvent(RenderPlayerEvent.Pre event) {
        if (!event.isCanceled()) return;

        restorePlayersRealArmor(event.entityPlayer);
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

    private void swapForCosmeticArmor(EntityPlayer player) {
        ItemStack[] armor = player.inventory.armorInventory;
        cache.put(player, Arrays.copyOf(armor, armor.length));

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
        ItemStack[] cachedArmor = cache.getUnchecked(player);
        if (cachedArmor != EMPTY) {
            ItemStack[] armor = player.inventory.armorInventory;
            if (armor != null && cachedArmor.length == armor.length) {
                System.arraycopy(cachedArmor, 0, armor, 0, cachedArmor.length);
                cache.put(player, EMPTY);
            }
        }
    }

}
