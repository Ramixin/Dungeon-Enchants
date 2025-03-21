package net.ramixin.dunchants.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.ramixin.dunchants.enchantments.ModEnchantmentEffects;
import net.ramixin.dunchants.payloads.EnchantmentPointsUpdateS2CPayload;
import net.ramixin.dunchants.util.ModUtils;
import net.ramixin.dunchants.util.PlayerEntityDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityDuck {

    @Shadow public int experienceLevel;

    @Unique
    private int enchantmentPoints;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getNextLevelExperience", at = @At("HEAD"), cancellable = true)
    private void changeXpProgression(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(50 * (experienceLevel + 1));
    }

    @ModifyReturnValue(method = "shouldAlwaysDropExperience", at = @At("RETURN"))
    private boolean changeShouldAlwaysDropXp(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "getExperienceToDrop", at = @At("RETURN"))
    private int preventXpOrbsFromDropping(int original) {
        return 0;
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeEnchantmentPointsToData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("EnchantmentPoints", enchantmentPoints);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readEnchantmentPointsFromData(NbtCompound nbt, CallbackInfo ci) {
        enchantmentPoints = nbt.getInt("EnchantmentPoints");
    }

    @Inject(method = "addExperienceLevels", at = @At("TAIL"))
    private void grantEnchantmentPointOnLevelUp(int levels, CallbackInfo ci) {
        if(levels == 0) return;
        enchantmentPoints += levels;
        //noinspection ConstantValue
        if(!(((LivingEntity) this) instanceof ServerPlayerEntity serverPlayer)) return;
        ServerPlayNetworking.send(serverPlayer, new EnchantmentPointsUpdateS2CPayload(enchantmentPoints));
    }


    @Override
    public void dungeonEnchants$changeEnchantmentPoints(int delta) {
        enchantmentPoints += delta;
    }

    @Override
    public void dungeonEnchants$setEnchantmentPoints(int enchantmentPoints) {
        this.enchantmentPoints = enchantmentPoints;
    }

    @Override
    public int dungeonEnchants$getEnchantmentPoints() {
        return enchantmentPoints;
    }



    @WrapOperation(method = "vanishCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasAnyEnchantmentsWith(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Z"))
    private boolean applyLeveledVanishingCurse(ItemStack stack, ComponentType<?> componentType, Operation<Boolean> original) {
        boolean val = original.call(stack, componentType);
        return val | ModUtils.getLeveledEnchantmentEffectValue(ModEnchantmentEffects.LEVELED_PREVENT_EQUIPMENT_DROP, getWorld(), stack);
    }
}
