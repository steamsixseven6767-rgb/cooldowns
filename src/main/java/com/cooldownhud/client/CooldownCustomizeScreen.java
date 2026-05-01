package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * "⚙ Вид панели" screen — lets the player tweak HUD scale, bg opacity,
 * accent colour and bar visibility without touching a config file.
 */
public class CooldownCustomizeScreen extends Screen {

    // ---- layout ----
    private static final int PANEL_W = 280;
    private static final int BTN_H   = 16;
    private static final int PAD     = 10;
    private static final int ROW_H   = 26;

    // ---- colours (reuse from settings screen) ----
    private static final int COL_PANEL  = 0xEE080812;
    private static final int COL_BORDER = 0xFF2E2E55;
    private static final int COL_TITLE  = 0xFFDDDDFF;
    private static final int COL_SUB    = 0xFF6666AA;
    private static final int COL_SEP    = 0x33FFFFFF;
    private static final int COL_LABEL  = 0xFFCCCCDD;

    private final Screen parent;

    // Sliders kept as fields so render() can read their current values for the preview
    private ScaleSlider   scaleSlider;
    private OpacitySlider opacitySlider;
    private ColorSlider   rSlider, gSlider, bSlider;

    public CooldownCustomizeScreen(Screen parent) {
        super(Text.literal("Вид панели"));
        this.parent = parent;
    }

    // ---- init ----

    @Override
    protected void init() {
        CooldownConfig cfg = CooldownConfig.get();

        int px = getPanelX();
        int py = getPanelY();
        int sliderW = PANEL_W - PAD * 2;
        int y = py + 34;

        // Scale  0.5 – 3.0
        scaleSlider = new ScaleSlider(px + PAD, y, sliderW, BTN_H + 2,
                (cfg.getHudScale() - 0.5f) / 2.5f);
        addDrawableChild(scaleSlider);
        y += ROW_H;

        // BG Opacity  0 – 255
        opacitySlider = new OpacitySlider(px + PAD, y, sliderW, BTN_H + 2,
                cfg.getBgOpacity() / 255.0);
        addDrawableChild(opacitySlider);
        y += ROW_H;

        // Accent R / G / B
        rSlider = new ColorSlider("R", cfg.getAccentR(), px + PAD, y, sliderW, BTN_H + 2);
        addDrawableChild(rSlider);
        y += ROW_H;

        gSlider = new ColorSlider("G", cfg.getAccentG(), px + PAD, y, sliderW, BTN_H + 2);
        addDrawableChild(gSlider);
        y += ROW_H;

        bSlider = new ColorSlider("B", cfg.getAccentB(), px + PAD, y, sliderW, BTN_H + 2);
        addDrawableChild(bSlider);
        y += ROW_H + 4;

        // Show bars toggle
        addDrawableChild(ButtonWidget.builder(
                barsText(cfg.isShowBars()),
                b -> {
                    boolean now = !CooldownConfig.get().isShowBars();
                    CooldownConfig.get().setShowBars(now);
                    b.setMessage(barsText(now));
                }
        ).dimensions(px + PAD, y, sliderW, BTN_H + 2).build());
        y += ROW_H + 4;

        // Save & back
        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔  Сохранить и назад"),
                b -> {
                    applyAll();
                    CooldownConfig.get().save();
                    client.setScreen(parent);
                }
        ).dimensions(px + PAD, y, sliderW, BTN_H + 2).build());
    }

    // ---- render ----

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x99000000);

        int px = getPanelX();
        int py = getPanelY();
        int ph = getPanelH();

        ctx.fill(px + 3, py + 3, px + PANEL_W + 3, py + ph + 3, 0x55000000);
        ctx.fill(px, py, px + PANEL_W, py + ph, COL_PANEL);
        CooldownSettingsScreen.drawBorder(ctx, px, py, PANEL_W, ph, COL_BORDER);

        // Title bar
        ctx.fill(px + 1, py + 1, px + PANEL_W - 1, py + 27, 0x1AFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 5, COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Вид панели", width / 2, py + 16, COL_SUB);
        ctx.fill(px + 6, py + 28, px + PANEL_W - 6, py + 29, COL_SEP);

        // Row labels drawn above each slider
        int y = py + 34;
        drawLabel(ctx, "Масштаб",      px + PAD, y - 9);  y += ROW_H;
        drawLabel(ctx, "Прозрачность", px + PAD, y - 9);  y += ROW_H;
        drawLabel(ctx, "Цвет акцента (R)", px + PAD, y - 9); y += ROW_H;
        drawLabel(ctx, "Цвет акцента (G)", px + PAD, y - 9); y += ROW_H;
        drawLabel(ctx, "Цвет акцента (B)", px + PAD, y - 9);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawLabel(DrawContext ctx, String text, int x, int y) {
        ctx.drawText(textRenderer, text, x, y, COL_LABEL, false);
    }

    // ---- helpers ----

    private void applyAll() {
        CooldownConfig cfg = CooldownConfig.get();
        if (scaleSlider   != null) cfg.setHudScale  (scaleSlider.getScale());
        if (opacitySlider != null) cfg.setBgOpacity  (opacitySlider.getOpacity());
        if (rSlider != null) cfg.setAccentR(rSlider.getChannel());
        if (gSlider != null) cfg.setAccentG(gSlider.getChannel());
        if (bSlider != null) cfg.setAccentB(bSlider.getChannel());
    }

    private int getPanelX() { return (width  - PANEL_W) / 2; }
    private int getPanelY() { return Math.max(2, (height - getPanelH()) / 2); }
    private int getPanelH() {
        // title(34) + 5 sliders + bars btn + save btn + spacing
        return 34 + ROW_H * 5 + 4 + (BTN_H + 2) + 4 + (BTN_H + 2) + PAD;
    }

    private static Text barsText(boolean on) {
        return on ? Text.literal("█ Полоски КД: ВКЛ") : Text.literal("░ Полоски КД: ВЫКЛ");
    }

    @Override
    public boolean shouldPause() { return false; }

    // ================================================================
    // Inner slider classes
    // ================================================================

    /** Scale slider: maps [0,1] → [0.5, 3.0] */
    private static class ScaleSlider extends SliderWidget {
        ScaleSlider(int x, int y, int w, int h, double value) {
            super(x, y, w, h, Text.empty(), value);
            updateMessage();
        }
        float getScale() { return 0.5f + (float)(value * 2.5f); }
        @Override protected void updateMessage() {
            setMessage(Text.literal(String.format("Масштаб: %.2f×", getScale())));
        }
        @Override protected void applyValue() { /* live-preview on next render */ }
    }

    /** Opacity slider: maps [0,1] → [0, 255] */
    private static class OpacitySlider extends SliderWidget {
        OpacitySlider(int x, int y, int w, int h, double value) {
            super(x, y, w, h, Text.empty(), value);
            updateMessage();
        }
        int getOpacity() { return (int)(value * 255); }
        @Override protected void updateMessage() {
            setMessage(Text.literal(String.format("Фон: %d%%", (int)(value * 100))));
        }
        @Override protected void applyValue() {}
    }

    /** One-channel (R/G/B) slider: maps [0,1] → [0, 255] */
    private static class ColorSlider extends SliderWidget {
        private final String channel;
        ColorSlider(String channel, int initial, int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), initial / 255.0);
            this.channel = channel;
            updateMessage();
        }
        int getChannel() { return (int)(value * 255); }
        @Override protected void updateMessage() {
            setMessage(Text.literal(String.format("%s: %d", channel, getChannel())));
        }
        @Override protected void applyValue() {}
    }
  }
