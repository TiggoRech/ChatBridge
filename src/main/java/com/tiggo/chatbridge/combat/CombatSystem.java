package com.tiggo.chatbridge.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Núcleo do módulo de combate:
 * - Singleton de CONFIG/PROCESS
 * - Registro de tick do cliente (FORGE bus)
 * - Toggle e debug (!combat ...)
 * - Abre a tela de configuração
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatSystem {

    /** Configuração global (usada na UI e no processo). */
    public static final CombatConfig CONFIG = new CombatConfig();

    /** Processo de combate “real” (mira/anda/ataca). */
    private static final CombatProcess PROCESS = new CombatProcess();

    private static final Minecraft mc = Minecraft.getInstance();

    /** Flags de controle rápido via comando. */
    private static boolean debug = false; // logs no chat

    private CombatSystem() {}

    /** Exposto para outros pontos (ex.: CommandsClient cancelar combate). */
    public static CombatProcess getProcess() {
        return PROCESS;
    }

    /** Liga/desliga o modo combate. (espelha em CONFIG.enabled) */
    public static void toggle() {
        CONFIG.enabled = !CONFIG.enabled;
        log("Combate " + (CONFIG.enabled ? "ativado" : "desativado"));
    }

    /** Liga/desliga mensagens de debug no chat. */
    public static void setDebug(boolean on) {
        debug = on;
        log("Debug " + (on ? "ativado" : "desativado"));
    }

    public static boolean isDebug() { return debug; }

    /** Mensagem simples no chat do cliente. */
    public static void log(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[Combat] " + msg), false);
        }
    }

    /** Tick do cliente: consome keybinds e roda o processo. */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        // keybind da tela
        CombatKeybinds.clientTick();

        // loop de combate (o próprio PROCESS verifica CONFIG.enabled)
        PROCESS.tick();

        // (Opcional) Exemplo mínimo de auto-ataque quando o PROCESS estiver inativo:
        // Mantive esse trecho bem simples só para diagnósticos quando alguém comentar o PROCESS inteiro.
        // Desative-o se preferir somente o PROCESS completo.
        if (CONFIG.enabled && isDebug()) {
            LocalPlayer self = mc.player;
            List<LivingEntity> nearby = mc.level.getEntitiesOfClass(
                    LivingEntity.class,
                    self.getBoundingBox().inflate(6.0),
                    e -> e != self && e.isAlive()
            );
            List<LivingEntity> targets = nearby.stream()
                    .filter(e -> {
                        if (e instanceof Player p && !p.isCreative()) return true;
                        if (e instanceof Mob m && !(m instanceof Creeper)) return true;
                        return false;
                    })
                    .collect(Collectors.toList());
            if (!targets.isEmpty()) {
                LivingEntity t = targets.stream()
                        .min(Comparator.comparingDouble(e -> e.distanceTo(self)))
                        .orElse(null);
                if (t != null) {
                    log("DEBUG quick-attack em " + t.getName().getString());
                }
            }
        }
    }

    /** Abre a interface de configuração. */
    public static void openConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new CombatConfigScreen(mc.screen));
    }
}
