package com.tiggo.chatbridge.commands;

import com.tiggo.chatbridge.baritone.BaritoneService;
import com.tiggo.chatbridge.chat.DialogueService;
import com.tiggo.chatbridge.config.TrChatConfig;
import com.tiggo.chatbridge.core.Command;
import com.tiggo.chatbridge.core.MoodService;
import com.tiggo.chatbridge.util.Delay;
import net.minecraft.client.Minecraft;

import java.util.Random;

public class MineCommand implements Command {
    private static final Random RNG = new Random();

    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;
    private final MoodService mood;

    public MineCommand(BaritoneService baritone, DialogueService dialogue, Delay delay, MoodService mood) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
        this.mood = mood;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        String lower = body.toLowerCase();
        if (!lower.startsWith("minerar ")) return false;

        String targetRaw = body.substring("minerar".length()).trim();
        if (targetRaw.isEmpty()) return true;

        String mapped = baritone.mapBlockName(targetRaw);
        MoodService.Mood m = mood.getMood(mc.player);

        boolean accept;
        String reply;

        switch (m) {
            case HAPPY -> {
                accept = true;
                reply = dialogue.pickOne(TrChatConfig.MSG_MINE_HAPPY_ACCEPT.get());
            }
            case NEUTRAL -> {
                accept = RNG.nextBoolean();
                reply = accept
                        ? dialogue.pickOne(TrChatConfig.MSG_MINE_NEUTRAL_ACCEPT.get())
                        : dialogue.pickOne(TrChatConfig.MSG_MINE_NEUTRAL_REJECT.get());
            }
            default -> {
                accept = false;
                reply = dialogue.pickOne(TrChatConfig.MSG_MINE_SAD_REJECT.get());
            }
        }

        reply = dialogue.inject(reply, "item", targetRaw);

        if (accept) {
            String finalReply = reply;
            delay.sendThenRun(mc, finalReply, () -> baritone.mine(mapped));
        } else {
            delay.sendThenRun(mc, reply, null);
        }
        return true;
    }
}
