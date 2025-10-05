package com.tiggo.trbaritonechatbridge.chat;

import java.util.List;
import java.util.Random;

public class DialogueService {
    private static final Random RNG = new Random();

    public String pickOne(List<? extends String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        return lines.get(RNG.nextInt(lines.size()));
    }

    public String inject(String msg, String key, String value) {
        return msg == null ? "" : msg.replace("{" + key + "}", value);
    }

    public String injectXYZ(String msg, String x, String y, String z) {
        return inject(inject(inject(msg, "x", x), "y", y), "z", z);
    }
}
