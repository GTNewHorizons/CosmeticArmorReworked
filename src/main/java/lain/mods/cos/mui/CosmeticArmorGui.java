package lain.mods.cos.mui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import cpw.mods.fml.common.Loader;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.inventory.InventoryCosArmor;

public class CosmeticArmorGui implements IGuiHolder<GuiData> {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    private static final String FACTORY_NAME = "cos:armor";
    private static final UITexture LEGACY_LEFT = texture(0, 0, 104, 83);
    private static final UITexture NORMAL_INVENTORY = texture(0, 83, WIDTH, 83);
    private static final UITexture INVENTORY_BUTTON = texture(0, 166, 10, 10);
    private static final UITexture INVENTORY_BUTTON_HOVERED = texture(10, 166, 10, 10);
    private static final UITexture TOGGLE_OFF = texture(0, 176, 5, 5);
    private static final UITexture TOGGLE_ON = texture(5, 176, 5, 5);
    private static final SimpleGuiFactory FACTORY = GuiFactories
        .createSimple(FACTORY_NAME, () -> CosmeticArmorReworked.invMan.createCosmeticArmorGui());

    public static void init() {
        FACTORY.init();
    }

    public static void open(EntityPlayerMP player) {
        FACTORY.open(player);
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        EntityPlayer player = data.getPlayer();
        InventoryCosArmor cosmeticInventory = data.isClient()
            ? CosmeticArmorReworked.invMan.getCosArmorInventoryClient(player.getUniqueID())
            : CosmeticArmorReworked.invMan.getCosArmorInventory(player.getUniqueID());

        syncManager.bindPlayerInventory(player);
        syncManager.registerSlotGroup("functional_armor", 1, 20);
        syncManager.registerSlotGroup("cosmetic_armor", 1, 10);

        IItemHandlerModifiable playerInventory = new InvWrapper(player.inventory);
        IItemHandlerModifiable cosmeticItems = new InvWrapper(cosmeticInventory);
        int armorStart = player.inventory.mainInventory.length;

        ModularPanel panel = ModularPanel.defaultPanel("cosmetic_armor", WIDTH, HEIGHT)
            .background(GuiTextures.MC_BACKGROUND);
        panel.child(
            new Widget<>().background(LEGACY_LEFT)
                .pos(0, 0)
                .size(104, 83)
                .name("legacy_left_frame"));
        panel.child(
            new Widget<>().background(NORMAL_INVENTORY)
                .pos(0, 83)
                .size(WIDTH, 83)
                .name("normal_inventory_frame"));

        for (int row = 0; row < 4; row++) {
            int inventoryIndex = 3 - row;
            int armorType = row;

            ModularSlot functionalArmorSlot = armorSlot(playerInventory, armorStart + inventoryIndex)
                .filter(stack -> isValidArmor(stack, armorType, player))
                .slotGroup("functional_armor");
            ModularSlot cosmeticArmorSlot = armorSlot(cosmeticItems, inventoryIndex)
                .filter(stack -> isValidArmor(stack, armorType, player))
                .changeListener(
                    (stack, onlyAmountChanged, client, init) -> {
                        if (!client && !init) cosmeticInventory.markDirty();
                    })
                .slotGroup("cosmetic_armor");

            panel.child(
                new ItemSlot().slot(functionalArmorSlot)
                    .background(
                        data.isClient() ? ClientGuiActions.armorSlotIcon(functionalArmorSlot, armorType)
                            : IDrawable.EMPTY)
                    .pos(7, 7 + row * 18)
                    .name("functional_armor_" + armorType));

            panel.child(
                new ItemSlot().slot(cosmeticArmorSlot)
                    .background(
                        data.isClient() ? ClientGuiActions.armorSlotIcon(cosmeticArmorSlot, armorType)
                            : IDrawable.EMPTY)
                    .pos(79, 7 + row * 18)
                    .name("cosmetic_armor_" + armorType));

            BooleanSyncValue visible = new BooleanSyncValue(
                () -> cosmeticInventory.isSkinArmor(inventoryIndex),
                enabled -> {
                    cosmeticInventory.setSkinArmor(inventoryIndex, enabled);
                    cosmeticInventory.markDirty();
                }).allowC2S();

            panel.child(
                new ToggleButton().value(visible)
                    .background(false, TOGGLE_OFF)
                    .background(true, TOGGLE_ON)
                    .hoverBackground(false, TOGGLE_OFF)
                    .hoverBackground(true, TOGGLE_ON)
                    .addTooltip(false, IKey.lang("cos.gui.hideArmor"))
                    .addTooltip(true, IKey.lang("cos.gui.showArmor"))
                    .pos(97, 7 + row * 18)
                    .size(5)
                    .name("cosmetic_visibility_" + armorType));
        }

        panel.child(
            new ButtonWidget<>().background(INVENTORY_BUTTON)
                .hoverBackground(INVENTORY_BUTTON_HOVERED)
                .addTooltipLine(IKey.lang("cos.gui.buttonNormal"))
                .onMousePressed(button -> {
                    if (data.isClient()) ClientGuiActions.openNormalInventory();
                    return true;
                })
                .pos(66, 67)
                .size(10)
                .name("normal_inventory"));

        panel.child(
            new ToggleButton()
                .value(
                    new BoolValue.Dynamic(
                        () -> data.isClient() && !ClientGuiActions.isRendererEnabled(),
                        disabled -> { if (data.isClient()) ClientGuiActions.setRendererEnabled(!disabled); }))
                .background(false, TOGGLE_OFF)
                .background(true, TOGGLE_ON)
                .hoverBackground(false, TOGGLE_OFF)
                .hoverBackground(true, TOGGLE_ON)
                .addTooltip(false, IKey.lang("cos.gui.rendererEnabled"))
                .addTooltip(true, IKey.lang("cos.gui.rendererDisabled"))
                .pos(60, 72)
                .size(5)
                .name("renderer_toggle"));

        if (data.isClient() && Loader.isModLoaded("serverutilities")) {
            ServerUtilitiesIntegration.addSidebar(panel);
        }

        panel.child(SlotGroupWidget.playerInventory(7, true, (index, slot) -> slot.background(IDrawable.EMPTY)));
        return panel;
    }

    private static boolean isValidArmor(ItemStack stack, int armorType, EntityPlayer player) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item != null
            && (item.isValidArmor(stack, armorType, player) || armorType == 0 && item instanceof ItemBlock);
    }

    private static ModularSlot armorSlot(IItemHandlerModifiable inventory, int index) {
        return new ModularSlot(inventory, index) {

            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public int getItemStackLimit(ItemStack stack) {
                return 1;
            }
        };
    }

    private static UITexture texture(int x, int y, int width, int height) {
        return UITexture.builder()
            .location(CosmeticArmorReworked.MOD_ID, "gui/cosarmorinventory")
            .imageSize(256, 256)
            .subAreaXYWH(x, y, width, height)
            .build();
    }

    public CosmeticArmorGui() {}
}
