package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.core.MoodService;
import net.minecraft.client.Minecraft;

public class ComeCommand implements Command {
    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final MoodService mood;
    private final com.tiggo.trbaritonechatbridge.util.Delay delay;

    public ComeCommand(BaritoneService baritone, DialogueService dialogue, com.tiggo.trbaritonechatbridge.util.Delay delay, MoodService mood) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
        this.mood = mood;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        if (!body.equalsIgnoreCase("vir")) return false;

        MoodService.Mood m = mood.getMood(mc.player);
        String raw = switch (m) {
            case HAPPY -> dialogue.pickOne(TrChatConfig.MSG_COME_HAPPY.get());
            case SAD -> dialogue.pickOne(TrChatConfig.MSG_COME_SAD.get());
            default -> dialogue.pickOne(TrChatConfig.MSG_COME_NEUTRAL.get());
        };
        String msg = dialogue.inject(raw, "owner", TrChatConfig.OWNER_NAME.get());
        delay.sendThenRun(mc, msg, () -> baritone.followPlayer(TrChatConfig.OWNER_NAME.get()));
        return true;
    }
}
