package com.tiggo.chatbridge.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Comando de controle do módulo de combate.
 * Uso:
 *   !combat toggle
 *   !combat debug on/off
 */
public class CombatCommand {

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean handle(String msg) {
        if (!msg.startsWith("!combat")) return false;

        String[] parts = msg.split(" ");
        if (parts.length < 2) {
            send("Uso: !combat <debug on/off | toggle>");
            return true;
        }

        switch (parts[1].toLowerCase()) {
            case "toggle" -> {
                CombatSystem.toggle();
                send("Combate: " + (CombatSystem.CONFIG.enabled ? "ativo" : "inativo"));
            }
            case "debug" -> {
                if (parts.length >= 3) {
                    boolean on = parts[2].equalsIgnoreCase("on");
                    CombatSystem.setDebug(on);
                } else {
                    send("Uso: !combat debug on/off");
                }
            }
            default -> send("Comando inválido.");
        }

        return true;
    }

    private static void send(String text) {
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("[Combat] " + text), false);
    }
}
