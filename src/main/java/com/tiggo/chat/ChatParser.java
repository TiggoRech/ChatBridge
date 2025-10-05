package com.tiggo.trbaritonechatbridge.chat;

import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.Locale;
import java.util.UUID;

/** Valida remetente e extrai o corpo do comando (Bot + Prefixo). */
public class ChatParser {

    /** Checa se a mensagem é do dono autorizado (ou se config permite qualquer um). */
    public boolean isOwnerMessage(Minecraft mc, ClientChatReceivedEvent event) {
        if (event.isSystem()) return false;

        final String rawShown = event.getMessage().getString().trim();
        if (rawShown.isEmpty()) return false;

        final String ownerName = TrChatConfig.OWNER_NAME.get().trim();
        final String ownerUuidStr = TrChatConfig.OWNER_UUID.get().trim();
        final UUID senderUuid = event.getSender(); // pode ser null

        // 1) UUID configurado e presente
        if (!ownerUuidStr.isEmpty()) {
            try {
                UUID ownerUuid = UUID.fromString(ownerUuidStr);
                if (senderUuid != null && senderUuid.equals(ownerUuid)) return true;
            } catch (IllegalArgumentException ignored) {}
        }

        // 2) Nome via UUID -> Player
        if (senderUuid != null && mc.level != null) {
            Player p = mc.level.getPlayerByUUID(senderUuid);
            if (p != null && p.getGameProfile() != null
                    && ownerName.equalsIgnoreCase(p.getGameProfile().getName())) return true;
        }

        // 3) Texto visível do chat
        if (rawShown.startsWith("<" + ownerName + ">")
                || rawShown.startsWith(ownerName + ":")
                || rawShown.startsWith(ownerName + " »")) {
            return true;
        }

        // 4) Fallback
        return TrChatConfig.ALLOW_ANY_SENDER.get();
    }

    /** Extrai o corpo do comando: "BotName !cmd ..." -> retorna "cmd ...". */
    public String extractCommandBody(ClientChatReceivedEvent event) {
        final String rawShown = event.getMessage().getString().trim();
        final String ownerName = TrChatConfig.OWNER_NAME.get();
        final String botName = TrChatConfig.BOT_NAME.get();
        final String prefix = TrChatConfig.CMD_PREFIX.get();

        String plain = stripChatDecoration(rawShown, ownerName);
        if (plain.isEmpty()) return null;

        // Checa "BotName "
        if (!plain.toLowerCase(Locale.ROOT).startsWith((botName + " ").toLowerCase(Locale.ROOT))) return null;

        String afterName = plain.substring(botName.length()).trim();
        if (!afterName.startsWith(prefix)) return null;

        String body = afterName.substring(prefix.length()).trim();
        return body.isEmpty() ? null : body;
    }

    /** Remove "<Nome> " / "Nome: " / "Nome » " do início do chat visível. */
    private String stripChatDecoration(String shown, String ownerName) {
        String s = shown;
        if (s.startsWith("<")) {
            int idx = s.indexOf('>');
            if (idx >= 0 && idx + 1 < s.length()) s = s.substring(idx + 1).trim();
        } else {
            String colon = ownerName + ":";
            String arrow  = ownerName + " »";
            if (s.startsWith(colon)) s = s.substring(colon.length()).trim();
            else if (s.startsWith(arrow)) s = s.substring(arrow.length()).trim();
        }
        return s;
    }
}
