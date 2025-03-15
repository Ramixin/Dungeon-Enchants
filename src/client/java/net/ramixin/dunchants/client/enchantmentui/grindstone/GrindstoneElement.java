package net.ramixin.dunchants.client.enchantmentui.grindstone;

import net.minecraft.item.ItemStack;
import net.ramixin.dunchants.client.enchantmentui.AbstractEnchantmentUIElement;
import net.ramixin.dunchants.client.enchantmentui.AbstractUIHoverManager;

public class GrindstoneElement extends AbstractEnchantmentUIElement {

    public GrindstoneElement(ItemStack stack, AbstractUIHoverManager hoverManager, int relX, int relY) {
        super(stack, hoverManager, relX, relY);
    }

    @Override
    public boolean renderGrayscale(int hoverIndex, String enchant) {
        return true;
    }

    @Override
    public AbstractEnchantmentUIElement createCopy(ItemStack stack) {
        return new GrindstoneElement(stack, getHoverManager(), getCachedRelatives()[0], getCachedRelatives()[1]);
    }

    @Override
    public boolean isAnimated() {
        return false;
    }
}
