package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CooldownSettingsScreen extends Screen {

    // Layout
    private static final int PANEL_W  = 300;
    private static final int ROW_H    = 22;
    private static final int BTN_W    = 110;
    private static final int BTN_H    = 18;
    private static final int ICON_SZ  = 16;
    private static final int SLD_H    = 16;

    // Colors
    private static final int COL_BG      = 0xEE0A0A14;
    private static final int COL_BORDER  = 0x55FFFFFF;
    private static final int COL_TITLE   = 0xFFFFFFFF;
    private static final int COL_SUB     = 0xFF888899;
    private static final int COL_SECTION = 0xFF666677;

    private final Screen parent;

    public CooldownSettingsScreen(Screen parent) {
        super(Text.literal("CooldownHUD — Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CooldownConfig cfg = CooldownConfig.get();
        int panelH = computePanelH();
        int px = (width  - PANEL_W) / 2;
        int py = Math.max(4, (height - panelH) / 2);

        int y = py + 36; // below title area

        // ---- Item toggles ----
        for (TrackedItem ti : TrackedItem.values()) {
            final TrackedItem item = ti;
            boolean enabled = cfg.isEnabled(ti);

            addDrawableChild(ButtonWidget.builder(
                    toggleLabel(enabled),
                    b -> {
                        boolean now = !CooldownConfig.get().isEnabled(item);
                        CooldownConfig.get().setEnabled(item, now);
                        b.setMessage(toggleLabel(now));
                    })
                .dimensions(px + PANEL_W - BTN_W - 8, y + 2, BTN_W, BTN_H)
                .build());

            y += ROW_H;
        }

        y += 6; // gap before customization section

        // ---- Scale slider ----
        addDrawableChild(new NamedSlider(
                px + 8, y, PANEL_W - 16, SLD_H,
                "Масштаб", cfg.getHudScale(), 0.5f, 3.0f
        ) {
            @Override protected void apply(float v) { CooldownConfig.get().setHudScale(v); }
        });
        y += SLD_H + 4;

        // ---- BG Opacity slider ----
        addDrawableChild(new NamedSlider(
                px + 8, y, PANEL_W - 16, SLD_H,
                "Прозрачность фона", cfg.getBgOpacity(), 0, 255
        ) {
            @Override protected void apply(float v) { CooldownConfig.get().setBgOpacity(Math.round(v)); }
        });
        y += SLD_H + 4;

        // ---- Accent R/G/B sliders ----
        addDrawableChild(new NamedSlider(
                px + 8, y, PANEL_W - 16, SLD_H,
                "Цвет (R)", cfg.getAccentR(), 0, 255
        ) {
            @Override protected void apply(float v) { CooldownConfig.get().setAccentR(Math.round(v)); }
        });
        y += SLD_H + 4;

        addDrawableChild(new NamedSlider(
                px + 8, y, PANEL_W - 16, SLD_H,
                "Цвет (G)", cfg.getAccentG(), 0, 255
        ) {
            @Override protected void apply(float v) { CooldownConfig.get().setAccentG(Math.round(v)); }
        });
        y += SLD_H + 4;

        addDrawableChild(new NamedSlider(
                px + 8, y, PANEL_W - 16, SLD_H,
                "Цвет (B)", cfg.getAccentB(), 0, 255
        ) {
            @Override protected void apply(float v) { CooldownConfig.get().setAccentB(Math.round(v)); }
        });
        y += SLD_H + 6;

        // ---- Show bars toggle ----
        addDrawableChild(ButtonWidget.builder(
                barsLabel(cfg.isShowBars()),
                b -> {
                    boolean now = !CooldownConfig.get().isShowBars();
                    CooldownConfig.get().setShowBars(now);
                    b.setMessage(barsLabel(now));
                })
            .dimensions(px + (PANEL_W - 140) / 2, y, 140, BTN_H)
            .build());
        y += BTN_H + 6;

        // ---- Save & close ----
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Сохранить и закрыть"),
                b -> {
                    CooldownConfig.get().save();
                    this.client.setScreen(parent);
                })
            .dimensions(px + (PANEL_W - 160) / 2, y, 160, BTN_H)
            .build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int panelH = computePanelH();
        int px = (width  - PANEL_W) / 2;
        int py = Math.max(4, (height - panelH) / 2);

        // Panel BG + border
        ctx.fill(px, py, px + PANEL_W, py + panelH, COL_BG);
        ctx.fill(px,              py,              px + PANEL_W,     py + 1,           COL_BORDER);
        ctx.fill(px,              py + panelH - 1, px + PANEL_W,     py + panelH,      COL_BORDER);
        ctx.fill(px,              py,              px + 1,           py + panelH,      COL_BORDER);
        ctx.fill(px + PANEL_W - 1, py,             px + PANEL_W,     py + panelH,      COL_BORDER);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 8, COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Настройки", width / 2, py + 18, COL_SUB);
        ctx.fill(px + 8, py + 30, px + PANEL_W - 8, py + 31, COL_BORDER);

        // Item rows: icon + name
        int y = py + 36;
        for (TrackedItem ti : TrackedItem.values()) {
            // Draw item icon directly — no blur because we're outside a Screen's item rendering quirk
            ctx.drawItem(new ItemStack(ti.item), px + 8, y + 2);
            ctx.drawText(textRenderer, ti.displayName, px + 8 + ICON_SZ + 4, y + 6, COL_TITLE, false);
            y += ROW_H;
        }

        y += 6;

        // Section label for customization
        ctx.fill(px + 8, y - 2, px + PANEL_W - 8, y - 1, COL_SECTION);
        ctx.drawText(textRenderer, "Внешний вид", px + 8, y + 1, COL_SUB, false);
        // shift labels are drawn by the sliders themselves

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ---- helpers ----

    private static Text toggleLabel(boolean enabled) {
        return enabled ? Text.literal("✔ Вкл") : Text.literal("✘ Выкл");
    }

    private static Text barsLabel(boolean show) {
        return show ? Text.literal("▬ Шкалы: вкл") : Text.literal("▬ Шкалы: выкл");
    }

    private int computePanelH() {
        int itemsH   = TrackedItem.values().length * ROW_H + 6;
        int slidersH = (SLD_H + 4) * 5 + 6; // scale + opacity + R + G + B
        int buttonsH = BTN_H + 6 + BTN_H + 6;
        return 36 + itemsH + slidersH + buttonsH + 8;
    }

    // ---- generic named slider ----

    private static abstract class NamedSlider extends SliderWidget {
        private final String label;
        private final float min;
        private final float range;

        NamedSlider(int x, int y, int w, int h, String label, float initial, float min, float max) {
            super(x, y, w, h, Text.literal(""), (initial - min) / (max - min));
            this.label = label;
            this.min   = min;
            this.range = max - min;
            updateMessage(); // set initial text
        }

        float currentValue() {
            return min + (float) value * range;
        }

        @Override
        protected void updateMessage() {
            float v = currentValue();
            // Show integer for 0-255 fields, one decimal for scale
            String formatted = (range > 2f) ? String.format("%.0f", v) : String.format("%.2f", v);
            setMessage(Text.literal(label + ": " + formatted));
        }

        @Override
        protected void applyValue() {
            apply(currentValue());
        }

        protected abstract void apply(float v);
    }
}
