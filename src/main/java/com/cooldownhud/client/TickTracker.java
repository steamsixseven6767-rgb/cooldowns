package com.cooldownhud.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks cooldown start/end by watching getCooldownProgress transitions each tick.
 * When progress jumps from 0 to >0, we record the world tick and sample the
 * duration by observing how fast progress decreases.
 */
public class TickTracker {

    // item -> world tick when cooldown started, estimated total ticks
    private static final Map<Item, long[]> data = new HashMap<>();
    // [0] = startTick, [1] = estimatedTotal

    /**
     * Must be called every client tick.
     */
    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        long now = mc.world.getTime();

        for (TrackedItem ti : TrackedItem.values()) {
            Item item = ti.item;
            float progress = mc.player.getItemCooldownManager().getCooldownProgress(item, 0f);

            long[] entry = data.get(item);

            if (progress > 0f) {
                if (entry == null) {
                    // Cooldown just started — record start tick.
                    // We don't know total yet; estimate on next tick via delta.
                    data.put(item, new long[]{ now, 0L });
                } else if (entry[1] == 0L && now > entry[0]) {
                    // Estimate total from first two samples:
                    // progress = (endTick - now) / (endTick - startTick)
                    // => endTick = startTick + (now - startTick) / (1 - progress)  ... when progress < 1
                    // Simpler: total = (now - startTick) / (1.0 - progress) — but only reliable when
                    // we have at least 1 tick delta. Use: total = elapsed / (1 - progress) * 1
                    float elapsed = now - entry[0];
                    if (progress < 0.999f) {
                        entry[1] = Math.round(elapsed / (1.0f - progress));
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

        float progress = mc.player.getItemCooldownManager().getCooldownProgress(item, 0f);
        if (progress <= 0f) return 0;

        long[] entry = data.get(item);
        if (entry == null || entry[1] == 0L) {
            // Fallback: assume 20s max
            return Math.round(progress * 400f);
        }

        long now = mc.world.getTime();
        long end = entry[0] + entry[1];
        return (int) Math.max(0, end - now);
    }
}
