package com.cooldownhud.mixin;

import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Exposes getCooldownEntry via a public shadow so CooldownHelper can read
 * startTick/endTick without touching the private Entry class directly.
 */
@Mixin(ItemCooldownManager.class)
public abstract class ItemCooldownManagerMixin {

    // Shadow the protected/package method that returns the raw progress fraction.
    // We don't need Entry at all — we derive ticks from getCooldown() + world time.
    @Shadow
    public abstract boolean isCoolingDown(Item item);
}
