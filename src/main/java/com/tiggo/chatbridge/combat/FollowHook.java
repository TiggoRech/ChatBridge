package com.tiggo.chatbridge.combat;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber
public class FollowHook {
    private static final AtomicReference<UUID> currentFollowed = new AtomicReference<>(null);

    /** chame isso quando você disparar "#follow player <nome>" pelo seu addon */
    public static void setFollowed(UUID playerUuid) { currentFollowed.set(playerUuid); }
    public static Optional<UUID> getFollowed() { return Optional.ofNullable(currentFollowed.get()); }
    public static void clearFollowed() { currentFollowed.set(null); }

    // Opcional: capturar quando o usuário digitar manualmente no chat
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage();
        // exemplo simples: "#follow player NOME" -> aqui você poderia resolver o UUID do NOME
        // e chamar setFollowed(uuidDoNome). Mantido como placeholder, pois resolver UUID varia.
    }
}
