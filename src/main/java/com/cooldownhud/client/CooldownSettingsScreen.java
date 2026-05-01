package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class CooldownSettingsScreen extends Screen {

    private static final int ROW_H    = 24;
    private static final int BTN_W    = 120;
    private static final int BTN_H    = 20;
    private static final int PANEL_W  = 320;

    // Colors
    private static final int COL_BG     = 0xEE0D0D17;
    private static final int COL_BORDER = 0x55FFFFFF;
    private static final int COL_TITLE  = 0xFFFFFFFF;
    private static final int COL_SUB    = 0xFFAAAAAA;

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
        int py = (height - panelH)  / 2;

        int y = py + 40; // below title

        // Toggle button per item
        for (TrackedItem ti : TrackedItem.values()) {
            final TrackedItem item = ti;
            boolean enabled = cfg.isEnabled(ti);

            ButtonWidget btn = ButtonWidget.builder(
                    toggleLabel(ti, enabled),
                    b -> {
                        boolean nowEnabled = !CooldownConfig.get().isEnabled(item);
                        CooldownConfig.get().setEnabled(item, nowEnabled);
                        b.setMessage(toggleLabel(item, nowEnabled));
                    })
                .dimensions(px + PANEL_W - BTN_W - 12, y, BTN_W, BTN_H)
                .build();

            addDrawableChild(btn);
            y += ROW_H;
        }

        y += 8; // extra gap before scale

        // Scale slider
        addDrawableChild(new ScaleSlider(px + 12, y, PANEL_W - 24, BTN_H, cfg.getHudScale()));

        y += ROW_H + 8;

        // Close / Save button
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
        // Dim background
        renderBackground(ctx, mouseX, mouseY, delta);

        CooldownConfig cfg = CooldownConfig.get();
        int panelH = computePanelH();
        int px = (width  - PANEL_W) / 2;
        int py = (height - panelH)  / 2;

        // Panel background
        ctx.fill(px, py, px + PANEL_W, py + panelH, COL_BG);
        // Border
        ctx.fill(px,           py,           px + PANEL_W, py + 1,          COL_BORDER);
        ctx.fill(px,           py + panelH - 1, px + PANEL_W, py + panelH,  COL_BORDER);
        ctx.fill(px,           py,           px + 1,       py + panelH,     COL_BORDER);
        ctx.fill(px + PANEL_W - 1, py,      px + PANEL_W, py + panelH,     COL_BORDER);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 10, COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Настройки отображения", width / 2, py + 22, COL_SUB);

        // Separator
        ctx.fill(px + 10, py + 34, px + PANEL_W - 10, py + 35, COL_BORDER);

        // Row labels (item names)
        int y = py + 40;
        for (TrackedItem ti : TrackedItem.values()) {
            ctx.drawItem(new net.minecraft.item.ItemStack(ti.item), px + 12, y + 2);
            ctx.drawText(textRenderer, ti.displayName, px + 32, y + 6, 0xFFFFFFFF, false);
            y += ROW_H;
        }

        y += 8;
        // Scale label
        ctx.drawText(textRenderer, "Масштаб HUD", px + 12, y + 6, COL_SUB, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private Text toggleLabel(TrackedItem ti, boolean enabled) {
        return enabled
            ? Text.literal("✔ Показывать")
            : Text.literal("✘ Скрыть");
    }

    private int computePanelH() {
        // items + scale row + save button + paddings
        return 40 + TrackedItem.values().length * ROW_H + 8 + ROW_H + 8 + BTN_H + 12;
    }

    // ---- inline scale slider ----
    private static class ScaleSlider extends SliderWidget {
        ScaleSlider(int x, int y, int w, int h, float initial) {
            super(x, y, w, h, Text.literal("Масштаб: " + String.format("%.2f", initial)), (initial - 0.5f) / 1.5f);
        }

        @Override
        protected void updateMessage() {
            double scale = 0.5 + value * 1.5;
            setMessage(Text.literal(String.format("Масштаб: %.2f", scale)));
        }

        @Override
        protected void applyValue() {
            float scale = (float)(0.5 + value * 1.5);
            CooldownConfig.get().setHudScale(scale);
        }
    }
}
