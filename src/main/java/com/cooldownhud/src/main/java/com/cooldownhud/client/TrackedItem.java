package com.cooldownhud.client;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Every item the HUD can track.
 * POTION_HEALING is matched by potion type, not just item id.
 */
public enum TrackedItem {

    CHARKA      ("Чарка",         "charka",       Items.ENCHANTED_GOLDEN_APPLE),
    GEPL        ("Гепл",          "gepl",         Items.GOLDEN_APPLE),
    PERKA       ("Перка",         "perka",        Items.ENDER_PEARL),
    HORUS       ("Хорус",         "horus",        Items.CHORUS_FRUIT),
    PLAST       ("Пласт",         "plast",        Items.DRIED_KELP),
    TRAPKA      ("Трапка",        "trapka",       Items.NETHERITE_SCRAP),
    DEZOR       ("Дезориентация", "dezor",        Items.ENDER_EYE),
    SUGAR_DUST  ("Явная пыль",    "sugar_dust",   Items.SUGAR),
    BOZH_AURA   ("Божья аура",    "bozh_aura",    Items.PHANTOM_MEMBRANE),
    HEALING     ("Исцеление",     "healing",      Items.POTION);    // matched by PotionContents

    public final String displayName;
    /** Config key, used in cooldownhud.properties */
    public final String configKey;
    public final Item item;

    TrackedItem(String displayName, String configKey, Item item) {
        this.displayName = displayName;
        this.configKey   = configKey;
        this.item        = item;
    }
}
