package com.tiggo.chatbridge.core;

import net.minecraft.client.player.LocalPlayer;

public class MoodService {

    public enum Mood { HAPPY, NEUTRAL, SAD }

    /** Converte food (0..20) para humor. */
    public Mood getMood(LocalPlayer me) {
        int food = me.getFoodData() != null ? me.getFoodData().getFoodLevel() : 20;
        double icons = food / 2.0;
        if (icons >= 7.0) return Mood.HAPPY;
        if (icons <= 4.0) return Mood.SAD;
        return Mood.NEUTRAL;
    }
}
