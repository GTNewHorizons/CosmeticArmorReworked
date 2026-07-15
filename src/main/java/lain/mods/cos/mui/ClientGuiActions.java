package lain.mods.cos.mui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.network.packet.PacketOpenNormalInventory;

@SideOnly(Side.CLIENT)
final class ClientGuiActions {

    private static final Gui GUI_DRAWER = new Gui();

    private ClientGuiActions() {}

    static void openNormalInventory() {
        Minecraft minecraft = Minecraft.getMinecraft();
        CosmeticArmorReworked.network.sendToServer(new PacketOpenNormalInventory());
        minecraft.displayGuiScreen(new GuiInventory(minecraft.thePlayer));
    }

    static boolean isRendererEnabled() {
        return CosmeticArmorReworked.getClient()
            .isRenderActive();
    }

    static void setRendererEnabled(boolean enabled) {
        if (isRendererEnabled() != enabled) {
            CosmeticArmorReworked.getClient()
                .toggleRenderer();
        }
    }

    static IDrawable armorSlotIcon(ModularSlot slot, int armorType) {
        return (context, x, y, width, height, widgetTheme) -> {
            if (slot.getStack() != null) return;
            IIcon icon = ItemArmor.func_94602_b(armorType);
            if (icon == null) return;

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
            try {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(TextureMap.locationItemsTexture);
                GL11.glColor4f(1F, 1F, 1F, 1F);
                GUI_DRAWER.drawTexturedModelRectFromIcon(x + 1, y + 1, icon, 16, 16);
            } finally {
                GL11.glPopAttrib();
                GL11.glColor4f(1F, 1F, 1F, 1F);
            }
        };
    }

    static void drawPlayerPreview(ModularScreen screen, EntityPlayer player) {
        int oldMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        float oldPlayerViewY = RenderManager.instance.playerViewY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glClearDepth(1.0D);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            int guiLeft = screen.getMainPanel()
                .getArea().x;
            int guiTop = screen.getMainPanel()
                .getArea().y;
            int centerX = guiLeft + 51;
            int centerY = guiTop + 75;
            drawPlayerModel(
                centerX,
                centerY,
                30,
                centerX - screen.getContext()
                    .getAbsMouseX(),
                centerY - 50
                    - screen.getContext()
                        .getAbsMouseY(),
                player);
        } finally {
            RenderManager.instance.playerViewY = oldPlayerViewY;
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(oldMatrixMode);
            GL11.glPopAttrib();
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1F, 1F, 1F, 1F);
        }
    }

    private static void drawPlayerModel(int x, int y, int scale, float mouseX, float mouseY, EntityLivingBase entity) {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 50F);
        GL11.glScalef(-scale, scale, scale);
        GL11.glRotatef(180F, 0F, 0F, 1F);

        float oldRenderYawOffset = entity.renderYawOffset;
        float oldRotationYaw = entity.rotationYaw;
        float oldRotationPitch = entity.rotationPitch;
        float oldPrevRotationYawHead = entity.prevRotationYawHead;
        float oldRotationYawHead = entity.rotationYawHead;

        GL11.glRotatef(135F, 0F, 1F, 0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0F, 1F, 0F);
        GL11.glRotatef(-(float) Math.atan(mouseY / 40F) * 20F, 1F, 0F, 0F);

        entity.renderYawOffset = (float) Math.atan(mouseX / 40F) * 20F;
        entity.rotationYaw = (float) Math.atan(mouseX / 40F) * 40F;
        entity.rotationPitch = -(float) Math.atan(mouseY / 40F) * 20F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;

        GL11.glTranslatef(0F, entity.yOffset, 0F);
        RenderManager.instance.playerViewY = 180F;
        RenderManager.instance.renderEntityWithPosYaw(entity, 0D, 0D, 0D, 0F, 1F);

        entity.renderYawOffset = oldRenderYawOffset;
        entity.rotationYaw = oldRotationYaw;
        entity.rotationPitch = oldRotationPitch;
        entity.prevRotationYawHead = oldPrevRotationYawHead;
        entity.rotationYawHead = oldRotationYawHead;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

}
