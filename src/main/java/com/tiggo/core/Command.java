package com.tiggo.chatbridge.core;

import net.minecraft.client.Minecraft;

/** Contrato simples: se reconhece o corpo, executa e retorna true. */
public interface Command {
    boolean tryExecute(Minecraft mc, String body);
}
