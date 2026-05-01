package com.cooldownhud.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class CooldownHelper {

    private CooldownHelper() {}

    public static int getTicksRemaining(Item item) {
        return TickTracker.getTicksRemaining(item);
    }

    public static float getProgress(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0f;
        return mc.player.getItemCooldownManager().getCooldownProgress(new ItemStack(item), 0f);
    }

    public static boolean hasCooldown(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        return mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(item));
    }

    public static String formatTicks(int ticks) {
        float secs = ticks / 20f;
        if (secs >= 10f) return String.format("%.0fs", secs);
        return String.format("%.1fs", secs);
    }
}
