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
        HudRenderCallback.EVENT.register(CooldownHudRenderer.INSTANCE);

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cooldownhud.settings",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.cooldownhud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TickTracker.tick();
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new CooldownSettingsScreen(null));
            }
        });
    }
}
