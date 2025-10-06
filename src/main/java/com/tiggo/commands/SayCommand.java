package com.tiggo.chatbridge.commands;

import com.tiggo.chatbridge.baritone.BaritoneService;
import com.tiggo.chatbridge.chat.DialogueService;
import com.tiggo.chatbridge.config.TrChatConfig;
import com.tiggo.chatbridge.core.Command;
import com.tiggo.chatbridge.util.Delay;
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
