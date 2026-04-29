package lain.mods.cos.client;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.network.packet.PacketOpenCosArmorInventory;
import lain.mods.cos.network.packet.PacketOpenNormalInventory;

public class GuiEvents {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.gui instanceof GuiInventory || event.gui instanceof GuiCosArmorInventory) {
            if (event.button.id == 76) {
                if (event.gui instanceof GuiCosArmorInventory) {
                    event.gui.mc.displayGuiScreen(new GuiInventory(event.gui.mc.thePlayer));
                    CosmeticArmorReworked.network.sendToServer(new PacketOpenNormalInventory());
                } else {
                    CosmeticArmorReworked.network.sendToServer(new PacketOpenCosArmorInventory());
                }
            } else if (event.button.id == 77) {
                CosmeticArmorReworked.getClient()
                    .toggleRenderer();
                ((GuiCosArmorToggleButton) event.button).state = CosmeticArmorReworked.getClient()
                    .isRenderActive() ? 0 : 1;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiInventory || event.gui instanceof GuiCosArmorInventory) {
            int xSize = 176;
            int ySize = 166;

            int guiLeft = (event.gui.width - xSize) / 2;
            int guiTop = (event.gui.height - ySize) / 2;

            event.buttonList.add(
                new GuiCosArmorButton(
                    76,
                    guiLeft + 66,
                    guiTop + 67,
                    10,
                    10,
                    event.gui instanceof GuiCosArmorInventory ? "cos.gui.buttonNormal" : "cos.gui.buttonCos"));
            GuiCosArmorToggleButton t = new GuiCosArmorToggleButton(77, guiLeft + 60, guiTop + 72, 5, 5, "");
            t.state = CosmeticArmorReworked.getClient()
                .isRenderActive() ? 0 : 1;
            event.buttonList.add(t);
        }
    }

}
