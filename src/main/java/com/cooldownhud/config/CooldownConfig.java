package com.cooldownhud.config;

import com.cooldownhud.CooldownHudMod;
import com.cooldownhud.client.TrackedItem;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Config stored in .minecraft/config/cooldownhud.properties
 *
 *   enabled.<key>      = true/false
 *   hud.x / hud.y      = float 0-1 (normalised screen position)
 *   hud.scale          = float 0.5-3.0
 *   hud.bg_opacity     = int   0-255
 *   hud.accent_r/g/b   = int   0-255
 *   hud.show_bars      = true/false
 */
public class CooldownConfig {

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("cooldownhud.properties");

    private static final CooldownConfig INSTANCE = new CooldownConfig();
    public static CooldownConfig get() { return INSTANCE; }

    private final Map<String, Boolean> itemEnabled = new LinkedHashMap<>();
    private float   hudX      = 0.02f;
    private float   hudY      = 0.75f;
    private float   hudScale  = 1.0f;
    private int     bgOpacity = 200;
    private int     accentR   = 68;
    private int     accentG   = 255;
    private int     accentB   = 136;
    private boolean showBars  = true;

    private CooldownConfig() {
        for (TrackedItem t : TrackedItem.values()) itemEnabled.put(t.configKey, true);
        load();
    }

    public boolean isEnabled(TrackedItem t)             { return itemEnabled.getOrDefault(t.configKey, true); }
    public void    setEnabled(TrackedItem t, boolean v) { itemEnabled.put(t.configKey, v); }

    public float   getHudX()      { return hudX; }
    public float   getHudY()      { return hudY; }
    public float   getHudScale()  { return hudScale; }
    public int     getBgOpacity() { return bgOpacity; }
    public int     getAccentR()   { return accentR; }
    public int     getAccentG()   { return accentG; }
    public int     getAccentB()   { return accentB; }
    public boolean isShowBars()   { return showBars; }

    public void setHudX(float v)        { hudX = v; }
    public void setHudY(float v)        { hudY = v; }
    public void setHudScale(float v)    { hudScale = Math.max(0.5f, Math.min(3.0f, v)); }
    public void setBgOpacity(int v)     { bgOpacity = clamp(v, 0, 255); }
    public void setAccentR(int v)       { accentR = clamp(v, 0, 255); }
    public void setAccentG(int v)       { accentG = clamp(v, 0, 255); }
    public void setAccentB(int v)       { accentB = clamp(v, 0, 255); }
    public void setShowBars(boolean v)  { showBars = v; }

    /** ARGB with given alpha (0-255). */
    public int accentColor(int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (accentR << 16) | (accentG << 8) | accentB;
    }

    /** Background ARGB. */
    public int bgColor() { return (bgOpacity << 24) | 0x080812; }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_PATH.toFile()))) {
            pw.println("# CooldownHUD config");
            pw.printf("hud.x=%.4f%n",        hudX);
            pw.printf("hud.y=%.4f%n",        hudY);
            pw.printf("hud.scale=%.2f%n",    hudScale);
            pw.printf("hud.bg_opacity=%d%n", bgOpacity);
            pw.printf("hud.accent_r=%d%n",   accentR);
            pw.printf("hud.accent_g=%d%n",   accentG);
            pw.printf("hud.accent_b=%d%n",   accentB);
            pw.printf("hud.show_bars=%b%n",  showBars);
            for (Map.Entry<String, Boolean> e : itemEnabled.entrySet())
                pw.printf("enabled.%s=%b%n", e.getKey(), e.getValue());
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
            hudX      = parseFloat(p, "hud.x",          hudX);
            hudY      = parseFloat(p, "hud.y",          hudY);
            hudScale  = parseFloat(p, "hud.scale",      hudScale);
            bgOpacity = parseInt  (p, "hud.bg_opacity", bgOpacity);
            accentR   = parseInt  (p, "hud.accent_r",   accentR);
            accentG   = parseInt  (p, "hud.accent_g",   accentG);
            accentB   = parseInt  (p, "hud.accent_b",   accentB);
            showBars  = parseBool (p, "hud.show_bars",  showBars);
            for (TrackedItem t : TrackedItem.values()) {
                String key = "enabled." + t.configKey;
                if (p.containsKey(key))
                    itemEnabled.put(t.configKey, Boolean.parseBoolean(p.getProperty(key)));
            }
        } catch (IOException ex) {
            CooldownHudMod.LOGGER.error("[CooldownHUD] Failed to load config", ex);
        }
    }

    private float   parseFloat(Properties p, String k, float   d) {
        try { return Float.parseFloat(p.getProperty(k, String.valueOf(d))); } catch (NumberFormatException e) { return d; }
    }
    private int     parseInt  (Properties p, String k, int     d) {
        try { return Integer.parseInt(p.getProperty(k, String.valueOf(d))); } catch (NumberFormatException e) { return d; }
    }
    private boolean parseBool (Properties p, String k, boolean d) {
        String v = p.getProperty(k); return v != null ? Boolean.parseBoolean(v) : d;
    }
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
