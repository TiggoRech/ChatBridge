package com.tiggo.chatbridge.core;

import com.tiggo.chatbridge.baritone.BaritoneService;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {
    private final List<Command> commands = new ArrayList<>();
    private final BaritoneService baritone;

    public CommandRegistry(BaritoneService baritone) {
        this.baritone = baritone;
    }

    public void register(Command cmd) {
        commands.add(cmd);
    }

    public void executeIfMatched(Minecraft mc, String body) {
        for (Command c : commands) {
            if (c.tryExecute(mc, body)) return;
        }
        baritone.infoClient("[ChatBridge] Comando desconhecido: " + body);
    }
}
