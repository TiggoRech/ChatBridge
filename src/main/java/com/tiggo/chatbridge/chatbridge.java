package com.tiggo.chatbridge;

import com.tiggo.chatbridge.baritone.BaritoneService;
import com.tiggo.chatbridge.baritone.BedInteractor;
import com.tiggo.chatbridge.chat.ChatParser;
import com.tiggo.chatbridge.chat.DialogueService;
import com.tiggo.chatbridge.config.TrChatConfig;
import com.tiggo.chatbridge.core.CommandRegistry;
import com.tiggo.chatbridge.core.MoodService;
import com.tiggo.chatbridge.commands.*;
import com.tiggo.chatbridge.util.Delay;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(chatbridge.MOD_ID)
public class chatbridge {

    public static final String MOD_ID = "tr_baritone_chatbridge";

    // Services
    private final Minecraft mc = Minecraft.getInstance();
    private final DialogueService dialogue = new DialogueService();
    private final MoodService mood = new MoodService();
    private final Delay delay = new Delay();
    private final BaritoneService baritone = new BaritoneService();
    private final BedInteractor bedInteractor = new BedInteractor(baritone);
    private final ChatParser chat = new ChatParser();

    private final CommandRegistry registry = new CommandRegistry(baritone);

    public chatbridge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, TrChatConfig.SPEC);
        TrChatConfig.loadEarly(FMLPaths.CONFIGDIR.get().resolve("tr_baritone_chatbridge.toml"));

        MinecraftForge.EVENT_BUS.register(this);

        // Commands
        registry.register(new StopCommand(baritone, dialogue, delay, bedInteractor));
        registry.register(new ComeCommand(baritone, dialogue, delay, mood));
        registry.register(new FollowCommand(baritone, dialogue, delay));
        registry.register(new GoCommand(baritone, dialogue, delay));
        registry.register(new MineCommand(baritone, dialogue, delay, mood));
        registry.register(new SayCommand(baritone, dialogue, delay));
        registry.register(new SleepCommand(baritone, dialogue, delay, bedInteractor));
        registry.register(new LayCommand(baritone, dialogue, delay, bedInteractor));   // (!deitar)

        // OBS.: CombatSystem não precisa de init manual — ele se registra via EventBus.
    }

    @SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		if (!chat.isOwnerMessage(mc, event)) return;

		String body = chat.extractCommandBody(event);
		if (body == null || body.isBlank()) return;

		// Novo: intercepta comandos !combat
		if (com.tiggo.chatbridge.combat.CombatCommand.handle(body)) return;

		// Se não for comando de combate, passa para os comandos do ChatBridge
		registry.executeIfMatched(mc, body);
	}
}
