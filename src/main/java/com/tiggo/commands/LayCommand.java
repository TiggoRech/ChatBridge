package com.tiggo.chatbridge.commands;

import com.tiggo.chatbridge.baritone.BaritoneService;
import com.tiggo.chatbridge.baritone.BedInteractor;
import com.tiggo.chatbridge.chat.DialogueService;
import com.tiggo.chatbridge.config.TrChatConfig;
import com.tiggo.chatbridge.core.Command;
import com.tiggo.chatbridge.util.Delay;
import net.minecraft.client.Minecraft;

public class LayCommand implements Command {

    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;
    private final BedInteractor bedInteractor;

    public LayCommand(BaritoneService baritone, DialogueService dialogue, Delay delay, BedInteractor bedInteractor) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
        this.bedInteractor = bedInteractor;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        if (!body.equalsIgnoreCase("deitar")) return false;

        if (bedInteractor.hasNearbyRespawn(8)) {
            // já tem respawn salvo por perto
            String msg = dialogue.pickOne(TrChatConfig.MSG_LAY_ALREADY.get());
            delay.sendThenRun(mc, msg, null);
        } else {
            // vai registrar agora
            String msg = dialogue.pickOne(TrChatConfig.MSG_LAY_REGISTERING.get());
            delay.sendThenRun(mc, msg, () -> bedInteractor.interactOnceAtNearestBed(true));
        }
        return true;
    }
}
