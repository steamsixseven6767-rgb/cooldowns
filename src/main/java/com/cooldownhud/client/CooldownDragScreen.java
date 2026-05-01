package com.cooldownhud.client;

import com.cooldownhud.config.CooldownConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Transparent overlay screen that allows dragging the HUD panel.
 * The HUD renderer still draws because shouldPause=false and hudHidden=false.
 * Mouse events are forwarded to CooldownHudRenderer.
 */
public class CooldownDragScreen extends Screen {

    private final Screen parent;

    public CooldownDragScreen(Screen parent) {
        super(Text.literal("Переместить панель"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // "Готово" button at bottom center
        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔  Готово"),
                b -> {
                    CooldownConfig.get().save();
                    client.setScreen(parent);
                }
        ).dimensions(width / 2 - 60, height - 30, 120, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Very subtle dim so the HUD is clearly visible
        ctx.fill(0, 0, width, height, 0x55000000);

        // Instruction label
        String hint = "Перетащи панель мышью";
        int tw = textRenderer.getWidth(hint);
        ctx.fill(width / 2 - tw / 2 - 4, 8, width / 2 + tw / 2 + 4, 22, 0xAA000000);
        ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, 11, 0xFFFFFF55);

        super.render(ctx, mouseX, mouseY, delta);

        // Draw the HUD panel manually so it's visible while dragging
        // (HudRenderCallback doesn't fire when a screen is open, so we call it directly)
        CooldownHudRenderer.INSTANCE.renderInScreen(ctx, client);
    }

    // In Minecraft 1.21+ Screen uses mouseClicked instead of mousePressed
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            CooldownHudRenderer.INSTANCE.onMousePress(mx, my);
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            CooldownHudRenderer.INSTANCE.onMouseRelease();
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0) {
            CooldownHudRenderer.INSTANCE.onMouseDrag(mx, my);
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
