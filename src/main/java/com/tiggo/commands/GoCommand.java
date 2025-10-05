package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.util.Delay;
import net.minecraft.client.Minecraft;

public class GoCommand implements Command {
    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;

    public GoCommand(BaritoneService baritone, DialogueService dialogue, Delay delay) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        String lower = body.toLowerCase();
        if (!lower.startsWith("ir ")) return false;

        String[] p = body.split("\\s+");
        if (p.length != 4) {
            baritone.infoClient("[ChatBridge] Uso: " + TrChatConfig.BOT_NAME.get() + " " +
                    TrChatConfig.CMD_PREFIX.get() + "ir <x> <y> <z>");
            return true;
        }

        String msg = dialogue.injectXYZ(
                dialogue.pickOne(TrChatConfig.MSG_GO.get()),
                p[1], p[2], p[3]
        );
        delay.sendThenRun(mc, msg, () -> baritone.pathTo(p[1], p[2], p[3]));
        return true;
    }
}
