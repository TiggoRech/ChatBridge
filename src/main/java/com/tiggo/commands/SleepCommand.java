package com.tiggo.trbaritonechatbridge.commands;

import com.tiggo.trbaritonechatbridge.baritone.BaritoneService;
import com.tiggo.trbaritonechatbridge.baritone.BedInteractor;
import com.tiggo.trbaritonechatbridge.chat.DialogueService;
import com.tiggo.trbaritonechatbridge.config.TrChatConfig;
import com.tiggo.trbaritonechatbridge.core.Command;
import com.tiggo.trbaritonechatbridge.util.Delay;
import net.minecraft.client.Minecraft;

public class SleepCommand implements Command {

    private final BaritoneService baritone;
    private final DialogueService dialogue;
    private final Delay delay;
    private final BedInteractor bedInteractor;

    public SleepCommand(BaritoneService baritone, DialogueService dialogue, Delay delay, BedInteractor bedInteractor) {
        this.baritone = baritone;
        this.dialogue = dialogue;
        this.delay = delay;
        this.bedInteractor = bedInteractor;
    }

    @Override
    public boolean tryExecute(Minecraft mc, String body) {
        if (!body.equalsIgnoreCase("dormir")) return false;

        String msg = dialogue.pickOne(TrChatConfig.MSG_SLEEP_TRY.get());
        delay.sendThenRun(mc, msg, bedInteractor::startSleepLoop);
        return true;
    }
}
