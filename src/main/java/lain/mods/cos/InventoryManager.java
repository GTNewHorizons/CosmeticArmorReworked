package lain.mods.cos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import lain.mods.cos.inventory.InventoryCosArmor;
import lain.mods.cos.network.packet.PacketSyncCosArmor;

/// Keeps each player's cosmetic armor in its own file, cached while the player is online.
///
/// A player whose file fails its Postea transform fails to load with the transform's [ReportedException], and
/// that player's file is not saved until a later read of it succeeds.
@SuppressWarnings("UnstableApiUsage")
public class InventoryManager {

    private final Set<UUID> failedLoads = ConcurrentHashMap.newKeySet();

    LoadingCache<UUID, InventoryCosArmor> cache = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {

            @Override
            public InventoryCosArmor load(UUID owner) {
                InventoryCosArmor inv = new InventoryCosArmor();

                try {
                    read(owner, inv);
                } catch (IOException e) {
                    System.err.println("Error loading CosmeticArmor data file: " + e.getMessage());
                    e.printStackTrace();
                    inv = new InventoryCosArmor();
                }

                inv.markDirty();

                return inv;
            }

        });

    public void init(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    void read(UUID uuid, InventoryCosArmor inv) throws IOException {
        failedLoads.remove(uuid);
        NBTTagCompound compound;
        try (FileInputStream in = new FileInputStream(getDataFile(uuid))) {
            compound = CompressedStreamTools.readCompressed(in);
        } catch (FileNotFoundException ignored) {
            return;
        }
        try {
            PosteaCompat.transform("cosarmor:" + uuid, compound);
        } catch (ReportedException e) {
            failedLoads.add(uuid);
            throw e;
        }
        inv.readFromNBT(compound);
        inv.setSavedStamps(PosteaCompat.savedStamps(compound));
    }

    void forceSave(UUID uuid) throws IOException {
        if (failedLoads.contains(uuid)) return;
        InventoryCosArmor inv = cache.getIfPresent(uuid);
        if (inv == null) return;
        NBTTagCompound compound = new NBTTagCompound();
        inv.writeToNBT(compound);
        PosteaCompat.stamp(compound, inv.getSavedStamps());
        try (FileOutputStream out = new FileOutputStream(getDataFile(uuid))) {
            CompressedStreamTools.writeCompressed(compound, out);
        }
    }

    public InventoryCosArmor getCosArmorInventory(UUID uuid) {
        try {
            return cache.getUnchecked(uuid);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof ReportedException) throw (ReportedException) e.getCause();
            throw e;
        }
    }

    public InventoryCosArmor getCosArmorInventoryClient(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    File getDataFile(UUID uuid) {
        return new File(new File(getSavesDirectory(), "playerdata"), uuid + ".cosarmor");
    }

    File getSavesDirectory() {
        MinecraftServer server = FMLCommonHandler.instance()
            .getMinecraftServerInstance();
        if (FMLCommonHandler.instance()
            .getSide()
            .isClient())
            return new File(
                server.getFile("saves"),
                server.worldServerForDimension(0)
                    .getSaveHandler()
                    .getWorldDirectoryName());
        return server.getFile(server.getFolderName());
    }

    @SubscribeEvent
    public void handleEvent(PlayerDropsEvent event) {
        if (event.entityPlayer instanceof EntityPlayerMP && !event.entityPlayer.worldObj.isRemote
            && !event.entityPlayer.worldObj.getGameRules()
                .getGameRuleBooleanValue("keepInventory")) {
            InventoryCosArmor inv = getCosArmorInventory(event.entityPlayer.getUniqueID());
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null) {
                    EntityItem ent = new EntityItem(
                        event.entityPlayer.worldObj,
                        event.entityPlayer.posX,
                        event.entityPlayer.posY + event.entityPlayer.getEyeHeight(),
                        event.entityPlayer.posZ,
                        stack.copy());
                    ent.delayBeforeCanPickup = 40;
                    float f1 = event.entityPlayer.worldObj.rand.nextFloat() * 0.5F;
                    float f2 = event.entityPlayer.worldObj.rand.nextFloat() * (float) Math.PI * 2.0F;
                    ent.motionX = (double) (-MathHelper.sin(f2) * f1);
                    ent.motionZ = (double) (MathHelper.cos(f2) * f1);
                    ent.motionY = 0.20000000298023224D;
                    event.drops.add(ent);
                    inv.setInventorySlotContents(i, null);
                    inv.markDirty();
                }
            }
        }

    }

    @SubscribeEvent
    public void handleEvent(PlayerEvent.LoadFromFile event) {
        UUID uuid = UUID.fromString(event.playerUUID);
        boolean wasCached = cache.getIfPresent(uuid) != null;
        InventoryCosArmor inv = getCosArmorInventory(uuid);

        if (wasCached) {
            try {
                read(uuid, inv);
            } catch (IOException e) {
                System.err.println("Error loading CosmeticArmor data file: " + e.getMessage());
                e.printStackTrace();
                cache.refresh(uuid);
                inv = getCosArmorInventory(uuid);
            }
        }

        inv.markDirty();
    }

    @SubscribeEvent
    public void handleEvent(PlayerEvent.SaveToFile event) {
        UUID uuid = UUID.fromString(event.playerUUID);
        try {
            forceSave(uuid);
        } catch (IOException e) {
            System.err.println("Error saving CosmeticArmor data file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void handleEvent(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            InventoryCosArmor inv = getCosArmorInventory(event.player.getUniqueID());
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                CosmeticArmorReworked.network.sendToAll(new PacketSyncCosArmor(event.player, i));
            }
            inv.markClean();

            for (EntityPlayerMP other : (List<EntityPlayerMP>) FMLCommonHandler.instance()
                .getMinecraftServerInstance()
                .getConfigurationManager().playerEntityList) {
                if (other == event.player) continue;
                inv = getCosArmorInventory(other.getUniqueID());
                for (int i = 0; i < inv.getSizeInventory(); i++) CosmeticArmorReworked.network
                    .sendTo(new PacketSyncCosArmor(other, i), (EntityPlayerMP) event.player);
            }
        }
    }

    @SubscribeEvent
    public void handleEvent(PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            UUID uuid = event.player.getUniqueID();
            try {
                forceSave(uuid);
            } catch (IOException e) {
                System.err.println("Error saving CosmeticArmor data file: " + e.getMessage());
                e.printStackTrace();
            }
            cache.invalidate(uuid);
        }
    }

    @SubscribeEvent
    public void handleEvent(PlayerTickEvent event) {
        if (event.phase == Phase.START) {
            if (event.player instanceof EntityPlayerMP) {
                InventoryCosArmor inv = getCosArmorInventory(event.player.getUniqueID());
                if (inv.isDirty()) {
                    for (int i = 0; i < inv.getSizeInventory(); i++)
                        CosmeticArmorReworked.network.sendToAll(new PacketSyncCosArmor(event.player, i));
                    inv.markClean();
                }
            }
        }
    }

    void onServerStarting() {
        failedLoads.clear();
        cache.invalidateAll();
    }

    void onServerStopping() {
        for (UUID uuid : cache.asMap()
            .keySet()) {
            try {
                forceSave(uuid);
            } catch (IOException e) {
                System.err.println("Error saving CosmeticArmor data file: " + e.getMessage());
                e.printStackTrace();
            }
        }
        cache.invalidateAll();
    }

    public boolean isClient() {
        return false;
    }
}
