package com.cooldownhud.client;

/**
 * Interface injected into ItemCooldownManager via Mixin
 * to expose raw tick data.
 */
public interface ICooldownManagerAccess {
    int cooldownhud$getTicksRemaining(net.minecraft.item.Item item);
}
