package com.cooldownhud.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CooldownHudClient implements ClientModInitializer {

    public static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        // Register HUD renderer
        HudRenderCallback.EVENT.register(CooldownHudRenderer.INSTANCE);

        // Keybind: H → open settings
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cooldownhud.settings",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.cooldownhud"
        ));

        // Tick: run TickTracker + check keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TickTracker.tick();

            while (openSettingsKey.wasPressed()) {
                client.setScreen(new CooldownSettingsScreen(null));
            }
        });

        // Mouse drag for HUD panel
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null) return;
            long window = client.getWindow().getHandle();
            double[] mx = {0}, my = {0};
            GLFW.glfwGetCursorPos(window, mx, my);

            double scale = client.getWindow().getScaleFactor();
            double gx = mx[0] / scale;
            double gy = my[0] / scale;

            boolean pressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT)
                              == GLFW.GLFW_PRESS;
            if (pressed) {
                CooldownHudRenderer.INSTANCE.onMousePress(gx, gy);
                CooldownHudRenderer.INSTANCE.onMouseDrag(gx, gy);
            } else {
                CooldownHudRenderer.INSTANCE.onMouseRelease();
            }
        });
    }
}
