package com.tiggo.trbaritonechatbridge.util;

import net.minecraft.client.Minecraft;

import java.util.concurrent.ThreadLocalRandom;

/** Envia a fala (se houver) e, após 250–2000ms, executa a ação (se houver) no thread do jogo. */
public class Delay {

    public void sendThenRun(Minecraft mc, String line, Runnable afterDelay) {
        if (line != null && !line.isBlank()) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.connection.sendChat(line);
                }
            });
        }
        long delay = ThreadLocalRandom.current().nextLong(250, 2001);
        new Thread(() -> {
            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
            if (afterDelay != null) mc.execute(afterDelay);
        }, "tr_chatbridge_delay").start();
    }
}
