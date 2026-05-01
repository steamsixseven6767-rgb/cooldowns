package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Main settings screen (H).
 * shouldPause() = false → no Minecraft blur pass → icons render sharp.
 */
public class CooldownSettingsScreen extends Screen {

    private static final int PANEL_W = 260;
    private static final int ROW_H   = 22;
    private static final int BTN_W   = 88;
    private static final int BTN_H   = 16;
    private static final int ICON_SZ = 16;
    private static final int PAD     = 10;

    private static final int COL_PANEL   = 0xEE080812;
    private static final int COL_BORDER  = 0xFF2E2E55;
    private static final int COL_TITLE   = 0xFFDDDDFF;
    private static final int COL_SUB     = 0xFF6666AA;
    private static final int COL_ROW_ALT = 0x12FFFFFF;
    private static final int COL_SEP     = 0x33FFFFFF;

    private final Screen parent;

    public CooldownSettingsScreen(Screen parent) {
        super(Text.literal("CooldownHUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CooldownConfig cfg = CooldownConfig.get();
        int py = getPanelY();

        int y = py + 34;
        for (TrackedItem ti : TrackedItem.values()) {
            final TrackedItem item = ti;
            addDrawableChild(ButtonWidget.builder(
                    toggleText(cfg.isEnabled(ti)),
                    b -> {
                        boolean now = !CooldownConfig.get().isEnabled(item);
                        CooldownConfig.get().setEnabled(item, now);
                        b.setMessage(toggleText(now));
                    }
            ).dimensions(getPanelX() + PANEL_W - BTN_W - PAD, y + 3, BTN_W, BTN_H).build());
            y += ROW_H;
        }

        y += 8;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("⚙  Вид панели..."),
                b -> client.setScreen(new CooldownCustomizeScreen(this))
        ).dimensions(getPanelX() + PAD, y, PANEL_W - PAD * 2, BTN_H + 2).build());

        y += BTN_H + 6;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔  Сохранить и закрыть"),
                b -> { CooldownConfig.get().save(); client.setScreen(parent); }
        ).dimensions(getPanelX() + PAD, y, PANEL_W - PAD * 2, BTN_H + 2).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip renderBackground() — that call triggers the blur pass.
        // We do our own dim instead.
        ctx.fill(0, 0, width, height, 0x99000000);

        int px = getPanelX();
        int py = getPanelY();
        int ph = getPanelH();

        // Shadow
        ctx.fill(px + 3, py + 3, px + PANEL_W + 3, py + ph + 3, 0x55000000);
        // Panel
        ctx.fill(px, py, px + PANEL_W, py + ph, COL_PANEL);
        drawBorder(ctx, px, py, PANEL_W, ph, COL_BORDER);

        // Title bar
        ctx.fill(px + 1, py + 1, px + PANEL_W - 1, py + 27, 0x1AFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 5, COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Настройки предметов", width / 2, py + 16, COL_SUB);
        ctx.fill(px + 6, py + 28, px + PANEL_W - 6, py + 29, COL_SEP);

        // Item rows
        TrackedItem[] items = TrackedItem.values();
        int y = py + 34;
        for (int i = 0; i < items.length; i++) {
            TrackedItem ti = items[i];
            if (i % 2 == 0) ctx.fill(px + 1, y, px + PANEL_W - 1, y + ROW_H, COL_ROW_ALT);

            // Icon (no blur because shouldPause=false means no blur shader active)
            ctx.drawItem(new ItemStack(ti.item), px + PAD, y + 3);
            ctx.drawText(textRenderer, ti.displayName, px + PAD + ICON_SZ + 4, y + 7, 0xFFEEEEFF, false);

            // Colored toggle bg overlay (drawn behind the vanilla button)
            boolean on = CooldownConfig.get().isEnabled(ti);
            int bx = px + PANEL_W - BTN_W - PAD;
            int by = y + 3;
            ctx.fill(bx + 1, by + 1, bx + BTN_W - 1, by + BTN_H - 1,
                    on ? 0xCC1A3A1A : 0xCC3A1A1A);
            drawBorder(ctx, bx, by, BTN_W, BTN_H,
                    on ? 0xFF44AA44 : 0xFFAA4444);

            y += ROW_H;
        }

        super.render(ctx, mouseX, mouseY, delta); // draws buttons on top
    }

    @Override
    public boolean shouldPause() {
        // false = game keeps ticking = Minecraft does NOT activate the blur shader
        return false;
    }

    // ---- helpers ----

    private int getPanelX() { return (width - PANEL_W) / 2; }
    private int getPanelY() { return Math.max(2, (height - getPanelH()) / 2); }
    private int getPanelH() {
        return 34 + TrackedItem.values().length * ROW_H
                + 8 + (BTN_H + 2)   // customize btn
                + 6 + (BTN_H + 2)   // save btn
                + PAD;
    }

    static Text toggleText(boolean on) {
        return on ? Text.literal("✔ Вкл") : Text.literal("✘ Выкл");
    }

    static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,         y,         x + w,     y + 1,     col);
        ctx.fill(x,         y + h - 1, x + w,     y + h,     col);
        ctx.fill(x,         y,         x + 1,     y + h,     col);
        ctx.fill(x + w - 1, y,         x + w,     y + h,     col);
    }
}
