package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.util.Delay;
import net.minecraft.client.Minecraft;

public class FollowCommand implements Command {

    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;

    public FollowCommand(BaritoneService baritone, DialogueService dialogue, Delay delay) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        String lower = body.toLowerCase();
        if (!lower.equals("seguir") && !lower.startsWith("seguir ")) return false;

        String target;
        boolean explicit = false;
        String[] parts = body.split("\\s+", 2);
        if (parts.length >= 2 && !parts[1].isBlank()) {
            target = parts[1].trim();
            explicit = true;
        } else {
            target = baritone.nearestPlayerName();
            if (target == null) {
                baritone.infoClient("[ChatBridge] Nenhum jogador por perto para seguir.");
                return true;
            }
        }

        String raw = explicit
                ? dialogue.pickOne(TrChatConfig.MSG_FOLLOW_OTHER.get())
                : dialogue.pickOne(TrChatConfig.MSG_FOLLOW_NEAREST.get());

        String msg = dialogue.inject(raw, "target", target);
        String tgt = target;
        delay.sendThenRun(mc, msg, () -> baritone.followPlayer(tgt));
        return true;
    }
}
