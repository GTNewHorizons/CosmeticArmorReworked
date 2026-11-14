package lain.mods.cos.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import lain.mods.cos.CosmeticArmorReworked;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @ModifyExpressionValue(
        method = { "shouldRenderPass(Lnet/minecraft/client/entity/AbstractClientPlayer;IF)I",
            "func_82408_c(Lnet/minecraft/client/entity/AbstractClientPlayer;IF)V" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;armorItemInSlot(I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack getArmorToRenderForPass(ItemStack original, @Local(argsOnly = true) AbstractClientPlayer player,
        @Local(argsOnly = true) int renderPass) {
        // vanilla reads armorItemInSlot(3 - renderPass) in these methods
        return CosmeticArmorReworked.getClient()
            .getArmorToRender(player, 3 - renderPass, original);
    }

    @ModifyExpressionValue(
        method = "renderEquippedItems(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/InventoryPlayer;armorItemInSlot(I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack getHelmetToRender(ItemStack original, @Local(argsOnly = true) AbstractClientPlayer player) {
        final int HELMET_SLOT = 3;
        return CosmeticArmorReworked.getClient()
            .getArmorToRender(player, HELMET_SLOT, original);
    }

}
