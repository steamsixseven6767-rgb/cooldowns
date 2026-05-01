package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * "⚙ Вид панели" screen — scale, bg opacity, accent colour, bars toggle.
 * Changes apply live so the HUD preview updates in real time.
 * shouldPause=false keeps the HUD renderer firing → instant preview.
 */
public class CooldownCustomizeScreen extends Screen {

    private static final int PANEL_W = 260;
    private static final int BTN_H   = 14;
    private static final int ROW_H   = 28;
    private static final int PAD     = 10;

    private static final int COL_PANEL  = 0xEE080812;
    private static final int COL_TITLE  = 0xFFDDDDFF;
    private static final int COL_SUB    = 0xFF6666AA;
    private static final int COL_SEP    = 0x33FFFFFF;
    private static final int COL_LABEL  = 0xFFAAAAAA;

    private final Screen parent;

    private ScaleSlider   scaleSlider;
    private OpacitySlider opacitySlider;
    private ColorSlider   rSlider, gSlider, bSlider;

    public CooldownCustomizeScreen(Screen parent) {
        super(Text.literal("Вид панели"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CooldownConfig cfg = CooldownConfig.get();

        int px = getPanelX();
        int py = getPanelY();
        int sw = PANEL_W - PAD * 2;
        int y  = py + 32; // after title

        scaleSlider = new ScaleSlider(px + PAD, y, sw, BTN_H + 2,
                (cfg.getHudScale() - 0.5f) / 2.5f);
        addDrawableChild(scaleSlider);
        y += ROW_H;

        opacitySlider = new OpacitySlider(px + PAD, y, sw, BTN_H + 2,
                cfg.getBgOpacity() / 255.0);
        addDrawableChild(opacitySlider);
        y += ROW_H;

        rSlider = new ColorSlider("R", cfg.getAccentR(), px + PAD, y, sw, BTN_H + 2);
        addDrawableChild(rSlider);
        y += ROW_H;

        gSlider = new ColorSlider("G", cfg.getAccentG(), px + PAD, y, sw, BTN_H + 2);
        addDrawableChild(gSlider);
        y += ROW_H;

        bSlider = new ColorSlider("B", cfg.getAccentB(), px + PAD, y, sw, BTN_H + 2);
        addDrawableChild(bSlider);
        y += ROW_H + 4;

        addDrawableChild(ButtonWidget.builder(
                barsText(cfg.isShowBars()),
                b -> {
                    boolean now = !CooldownConfig.get().isShowBars();
                    CooldownConfig.get().setShowBars(now);
                    b.setMessage(barsText(now));
                }
        ).dimensions(px + PAD, y, sw, BTN_H + 2).build());
        y += ROW_H;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔  Сохранить и назад"),
                b -> {
                    CooldownConfig.get().save();
                    client.setScreen(parent);
                }
        ).dimensions(px + PAD, y, sw, BTN_H + 2).build());
    }

    /** Push slider values into config every frame so HUD preview is live. */
    private void applyLive() {
        CooldownConfig cfg = CooldownConfig.get();
        if (scaleSlider   != null) cfg.setHudScale  (scaleSlider.getScale());
        if (opacitySlider != null) cfg.setBgOpacity  (opacitySlider.getOpacity());
        if (rSlider != null) cfg.setAccentR(rSlider.getChannel());
        if (gSlider != null) cfg.setAccentG(gSlider.getChannel());
        if (bSlider != null) cfg.setAccentB(bSlider.getChannel());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        applyLive(); // live preview

        ctx.fill(0, 0, width, height, 0x99000000);

        // Draw the real HUD panel as preview (visible behind the settings)
        CooldownHudRenderer.INSTANCE.renderInScreen(ctx, client);

        int px = getPanelX();
        int py = getPanelY();
        int ph = getPanelH();

        ctx.fill(px + 3, py + 3, px + PANEL_W + 3, py + ph + 3, 0x55000000);
        CooldownHudRenderer.drawRoundedRect(ctx, px, py, PANEL_W, ph, 3, COL_PANEL);
        CooldownHudRenderer.drawRoundedBorder(ctx, px, py, PANEL_W, ph, 3, 0xFF2E2E55);

        ctx.fill(px + 1, py + 1, px + PANEL_W - 1, py + 26, 0x1AFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 4,  COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Вид панели",  width / 2, py + 14, COL_SUB);
        ctx.fill(px + 6, py + 27, px + PANEL_W - 6, py + 28, COL_SEP);

        // Labels above sliders
        int y = py + 32;
        drawLabel(ctx, "Масштаб",            px + PAD, y - 8); y += ROW_H;
        drawLabel(ctx, "Прозрачность фона",  px + PAD, y - 8); y += ROW_H;
        drawLabel(ctx, "Акцент R",           px + PAD, y - 8); y += ROW_H;
        drawLabel(ctx, "Акцент G",           px + PAD, y - 8); y += ROW_H;
        drawLabel(ctx, "Акцент B",           px + PAD, y - 8);

        // Color swatch
        CooldownConfig cfg = CooldownConfig.get();
        int sw2 = 12;
        ctx.fill(px + PANEL_W - PAD - sw2, py + 32 + ROW_H * 2,
                 px + PANEL_W - PAD,       py + 32 + ROW_H * 2 + ROW_H * 3 - 4,
                 cfg.accentColor(0xFF));

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawLabel(DrawContext ctx, String text, int x, int y) {
        ctx.drawText(textRenderer, text, x, y, COL_LABEL, false);
    }

    private int getPanelX() { return (width  - PANEL_W) / 2; }
    private int getPanelY() { return Math.max(4, (height - getPanelH()) / 2); }
    private int getPanelH() {
        // 32 header + 5 sliders × ROW_H + 4 + bars btn + bars btn spacing + save btn + PAD
        return 32 + ROW_H * 5 + 4 + (BTN_H + 2) + ROW_H + (BTN_H + 2) + PAD;
    }

    private static Text barsText(boolean on) {
        return on ? Text.literal("█ Полоски КД: ВКЛ") : Text.literal("░ Полоски КД: ВЫКЛ");
    }

    @Override
    public boolean shouldPause() { return false; }

    // ---- inner slider classes ----

    private static class ScaleSlider extends SliderWidget {
        ScaleSlider(int x, int y, int w, int h, double value) {
            super(x, y, w, h, Text.empty(), value);
            updateMessage();
        }
        float getScale() { return 0.5f + (float)(value * 2.5f); }
        @Override protected void updateMessage() {
            setMessage(Text.literal(String.format("Масштаб: %.2f×", getScale())));
        }
        @Override protected void applyValue() {}
    }

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
