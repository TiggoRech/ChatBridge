package com.tiggo.trbaritonechatbridge.baritone;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public class BaritoneService {

    private final Minecraft mc = Minecraft.getInstance();

    public void sendChat(String text) {
        ClientPacketListener net = mc.getConnection();
        if (net != null) net.sendChat(text);
    }

    public void sendBaritone(String baritoneCommand) {
        if (!baritoneCommand.startsWith("#")) baritoneCommand = "#" + baritoneCommand;
        sendChat(baritoneCommand);
    }

    public void infoClient(String msg) {
        LocalPlayer me = mc.player;
        if (me != null) me.sendSystemMessage(Component.literal(msg));
    }

    // ===== high-level helpers =====

    public void stop() {
        sendBaritone("#stop");
    }

    public void followPlayer(String name) {
        sendBaritone("#follow player " + name);
    }

    public String nearestPlayerName() {
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null) return null;
        return mc.level.players().stream()
                .filter(p -> p != null && p.isAlive() && !p.getUUID().equals(me.getUUID()))
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(me)))
                .map(p -> {
                    GameProfile gp = p.getGameProfile();
                    return gp != null ? gp.getName() : null;
                })
                .orElse(null);
    }

    public void pathTo(String x, String y, String z) {
        sendBaritone("#path " + x + " " + y + " " + z);
    }

    public void mine(String target) {
        sendBaritone("#mine " + target);
    }

    // ===== mapping alvos de mineração =====
    public String mapBlockName(String name) {
        String n = name.toLowerCase();
        switch (n) {
            case "madeira":
            case "tronco":
            case "log":
            case "logs":
                return "minecraft:oak_log minecraft:spruce_log minecraft:birch_log minecraft:jungle_log minecraft:acacia_log minecraft:dark_oak_log minecraft:mangrove_log minecraft:cherry_log";
            case "pedra":
            case "stone":
                return "minecraft:stone";
            case "carvao":
            case "carvão":
                return "minecraft:coal_ore minecraft:deepslate_coal_ore";
            case "ferro":
                return "minecraft:iron_ore minecraft:deepslate_iron_ore";
            case "ouro":
            case "gold":
                return "minecraft:gold_ore minecraft:deepslate_gold_ore";
            case "diamante":
            case "diamond":
                return "minecraft:diamond_ore minecraft:deepslate_diamond_ore";
            case "redstone":
                return "minecraft:redstone_ore minecraft:deepslate_redstone_ore";
            case "esmeralda":
            case "emerald":
                return "minecraft:emerald_ore minecraft:deepslate_emerald_ore";
            case "cobre":
            case "copper":
                return "minecraft:copper_ore minecraft:deepslate_copper_ore";
            case "quartzo":
            case "quartz":
                return "minecraft:nether_quartz_ore";
            case "terra":
            case "dirt":
                return "minecraft:dirt minecraft:grass_block";
            default:
                if (!n.startsWith("minecraft:")) return "minecraft:" + n;
                return n;
        }
    }
}
