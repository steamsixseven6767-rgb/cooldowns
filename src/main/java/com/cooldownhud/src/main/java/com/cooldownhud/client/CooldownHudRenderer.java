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

    // Dragging state
    private boolean dragging = false;
    private float dragOffsetX, dragOffsetY;
    // absolute pixel position (computed from normalized each frame if not dragging)
    private float absX = -1, absY = -1;

    // Design constants
    private static final int ROW_H       = 22;
    private static final int ICON_SIZE   = 16;
    private static final int PAD_X       = 10;
    private static final int PAD_Y       = 8;
    private static final int GAP         = 6;   // gap between icon and text
    private static final int BAR_W       = 60;
    private static final int BAR_H       = 3;
    private static final int TITLE_H     = 14;

    // Colors
    private static final int COL_BG       = 0xCC0D0D17;
    private static final int COL_BORDER   = 0x55FFFFFF;
    private static final int COL_TITLE    = 0xFFCCCCCC;
    private static final int COL_NAME     = 0xFFFFFFFF;
    private static final int COL_TIME     = 0xFFAAAAAA;
    private static final int COL_BAR_BG   = 0xFF222233;
    private static final int COL_BAR_RED  = 0xFFFF4455;
    private static final int COL_BAR_ORG  = 0xFFFFAA22;
    private static final int COL_BAR_GRN  = 0xFF44FF88;

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter ticker) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        if (mc.currentScreen != null) return; // hide while settings open (settings draws itself)

        List<RowData> rows = buildRows();
        if (rows.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        CooldownConfig cfg = CooldownConfig.get();

        // Compute panel size
        int panelW = computePanelWidth(tr, rows);
        int panelH = PAD_Y + TITLE_H + rows.size() * ROW_H + PAD_Y;

        // First frame: init abs position from normalized
        if (absX < 0) {
            absX = cfg.getHudX() * sw;
            absY = cfg.getHudY() * sh;
        }

        // Clamp to screen
        absX = Math.max(0, Math.min(sw - panelW, absX));
        absY = Math.max(0, Math.min(sh - panelH, absY));

        int x = (int) absX;
        int y = (int) absY;

        // Background
        ctx.fill(x, y, x + panelW, y + panelH, COL_BG);
        // Border (1px)
        ctx.fill(x,              y,              x + panelW, y + 1,          COL_BORDER);
        ctx.fill(x,              y + panelH - 1, x + panelW, y + panelH,     COL_BORDER);
        ctx.fill(x,              y,              x + 1,      y + panelH,     COL_BORDER);
        ctx.fill(x + panelW - 1, y,              x + panelW, y + panelH,     COL_BORDER);

        // Title
        String title = "Cooldowns";
        int titleX = x + (panelW - tr.getWidth(title)) / 2;
        ctx.drawText(tr, title, titleX, y + PAD_Y, COL_TITLE, false);

        // Separator
        ctx.fill(x + PAD_X, y + PAD_Y + TITLE_H - 2, x + panelW - PAD_X, y + PAD_Y + TITLE_H - 1, COL_BORDER);

        int rowY = y + PAD_Y + TITLE_H + 2;
        for (RowData row : rows) {
            drawRow(ctx, tr, row, x + PAD_X, rowY, panelW - PAD_X * 2);
            rowY += ROW_H;
        }

        // Save normalized position
        cfg.setHudX(absX / sw);
        cfg.setHudY(absY / sh);
    }

    private void drawRow(DrawContext ctx, TextRenderer tr, RowData row, int x, int y, int availW) {
        // Item icon (16x16, centered vertically in ROW_H)
        int iconY = y + (ROW_H - ICON_SIZE) / 2;
        ctx.drawItem(row.stack, x, iconY);

        int textX = x + ICON_SIZE + GAP;
        int textY = y + 2;

        // Name
        ctx.drawText(tr, row.name, textX, textY, COL_NAME, false);

        // Time
        int timeW = tr.getWidth(row.time);
        ctx.drawText(tr, row.time, x + availW - timeW, textY, COL_TIME, false);

        // Bar
        int barY  = textY + tr.fontHeight + 2;
        int barX  = textX;
        ctx.fill(barX, barY, barX + BAR_W, barY + BAR_H, COL_BAR_BG);

        int fill  = (int)(BAR_W * row.progress);
        int barColor = row.progress > 0.66f ? COL_BAR_RED
                     : row.progress > 0.33f ? COL_BAR_ORG
                     : COL_BAR_GRN;
        if (fill > 0) ctx.fill(barX, barY, barX + fill, barY + BAR_H, barColor);
    }

    private int computePanelWidth(TextRenderer tr, List<RowData> rows) {
        int maxNameW = 0;
        for (RowData r : rows) {
            int w = ICON_SIZE + GAP + tr.getWidth(r.name) + 10 + tr.getWidth(r.time);
            if (w > maxNameW) maxNameW = w;
        }
        return PAD_X + maxNameW + PAD_X;
    }

    private List<RowData> buildRows() {
        MinecraftClient mc = MinecraftClient.getInstance();
        CooldownConfig cfg = CooldownConfig.get();
        List<RowData> rows = new ArrayList<>();

        for (TrackedItem ti : TrackedItem.values()) {
            if (!cfg.isEnabled(ti)) continue;

            int ticks = CooldownHelper.getTicksRemaining(ti.item);
            if (ticks <= 0) continue;

            float progress = CooldownHelper.getProgress(ti.item);
            rows.add(new RowData(
                new ItemStack(ti.item),
                ti.displayName,
                CooldownHelper.formatTicks(ticks),
                progress
            ));
        }
        return rows;
    }

    // ---- drag support (called from CooldownHudClient mouse handlers) ----

    public void onMousePress(double mx, double my) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        List<RowData> rows = buildRows();
        if (rows.isEmpty()) return;

        TextRenderer tr = mc.textRenderer;
        int panelW = computePanelWidth(tr, rows);
        int panelH = PAD_Y + TITLE_H + rows.size() * ROW_H + PAD_Y;

        if (mx >= absX && mx <= absX + panelW && my >= absY && my <= absY + panelH) {
            dragging = true;
            dragOffsetX = (float)(mx - absX);
            dragOffsetY = (float)(my - absY);
        }
    }

    public void onMouseRelease() {
        if (dragging) {
            dragging = false;
            CooldownConfig.get().save();
        }
    }

    public void onMouseDrag(double mx, double my) {
        if (!dragging) return;
        absX = (float)(mx - dragOffsetX);
        absY = (float)(my - dragOffsetY);
    }

    public static final CooldownHudRenderer INSTANCE = new CooldownHudRenderer();

    record RowData(ItemStack stack, String name, String time, float progress) {}
}
