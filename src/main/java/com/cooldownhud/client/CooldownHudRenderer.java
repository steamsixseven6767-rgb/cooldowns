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

    // Layout constants (at scale = 1.0)
    private static final int ROW_H     = 20;
    private static final int ICON_SIZE = 16;
    private static final int PAD_X     = 8;
    private static final int PAD_Y     = 6;
    private static final int GAP       = 5;
    private static final int BAR_W     = 55;
    private static final int BAR_H     = 2;
    private static final int TITLE_H   = 12;

    // Static colors
    private static final int COL_BORDER  = 0x55FFFFFF;
    private static final int COL_TITLE   = 0xFFCCCCCC;
    private static final int COL_NAME    = 0xFFFFFFFF;
    private static final int COL_TIME    = 0xFFAAAAAA;
    private static final int COL_BAR_BG  = 0xFF222233;
    private static final int COL_BAR_RED = 0xFFFF4455;
    private static final int COL_BAR_ORG = 0xFFFFAA22;

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter ticker) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden) return;
        if (mc.currentScreen != null) return;

        List<RowData> rows = buildRows();
        if (rows.isEmpty()) return;

        CooldownConfig cfg = CooldownConfig.get();
        float scale = cfg.getHudScale();
        TextRenderer tr = mc.textRenderer;

        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int panelW = computePanelWidth(tr, rows);
        int panelH = PAD_Y + TITLE_H + rows.size() * ROW_H + PAD_Y;

        // Init from config on first frame
        if (absX < 0) {
            absX = cfg.getHudX() * sw;
            absY = cfg.getHudY() * sh;
        }

        // Clamp so panel stays on screen considering scale
        absX = Math.max(0, Math.min(sw - panelW * scale, absX));
        absY = Math.max(0, Math.min(sh - panelH * scale, absY));

        int x = (int) absX;
        int y = (int) absY;

        // Apply scale transformation around the panel's top-left corner
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-x, -y, 0);

        // Background
        ctx.fill(x, y, x + panelW, y + panelH, cfg.bgColor());

        // Border
        ctx.fill(x,              y,              x + panelW,     y + 1,          COL_BORDER);
        ctx.fill(x,              y + panelH - 1, x + panelW,     y + panelH,     COL_BORDER);
        ctx.fill(x,              y,              x + 1,          y + panelH,     COL_BORDER);
        ctx.fill(x + panelW - 1, y,              x + panelW,     y + panelH,     COL_BORDER);

        // Title
        String title = "Cooldowns";
        ctx.drawText(tr, title, x + (panelW - tr.getWidth(title)) / 2, y + PAD_Y, COL_TITLE, false);
        ctx.fill(x + PAD_X, y + PAD_Y + TITLE_H - 2, x + panelW - PAD_X, y + PAD_Y + TITLE_H - 1, COL_BORDER);

        int rowY = y + PAD_Y + TITLE_H + 2;
        for (RowData row : rows) {
            drawRow(ctx, tr, row, x + PAD_X, rowY, panelW - PAD_X * 2, cfg);
            rowY += ROW_H;
        }

        ctx.getMatrices().pop();

        // Save normalized position
        cfg.setHudX(absX / sw);
        cfg.setHudY(absY / sh);
    }

    private void drawRow(DrawContext ctx, TextRenderer tr, RowData row,
                         int x, int y, int availW, CooldownConfig cfg) {
        int iconY = y + (ROW_H - ICON_SIZE) / 2;
        ctx.drawItem(row.stack, x, iconY);

        int textX = x + ICON_SIZE + GAP;
        int textY = y + 2;

        ctx.drawText(tr, row.name, textX, textY, COL_NAME, false);

        int timeW = tr.getWidth(row.time);
        ctx.drawText(tr, row.time, x + availW - timeW, textY, COL_TIME, false);

        if (cfg.isShowBars()) {
            int barY = textY + tr.fontHeight + 1;
            ctx.fill(textX, barY, textX + BAR_W, barY + BAR_H, COL_BAR_BG);

            int fill = (int)(BAR_W * row.progress);
            if (fill > 0) {
                int barColor = row.progress > 0.66f ? COL_BAR_RED
                             : row.progress > 0.33f ? COL_BAR_ORG
                             : cfg.accentColor(0xFF);
                ctx.fill(textX, barY, textX + fill, barY + BAR_H, barColor);
            }
        }
    }

    private int computePanelWidth(TextRenderer tr, List<RowData> rows) {
        int max = tr.getWidth("Cooldowns") + PAD_X * 2;
        for (RowData r : rows) {
            int w = PAD_X + ICON_SIZE + GAP + tr.getWidth(r.name) + 10 + tr.getWidth(r.time) + PAD_X;
            if (w > max) max = w;
        }
        return Math.max(max, 130);
    }

    private List<RowData> buildRows() {
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

    // ---- drag ----

    public void onMousePress(double mx, double my) {
        if (absX < 0) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        List<RowData> rows = buildRows();
        CooldownConfig cfg = CooldownConfig.get();
        float scale = cfg.getHudScale();
        int panelW = computePanelWidth(mc.textRenderer, rows.isEmpty()
                ? List.of(new RowData(ItemStack.EMPTY, "X", "0s", 0f)) : rows);
        int panelH = PAD_Y + TITLE_H + Math.max(1, rows.size()) * ROW_H + PAD_Y;
        float pw = panelW * scale;
        float ph = panelH * scale;
        if (mx >= absX && mx <= absX + pw && my >= absY && my <= absY + ph) {
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

    record RowData(ItemStack stack, String name, String time, float progress) {}
}
