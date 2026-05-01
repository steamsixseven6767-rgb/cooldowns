package com.cooldownhud.mixin;

import net.minecraft.entity.player.ItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder mixin — no injections needed.
 * CooldownHelper uses the public API (getCooldownProgress, isCoolingDown)
 * with ItemStack directly, so no private field access is required.
 */
@Mixin(ItemCooldownManager.class)
public class ItemCooldownManagerMixin {
    // intentionally empty
}
