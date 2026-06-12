package net.minecraft.client.gui;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiScreenTooltipHelper {

    private GuiScreenTooltipHelper() {}

    public static void drawTooltip(GuiScreen screen, List<String> textLines, int mouseX, int mouseY) {
        screen.func_146283_a(textLines, mouseX, mouseY);
    }

    public static List<GuiButton> getButtonList(GuiScreen screen) {
        return screen.buttonList;
    }
}
