package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.baritone.BedInteractor;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.util.Delay;
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
