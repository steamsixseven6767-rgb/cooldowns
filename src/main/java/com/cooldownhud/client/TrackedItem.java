package com.cooldownhud.client;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Every item the HUD can track.
 * expectedTicks = server-side cooldown (swortix), used as TickTracker fallback.
 */
public enum TrackedItem {

    BOZH_AURA   ("Божья аура",    "bozh_aura",    Items.PHANTOM_MEMBRANE,      300),  // 15s
    HEALING     ("Исцеление",      "healing",      Items.POTION,                400),  // 20s
    TRAPKA      ("Трапка",         "trapka",       Items.NETHERITE_SCRAP,       200),  // 10s
    PLAST       ("Пласт",          "plast",        Items.DRIED_KELP,            500),  // 25s
    DEZOR       ("Дезориентация",  "dezor",        Items.ENDER_EYE,            1200),  // 60s
    SUGAR_DUST  ("Явная пыль",     "sugar_dust",   Items.SUGAR,                1200),  // 60s
    CHARKA      ("Чарка",          "charka",       Items.ENCHANTED_GOLDEN_APPLE, 3000),// 150s
    GEPL        ("Гепл",           "gepl",         Items.GOLDEN_APPLE,          600),  // 30s
    HORUS       ("Хорус",          "horus",        Items.CHORUS_FRUIT,          400),  // 20s
    PERKA       ("Перл",           "perka",        Items.ENDER_PEARL,          1200),  // 60s
    WIND_CHARGE ("Заряд ветра",    "wind_charge",  Items.WIND_CHARGE,           600);  // 30s

    public final String displayName;
    public final String configKey;
    public final Item   item;
    public final int    expectedTicks;

    TrackedItem(String displayName, String configKey, Item item, int expectedTicks) {
        this.displayName   = displayName;
        this.configKey     = configKey;
        this.item          = item;
        this.expectedTicks = expectedTicks;
    }
}
