package com.cooldownhud.client;

import com.cooldownhud.mixin.ItemCooldownManagerMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;

import java.util.Map;

public final class CooldownHelper {

    private CooldownHelper() {}

    /**
     * Returns the number of ticks remaining on a cooldown,
     * or 0 if no cooldown is active.
     */
    public static int getTicksRemaining(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return 0;

        ItemCooldownManager mgr = mc.player.getItemCooldownManager();
        Map<Item, ItemCooldownManager.Entry> entries =
                ((ItemCooldownManagerMixin) mgr).cooldownhud$getEntries();

        if (entries == null) return 0;
        ItemCooldownManager.Entry entry = entries.get(item);
        if (entry == null) return 0;

        int now = (int)(mc.world.getTime() & Integer.MAX_VALUE);
        return Math.max(0, entry.endTick() - now);
    }

    /**
     * Returns progress 0..1 where 1 = just started, 0 = done.
     */
    public static float getProgress(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0f;
        return mc.player.getItemCooldownManager().getCooldownProgress(item, 0f);
    }

    /**
     * Formats ticks into a readable string: "1.2s" or "12s"
     */
    public static String formatTicks(int ticks) {
        float secs = ticks / 20f;
        if (secs >= 10f) return String.format("%.0fs", secs);
        return String.format("%.1fs", secs);
    }

    /**
     * Returns true if the item in the player's inventory matches a potion of healing II.
     * Used to detect "Исцеление" slot.
     */
    public static boolean playerHasHealingPotion() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (!stack.isOf(net.minecraft.item.Items.POTION)) continue;
            // Check PotionContents component (1.21.4 data-component system)
            var contents = stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;
            var potion = contents.potion();
            if (potion.isEmpty()) continue;
            // strong_healing = Healing II
            if (potion.get().value() == net.minecraft.entity.effect.StatusEffects.INSTANT_HEALTH) {
                // Check if it's level 2 (strong_healing)
                var key = potion.get().getKey();
                if (key != null && key.getValue().getPath().equals("strong_healing")) {
                    return true;
                }
            }
        }
        return false;
    }
}
