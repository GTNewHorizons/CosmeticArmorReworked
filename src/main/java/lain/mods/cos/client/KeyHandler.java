package lain.mods.cos.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.network.packet.PacketOpenCosArmorInventory;

public class KeyHandler {

    public final KeyBinding keyOpenCosArmorInventory = new KeyBinding(
        "cos.key.openCosArmorInventory",
        Keyboard.KEY_NONE,
        "key.categories.inventory");

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(keyOpenCosArmorInventory);
    }

    @SubscribeEvent
    public void handleEvent(InputEvent.KeyInputEvent event) {
        if (keyOpenCosArmorInventory.isPressed()) {
            CosmeticArmorReworked.network.sendToServer(new PacketOpenCosArmorInventory());
        }
    }
}
