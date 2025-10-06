package com.tiggo.chatbridge.combat;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber
public class CommandsClient {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("bot")
            .then(Commands.literal("vir").executes(ctx -> runComeHere()))
            .then(Commands.literal("!vir").executes(ctx -> runComeHere()))
        );
    }

    private static int runComeHere() {
        CombatSystem.getProcess().cancelCombatAndRelease();

        Optional<java.util.UUID> followed = FollowHook.getFollowed();
        var mc = Minecraft.getInstance();
        Player self = mc.player;
        if (self == null) return 1;

        if (BaritoneBridge.isPresent() && followed.isPresent()) {
            Player target = self.level().getPlayerByUUID(followed.get());
            if (target != null && target.isAlive()) {
                BlockPos near = target.blockPosition();
                BaritoneBridge.setGoalNear(near, 1);
            }
        }
        return 1;
    }
}
