package com.cooldownhud.mixin;

import com.cooldownhud.client.CooldownHudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /**
     * Called when a totem of undying activates (onDeath interception before death).
     * In 1.21 the method is tryUseTotem, which checks offhand/mainhand for totem.
     */
    @Inject(
        method = "tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z",
        at = @At("TAIL")
    )
    private void onTotemUsed(net.minecraft.entity.damage.DamageSource source,
                             org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // totem didn't activate

        // Only track for local player
        LivingEntity self = (LivingEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player == self) {
            CooldownHudRenderer.totemUsed = true;
            CooldownHudRenderer.totemUsedTime = System.currentTimeMillis();
        }
    }
}
