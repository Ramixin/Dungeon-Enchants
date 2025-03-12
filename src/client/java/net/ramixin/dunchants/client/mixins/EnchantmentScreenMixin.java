package net.ramixin.dunchants.client.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.ramixin.dunchants.client.EnchantmentUIElement;
import net.ramixin.util.ModUtils;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {

    @Shadow private ItemStack stack;

    @Unique
    private EnchantmentUIElement enchantingElement = null;


    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeBackgroundHeight(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.backgroundHeight = 173;
    }

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void applyTickToElements(CallbackInfo ci) {
        if(enchantingElement == null || enchantingElement.isInvalid(stack))
            enchantingElement = new EnchantmentUIElement(stack);
        enchantingElement.tick(stack);
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void preventDefaultRenderingAndRenderEnchantingUIElement(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
        int relX = (this.width - this.backgroundWidth) / 2;
        int relY = (this.height - this.backgroundHeight) / 2;
        if(enchantingElement == null) return;
        enchantingElement.render(context, relX, relY);
    }

    @Override
    public void mouseMoved(double x, double y) {
        int relX = (this.width - this.backgroundWidth) / 2;
        int relY = (this.height - this.backgroundHeight) / 2;
        if(enchantingElement == null) return;
        enchantingElement.updateMousePosition(x, y, relX, relY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void applyEnchantmentOnClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.mouseClicked(mouseX, mouseY, button));
        if(this.client == null || this.client.interactionManager == null) return;
        if(ModUtils.hasInvalidOptions(stack, this.client.world)) return;
        if(enchantingElement == null) return;
        int optionId = enchantingElement.getActiveHoverOption();
        if(optionId == -1) return;
        this.handler.onButtonClick(this.client.player, optionId);
        this.client.interactionManager.clickButton(this.handler.syncId, optionId);
    }
}
