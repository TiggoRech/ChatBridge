package com.tiggo.chatbridge.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Keybind para abrir a tela de configuração do combate. */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CombatKeybinds {

    private static final String CAT = "key.categories.chatbridge";
    private static final KeyMapping OPEN =
            new KeyMapping("Abrir configuração de combate", GLFW.GLFW_KEY_RIGHT_BRACKET, CAT);

    private CombatKeybinds() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        e.register(OPEN);
    }

    /** Chamado por CombatSystem a cada tick do cliente. */
    static void clientTick() {
        if (Minecraft.getInstance().player == null) return;
        while (OPEN.consumeClick()) {
            Minecraft.getInstance().setScreen(new CombatConfigScreen(Minecraft.getInstance().screen));
        }
    }
}
