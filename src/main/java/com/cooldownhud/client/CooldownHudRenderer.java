package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CooldownHudRenderer implements HudRenderCallback {

    public static final CooldownHudRenderer INSTANCE = new CooldownHudRenderer();

    // Drag state
    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;
    private float absX = -1, absY = -1;

    // Layout (unscaled)
    private static final int ROW_H     = 20;
    private static final int ICON_SIZE = 16;
    private static final int PAD_X     = 8;
    private static final int PAD_Y     = 6;
    private static final int GAP       = 4;
    private static final int BAR_W     = 50;
    private static final int BAR_H     = 2;
    private static final int TITLE_H   = 12;
    private static final int RADIUS    = 3;

    private static final int COL_TITLE   = 0xFFCCCCDD;
    private static final int COL_NAME    = 0xFFEEEEFF;
    private static final int COL_TIME    = 0xFF9999BB;
    private static final int COL_BAR_BG  = 0xFF1A1A2A;
    private static final int COL_BAR_RED = 0xFFFF3344;
    private static final int COL_BAR_ORG = 0xFFFF9922;

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter ticker) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        if (mc.currentScreen != null) return;
        renderPanel(ctx, mc);
    }

    public void renderInScreen(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        renderPanel(ctx, mc);
    }

    private void renderPanel(DrawContext ctx, MinecraftClient mc) {
        List<RowData> rows = buildRows(mc);
        if (rows.isEmpty()) return;

        CooldownConfig cfg = CooldownConfig.get();
        float scale = cfg.getHudScale();
        TextRenderer tr = mc.textRenderer;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int panelW = computePanelWidth(tr, rows);
        int panelH = computePanelHeight(rows.size());

        if (absX < 0) {
            absX = cfg.getHudX() * sw;
            absY = cfg.getHudY() * sh;
        }

        absX = Math.max(0, Math.min(sw - panelW * scale, absX));
        absY = Math.max(0, Math.min(sh - panelH * scale, absY));

        int x = (int) absX;
        int y = (int) absY;

        int accent = cfg.accentColor(0xFF);

        // Draw scaled background, text, bars
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-x, -y, 0);

        drawRoundedRect(ctx, x, y, panelW, panelH, RADIUS, cfg.bgColor());
        drawRoundedBorder(ctx, x, y, panelW, panelH, RADIUS, (accent & 0x00FFFFFF) | 0x88000000);

        ctx.drawCenteredTextWithShadow(tr, "Cooldowns", x + panelW / 2, y + PAD_Y, COL_TITLE);
        ctx.fill(x + PAD_X, y + PAD_Y + TITLE_H - 1,
                 x + panelW - PAD_X, y + PAD_Y + TITLE_H,
                 (accent & 0x00FFFFFF) | 0x55000000);

        int rowY = y + PAD_Y + TITLE_H + 2;
        for (RowData row : rows) {
            int textX = x + PAD_X + ICON_SIZE + GAP;
            int textY = rowY + 2;

            ctx.drawText(tr, row.name, textX, textY, COL_NAME, false);

            int timeW = tr.getWidth(row.time);
            ctx.drawText(tr, row.time, x + panelW - PAD_X - timeW, textY, COL_TIME, false);

            if (cfg.isShowBars()) {
                int barY = textY + tr.fontHeight + 1;
                ctx.fill(textX, barY, textX + BAR_W, barY + BAR_H, COL_BAR_BG);
                int fill = (int)(BAR_W * row.progress);
                if (fill > 0) {
                    int barCol = row.progress > 0.66f ? COL_BAR_RED
                               : row.progress > 0.33f ? COL_BAR_ORG
                               : accent;
                    ctx.fill(textX, barY, textX + fill, barY + BAR_H, barCol);
                }
            }
            rowY += ROW_H;
        }

        ctx.getMatrices().pop();

        // Draw item icons outside matrix transform to avoid blur.
        // Compute scaled screen position manually.
        rowY = y + PAD_Y + TITLE_H + 2;
        for (RowData row : rows) {
            int iconLocalX = PAD_X;
            int iconLocalY = rowY - y + (ROW_H - ICON_SIZE) / 2;
            int screenX = (int)(x + iconLocalX * scale);
            int screenY = (int)(y + iconLocalY * scale);
            ctx.drawItem(row.stack, screenX, screenY);
            rowY += ROW_H;
        }

        cfg.setHudX(absX / sw);
        cfg.setHudY(absY / sh);
    }

    // ---- rounded rect ----

    static void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        ctx.fill(x + r, y,         x + w - r, y + h,         color);
        ctx.fill(x,     y + r,     x + r,     y + h - r,     color);
        ctx.fill(x+w-r, y + r,     x + w,     y + h - r,     color);
    }

    static void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        ctx.fill(x + r,     y,         x + w - r,     y + 1,         color);
        ctx.fill(x + r,     y + h - 1, x + w - r,     y + h,         color);
        ctx.fill(x,         y + r,     x + 1,         y + h - r,     color);
        ctx.fill(x + w - 1, y + r,     x + w,         y + h - r,     color);
        // corner pixels
        ctx.fill(x + r - 1,     y + 1,     x + r,         y + 2,         color);
        ctx.fill(x + w - r,     y + 1,     x + w - r + 1, y + 2,         color);
        ctx.fill(x + r - 1,     y + h - 2, x + r,         y + h - 1,     color);
        ctx.fill(x + w - r,     y + h - 2, x + w - r + 1, y + h - 1,     color);
    }

    static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,         y,         x + w,     y + 1,     col);
        ctx.fill(x,         y + h - 1, x + w,     y + h,     col);
        ctx.fill(x,         y,         x + 1,     y + h,     col);
        ctx.fill(x + w - 1, y,         x + w,     y + h,     col);
    }

    int computePanelWidth(TextRenderer tr, List<RowData> rows) {
        int max = tr.getWidth("Cooldowns") + PAD_X * 2;
        for (RowData r : rows) {
            int w = PAD_X + ICON_SIZE + GAP + tr.getWidth(r.name) + 8 + tr.getWidth(r.time) + PAD_X;
            if (w > max) max = w;
        }
        return Math.max(max, 120);
    }

    int computePanelHeight(int rowCount) {
        return PAD_Y + TITLE_H + rowCount * ROW_H + PAD_Y;
    }

    List<RowData> buildRows(MinecraftClient mc) {
        CooldownConfig cfg = CooldownConfig.get();
        List<RowData> rows = new ArrayList<>();
        for (TrackedItem ti : TrackedItem.values()) {
            if (!cfg.isEnabled(ti)) continue;
            if (!CooldownHelper.hasCooldown(ti.item)) continue;
            int ticks = CooldownHelper.getTicksRemaining(ti.item);
            float progress = CooldownHelper.getProgress(ti.item);
            rows.add(new RowData(new ItemStack(ti.item), ti.displayName,
                    CooldownHelper.formatTicks(ticks), progress));
        }
        return rows;
    }

    // ---- drag API ----

    public void onMousePress(double mx, double my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || absX < 0) return;
        List<RowData> rows = buildRows(mc);
        float scale = CooldownConfig.get().getHudScale();
        int panelW = computePanelWidth(mc.textRenderer,
                rows.isEmpty() ? List.of(new RowData(ItemStack.EMPTY, "X", "0s", 0f)) : rows);
        int panelH = computePanelHeight(Math.max(1, rows.size()));
        if (mx >= absX && mx <= absX + panelW * scale &&
                my >= absY && my <= absY + panelH * scale) {
            dragging = true;
            dragOffsetX = (float)(mx - absX);
            dragOffsetY = (float)(my - absY);
        }
    }

    public void onMouseRelease() {
        if (dragging) { dragging = false; CooldownConfig.get().save(); }
    }

    public void onMouseDrag(double mx, double my) {
        if (!dragging) return;
        absX = (float)(mx - dragOffsetX);
        absY = (float)(my - dragOffsetY);
    }

    public boolean isDragging() { return dragging; }

    record RowData(ItemStack stack, String name, String time, float progress) {}
}
