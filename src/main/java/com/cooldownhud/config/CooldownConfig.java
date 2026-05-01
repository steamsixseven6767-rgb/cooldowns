package com.cooldownhud.config;

import com.cooldownhud.CooldownHudMod;
import com.cooldownhud.client.TrackedItem;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Simple .properties config stored in .minecraft/config/cooldownhud.properties
 *
 * Keys:
 *   enabled.<configKey>   = true/false   — whether that slot is visible
 *   hud.x                 = float        — HUD panel X position (0-1 normalized)
 *   hud.y                 = float        — HUD panel Y position (0-1 normalized)
 *   hud.scale             = float        — HUD scale multiplier
 */
public class CooldownConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("cooldownhud.properties");

    private static final CooldownConfig INSTANCE = new CooldownConfig();
    public static CooldownConfig get() { return INSTANCE; }

    // --- state ---
    private final Map<String, Boolean> itemEnabled = new LinkedHashMap<>();
    private float hudX     = 0.02f;
    private float hudY     = 0.75f;
    private float hudScale = 1.0f;

    private CooldownConfig() {
        // defaults
        for (TrackedItem t : TrackedItem.values()) {
            itemEnabled.put(t.configKey, true);
        }
        load();
    }

    // ---- public API ----

    public boolean isEnabled(TrackedItem t) {
        return itemEnabled.getOrDefault(t.configKey, true);
    }

    public void setEnabled(TrackedItem t, boolean val) {
        itemEnabled.put(t.configKey, val);
    }

    public float getHudX()     { return hudX; }
    public float getHudY()     { return hudY; }
    public float getHudScale() { return hudScale; }

    public void setHudX(float v)     { hudX = v; }
    public void setHudY(float v)     { hudY = v; }
    public void setHudScale(float v) { hudScale = v; }

    // ---- persistence ----

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_PATH.toFile()))) {
            pw.println("# CooldownHUD config");
            pw.printf("hud.x=%.4f%n", hudX);
            pw.printf("hud.y=%.4f%n", hudY);
            pw.printf("hud.scale=%.2f%n", hudScale);
            for (Map.Entry<String, Boolean> e : itemEnabled.entrySet()) {
                pw.printf("enabled.%s=%b%n", e.getKey(), e.getValue());
            }
        } catch (IOException ex) {
            CooldownHudMod.LOGGER.error("[CooldownHUD] Failed to save config", ex);
        }
    }

    public void load() {
        File f = CONFIG_PATH.toFile();
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            Properties p = new Properties();
            p.load(br);
            hudX     = parseFloat(p, "hud.x",     hudX);
            hudY     = parseFloat(p, "hud.y",     hudY);
            hudScale = parseFloat(p, "hud.scale", hudScale);
            for (TrackedItem t : TrackedItem.values()) {
                String key = "enabled." + t.configKey;
                if (p.containsKey(key)) {
                    itemEnabled.put(t.configKey, Boolean.parseBoolean(p.getProperty(key)));
                }
            }
        } catch (IOException ex) {
            CooldownHudMod.LOGGER.error("[CooldownHUD] Failed to load config", ex);
        }
    }

    private float parseFloat(Properties p, String key, float def) {
        try { return Float.parseFloat(p.getProperty(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
          }
