package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.util.Delay;
import net.minecraft.client.Minecraft;

public class SayCommand implements Command {
    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;

    public SayCommand(BaritoneService baritone, DialogueService dialogue, Delay delay) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        String lower = body.toLowerCase();
        if (!lower.startsWith("diga ")) return false;

        String say = body.substring("diga".length()).trim();
        if (say.isEmpty()) return true;

        String ack = dialogue.pickOne(TrChatConfig.MSG_TALK.get());
        delay.sendThenRun(mc, ack, () -> baritone.sendChat(say));
        return true;
    }
}
