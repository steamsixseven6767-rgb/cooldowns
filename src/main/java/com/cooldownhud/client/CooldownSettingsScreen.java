package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Main settings screen (H).
 * Compact layout with scroll support so it fits small screens.
 * shouldPause() = false → no blur pass → icons render sharp.
 */
public class CooldownSettingsScreen extends Screen {

    private static final int PANEL_W  = 240;
    private static final int ROW_H    = 20;
    private static final int BTN_W    = 76;
    private static final int BTN_H    = 14;
    private static final int ICON_SZ  = 16;
    private static final int PAD      = 8;
    private static final int HEADER_H = 28; // title + sep
    private static final int FOOTER_H = 72; // customize + move + save + spacing

    private static final int COL_PANEL   = 0xEE080812;
    private static final int COL_BORDER  = 0xFF2E2E55;
    private static final int COL_TITLE   = 0xFFDDDDFF;
    private static final int COL_SUB     = 0xFF6666AA;
    private static final int COL_ROW_ALT = 0x12FFFFFF;
    private static final int COL_SEP     = 0x33FFFFFF;

    private final Screen parent;

    // Scroll state
    private int scrollOffset = 0; // in rows
    private int visibleRows;

    public CooldownSettingsScreen(Screen parent) {
        super(Text.literal("CooldownHUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        CooldownConfig cfg = CooldownConfig.get();

        int maxPanelH = height - 8;
        int maxRows = (maxPanelH - HEADER_H - FOOTER_H) / ROW_H;
        visibleRows = Math.min(maxRows, TrackedItem.values().length);
        scrollOffset = Math.min(scrollOffset,
                Math.max(0, TrackedItem.values().length - visibleRows));

        rebuildButtons(cfg);
    }

    private void rebuildButtons(CooldownConfig cfg) {
        clearChildren();

        int listH = visibleRows * ROW_H;
        int panelH = HEADER_H + listH + FOOTER_H;
        int px = getPanelX();
        int py = Math.max(4, (height - panelH) / 2);

        TrackedItem[] items = TrackedItem.values();
        for (int i = 0; i < visibleRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= items.length) break;
            final TrackedItem item = items[idx];
            final int rowY = py + HEADER_H + i * ROW_H;
            addDrawableChild(ButtonWidget.builder(
                    toggleText(cfg.isEnabled(item)),
                    b -> {
                        boolean now = !CooldownConfig.get().isEnabled(item);
                        CooldownConfig.get().setEnabled(item, now);
                        b.setMessage(toggleText(now));
                    }
            ).dimensions(px + PANEL_W - BTN_W - PAD, rowY + 3, BTN_W, BTN_H).build());
        }

        int footerY = py + HEADER_H + listH + 6;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("⚙  Вид панели..."),
                b -> client.setScreen(new CooldownCustomizeScreen(this))
        ).dimensions(px + PAD, footerY, PANEL_W - PAD * 2, BTN_H + 2).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("⤢  Переместить панель"),
                b -> client.setScreen(new CooldownDragScreen(this))
        ).dimensions(px + PAD, footerY + BTN_H + 6, PANEL_W - PAD * 2, BTN_H + 2).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔  Сохранить и закрыть"),
                b -> { CooldownConfig.get().save(); client.setScreen(parent); }
        ).dimensions(px + PAD, footerY + (BTN_H + 6) * 2, PANEL_W - PAD * 2, BTN_H + 2).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizAmount, double vertAmount) {
        int maxScroll = Math.max(0, TrackedItem.values().length - visibleRows);
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll,
                    scrollOffset - (int)Math.signum(vertAmount)));
            rebuildButtons(CooldownConfig.get());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizAmount, vertAmount);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x99000000);

        int listH = visibleRows * ROW_H;
        int panelH = HEADER_H + listH + FOOTER_H;
        int px = getPanelX();
        int py = Math.max(4, (height - panelH) / 2);

        // Shadow
        ctx.fill(px + 3, py + 3, px + PANEL_W + 3, py + panelH + 3, 0x55000000);
        // Panel
        CooldownHudRenderer.drawRoundedRect(ctx, px, py, PANEL_W, panelH, 3, COL_PANEL);
        CooldownHudRenderer.drawRoundedBorder(ctx, px, py, PANEL_W, panelH, 3, COL_BORDER);

        // Title bar
        ctx.fill(px + 1, py + 1, px + PANEL_W - 1, py + HEADER_H, 0x1AFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "CooldownHUD", width / 2, py + 4, COL_TITLE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Настройки", width / 2, py + 14, COL_SUB);
        ctx.fill(px + 6, py + HEADER_H - 1, px + PANEL_W - 6, py + HEADER_H, COL_SEP);

        // Item rows
        TrackedItem[] items = TrackedItem.values();
        for (int i = 0; i < visibleRows; i++) {
            int idx = i + scrollOffset;
            if (idx >= items.length) break;
            TrackedItem ti = items[idx];
            int rowY = py + HEADER_H + i * ROW_H;

            if (i % 2 == 0) ctx.fill(px + 1, rowY, px + PANEL_W - 1, rowY + ROW_H, COL_ROW_ALT);

            ctx.drawItem(new ItemStack(ti.item), px + PAD, rowY + 2);
            ctx.drawText(textRenderer, ti.displayName,
                    px + PAD + ICON_SZ + 4, rowY + 6, 0xFFEEEEFF, false);

            boolean on = CooldownConfig.get().isEnabled(ti);
            int bx = px + PANEL_W - BTN_W - PAD;
            int by = rowY + 3;
            ctx.fill(bx + 1, by + 1, bx + BTN_W - 1, by + BTN_H - 1,
                    on ? 0xCC1A3A1A : 0xCC3A1A1A);
            drawBorder(ctx, bx, by, BTN_W, BTN_H, on ? 0xFF44AA44 : 0xFFAA4444);
        }

        // Scroll hint
        int maxScroll = Math.max(0, items.length - visibleRows);
        if (maxScroll > 0) {
            String hint = (scrollOffset > 0 ? "▲ " : "") +
                          (scrollOffset > 0 && scrollOffset < maxScroll ? "колесо" :
                           scrollOffset == 0 ? "▼ прокрути" : "▲ прокрути") +
                          (scrollOffset < maxScroll ? " ▼" : "");
            ctx.drawCenteredTextWithShadow(textRenderer, hint,
                    width / 2, py + HEADER_H + listH - 1, 0xFF666688);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }

    private int getPanelX() { return (width - PANEL_W) / 2; }

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
