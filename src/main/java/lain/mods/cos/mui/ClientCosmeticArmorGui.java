package lain.mods.cos.mui;

import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lain.mods.cos.CosmeticArmorReworked;

@SideOnly(Side.CLIENT)
public final class ClientCosmeticArmorGui extends CosmeticArmorGui {

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
        return new ModularScreen(CosmeticArmorReworked.MOD_ID, mainPanel) {

            @Override
            public void drawScreen() {
                super.drawScreen();
                ClientGuiActions.drawPlayerPreview(this, data.getPlayer());
            }
        };
    }
}
