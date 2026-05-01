package com.cooldownhud.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TickTracker {

    private static final Map<Item, long[]> data = new HashMap<>();

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        long now = mc.world.getTime();

        for (TrackedItem ti : TrackedItem.values()) {
            Item item = ti.item;
            float progress = mc.player.getItemCooldownManager()
                    .getCooldownProgress(new ItemStack(item), 0f);

            long[] entry = data.get(item);

            if (progress > 0f) {
                if (entry == null) {
                    data.put(item, new long[]{ now, 0L });
                } else if (entry[1] == 0L && now > entry[0]) {
                    float elapsed = now - entry[0];
                    if (progress < 0.999f) {
                        long computed = Math.round(elapsed / (1.0f - progress));
                        // Sanity-check: accept only if within 50%–200% of expected
                        if (computed >= ti.expectedTicks * 0.5f && computed <= ti.expectedTicks * 2.0f) {
                            entry[1] = computed;
                        }
                    }
                }
            } else {
                if (entry != null) data.remove(item);
            }
        }
    }

    public static int getTicksRemaining(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return 0;

        float progress = mc.player.getItemCooldownManager()
                .getCooldownProgress(new ItemStack(item), 0f);
        if (progress <= 0f) return 0;

        long[] entry = data.get(item);
        if (entry != null && entry[1] > 0L) {
            long now = mc.world.getTime();
            return (int) Math.max(0, entry[0] + entry[1] - now);
        }

        // Fallback: use server-defined expected duration
        for (TrackedItem ti : TrackedItem.values()) {
            if (ti.item == item) return Math.round(progress * ti.expectedTicks);
        }
        return Math.round(progress * 400f);
    }
}
