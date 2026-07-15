package lain.mods.cos.mui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import serverutils.client.EnumPlacement;
import serverutils.client.EnumSidebarLocation;
import serverutils.client.ServerUtilitiesClientConfig;
import serverutils.client.gui.GuiSidebar;
import serverutils.client.gui.SidebarButton;
import serverutils.client.gui.SidebarButtonGroup;
import serverutils.client.gui.SidebarButtonManager;
import serverutils.lib.client.ClientUtils;
import serverutils.lib.icon.Color4I;

@SideOnly(Side.CLIENT)
final class ServerUtilitiesIntegration {

    static void addSidebar(ModularPanel panel) {
        EnumSidebarLocation location = ServerUtilitiesClientConfig.sidebar_buttons;
        if (location == EnumSidebarLocation.DISABLED) return;

        boolean aboveInventory = location.above();
        EnumPlacement placement = aboveInventory ? EnumPlacement.HORIZONTAL
            : ServerUtilitiesClientConfig.sidebar_placement;
        int buttonX = 0;
        int buttonY = 0;
        int max = placement.getMaxInRow();

        for (SidebarButtonGroup group : SidebarButtonManager.INSTANCE.groups) {
            boolean addedAny = false;
            for (SidebarButton button : group.getButtons()) {
                if (!button.isActuallyVisible()) continue;
                addButton(panel, button, location, aboveInventory, buttonX, buttonY);
                switch (placement) {
                    case HORIZONTAL -> buttonY++;
                    case VERTICAL -> buttonX++;
                    case GROUPED -> {
                        buttonX++;
                        addedAny = true;
                    }
                }

                if (placement != EnumPlacement.GROUPED) {
                    if (buttonY >= max) {
                        buttonY = 0;
                        buttonX--;
                    } else if (buttonX >= max) {
                        buttonX = 0;
                        buttonY++;
                    }
                }
            }
            if (addedAny) {
                buttonX = 0;
                buttonY++;
            }
        }
    }

    private static void addButton(ModularPanel panel, SidebarButton sidebarButton, EnumSidebarLocation location,
        boolean aboveInventory, int buttonX, int buttonY) {
        int dragX = location.isLocked() ? 0 : GuiSidebar.dragOffsetX;
        int dragY = location.isLocked() ? 0 : GuiSidebar.dragOffsetY;
        int x = aboveInventory ? -22 - buttonY * 17 : -18 + dragX - buttonY * 17;
        int y = aboveInventory ? -18 + buttonX * 17 : 8 + dragY + buttonX * 17;
        boolean relativeToScreen = location == EnumSidebarLocation.TOP_LEFT;
        if (relativeToScreen) {
            x = 1 + buttonX * 17;
            y = 1 + buttonY * 17;
        }

        IDrawable icon = (context, drawX, drawY, width, height, widgetTheme) -> {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            try {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1F, 1F, 1F, 1F);
                sidebarButton.getIcon()
                    .draw(drawX, drawY, width, height);
                if (sidebarButton.getCustomTextHandler() != null) {
                    String text = sidebarButton.getCustomTextHandler()
                        .get();
                    if (text != null && !text.isEmpty()) {
                        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
                        int textWidth = font.getStringWidth(text);
                        Color4I.LIGHT_RED.draw(drawX + width - textWidth, drawY - 1, textWidth + 1, 9);
                        font.drawString(text, drawX + width - textWidth + 1, drawY, 0xFFFFFFFF);
                    }
                }
            } finally {
                GL11.glPopAttrib();
                GL11.glColor4f(1F, 1F, 1F, 1F);
            }
        };
        IDrawable hover = (context, drawX, drawY, width, height, widgetTheme) -> Color4I.WHITE.withAlpha(33)
            .draw(drawX, drawY, width, height);

        ButtonWidget<?> widget = new ButtonWidget<>().background(icon)
            .hoverBackground(IDrawable.of(icon, hover))
            .addTooltipLine(IKey.lang(sidebarButton.getLangKey()))
            .onMousePressed(mouseButton -> {
                if (mouseButton != 0) return false;
                sidebarButton.onClicked(GuiScreen.isShiftKeyDown());
                return true;
            })
            .pos(x, y)
            .size(16)
            .name(
                "server_utilities_" + sidebarButton.id.toString()
                    .replace(':', '_'));
        if (relativeToScreen) widget.relativeToScreen();

        List<String> extraTooltip = new ArrayList<>();
        if (sidebarButton.getTooltipHandler() != null) {
            sidebarButton.getTooltipHandler()
                .accept(extraTooltip);
        }
        if (sidebarButton.isDisabled()) {
            extraTooltip.add(EnumChatFormatting.RED + ClientUtils.getDisabledTip());
        }
        for (String line : extraTooltip) {
            widget.addTooltipLine(line);
        }
        panel.child(widget);
    }

    private ServerUtilitiesIntegration() {}
}
