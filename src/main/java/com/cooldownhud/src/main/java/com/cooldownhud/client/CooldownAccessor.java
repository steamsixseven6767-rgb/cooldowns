package com.cooldownhud.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

/**
 * Accesses the raw ticks remaining for a given item cooldown.
 * Works by using the mixin-exposed interface on ItemCooldownManager.
 */
public class CooldownAccessor {

    public static int getTicksRemaining(PlayerEntity player, Item item) {
        // Delegate to the mixin-instrumented manager
        if (player.getItemCooldownManager() instanceof ICooldownManagerAccess access) {
            return access.cooldownhud$getTicksRemaining(item);
        }
        // Fallback: estimate from progress (less accurate)
        float progress = player.getItemCooldownManager().getCooldownProgress(item, 0f);
        return (int) (progress * 40); // rough fallback (2 seconds base)
    }
}
