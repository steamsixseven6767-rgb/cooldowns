package com.cooldownhud.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Computes remaining cooldown ticks for tracked items.
 *
 * getCooldownProgress() returns a value that goes from 1.0 (just started)
 * down to 0.0 (finished). Multiplying by the known expectedTicks gives a
 * reliable remaining-ticks estimate without any server-side packet needed.
 */
public class TickTracker {

    // tick() is called every client tick but no state is needed anymore –
    // kept for compatibility with CooldownHudClient.
    public static void tick() { }

    public static int getTicksRemaining(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return 0;

        float progress = mc.player.getItemCooldownManager()
                .getCooldownProgress(new ItemStack(item), 0f);
        if (progress <= 0f) return 0;

        // progress: 1.0 = full cooldown remaining, 0.0 = done
        for (TrackedItem ti : TrackedItem.values()) {
            if (ti.item == item) return Math.round(progress * ti.expectedTicks);
        }
        // Fallback for unknown items (should not happen in practice)
        return Math.round(progress * 400f);
    }
}
