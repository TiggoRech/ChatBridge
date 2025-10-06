package com.tiggo.chatbridge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;

public class TrChatConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<String> BOT_NAME;
    public static final ForgeConfigSpec.ConfigValue<String> OWNER_NAME;
    public static final ForgeConfigSpec.ConfigValue<String> OWNER_UUID;
    public static final ForgeConfigSpec.ConfigValue<String> CMD_PREFIX;
    public static final ForgeConfigSpec.BooleanValue ALLOW_ANY_SENDER;

    // Dialogues (existentes)
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_MINE_HAPPY_ACCEPT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_MINE_NEUTRAL_ACCEPT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_MINE_NEUTRAL_REJECT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_MINE_SAD_REJECT;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_COME_HAPPY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_COME_NEUTRAL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_COME_SAD;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_FOLLOW_OTHER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_FOLLOW_NEAREST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_STOP;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_GO;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_TALK;

    // Novas falas: dormir/deitar
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_SLEEP_TRY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_SLEEP_CLICKED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_LAY_REGISTERING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MSG_LAY_ALREADY;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("ChatBridge");
        BOT_NAME = b.define("botName", "Luna");
        OWNER_NAME = b.define("ownerName", "TiggoRech");
        OWNER_UUID = b.define("ownerUuid", "");
        CMD_PREFIX = b.define("commandPrefix", "!");
        ALLOW_ANY_SENDER = b.define("allowAnySender", false);
        b.pop();

        b.push("Dialogues");

        // minerar
        MSG_MINE_HAPPY_ACCEPT = b.defineList("mineHappyAccept", List.of(
                "Tudo bem! Vou lá pegar {item}.",
                "Deixa comigo, coletando {item}!",
                "Bora minerar {item}!",
                "Perfeito, {item} vindo aí.",
                "Pode deixar, mestre. {item} já já."
        ), o -> o instanceof String);

        MSG_MINE_NEUTRAL_ACCEPT = b.defineList("mineNeutralAccept", List.of(
                "Ok, vou minerar {item}.",
                "Certo, indo pegar {item}.",
                "Entendido, {item} a caminho."
        ), o -> o instanceof String);

        MSG_MINE_NEUTRAL_REJECT = b.defineList("mineNeutralReject", List.of(
                "Hmm… tô um pouco cansada e com fome. Agora não pego {item}.",
                "Talvez depois… preciso comer algo antes de buscar {item}.",
                "Metade de mim quer ir, metade quer uma soneca. Pulo {item} por enquanto."
        ), o -> o instanceof String);

        MSG_MINE_SAD_REJECT = b.defineList("mineSadReject", List.of(
                "Não consigo… tô morrendo de fome. Sem {item} agora.",
                "Desculpa… sem energia pra {item}.",
                "Eu… preciso comer primeiro. {item} depois."
        ), o -> o instanceof String);

        // vir
        MSG_COME_HAPPY = b.defineList("comeHappy", List.of(
                "Indo aí, {owner}!",
                "Chegando já, {owner}!",
                "Partiu, {owner}!",
                "No seu encalço, {owner}!",
                "Sim! Vindo agora, {owner}!"
        ), o -> o instanceof String);

        MSG_COME_NEUTRAL = b.defineList("comeNeutral", List.of(
                "Ok, indo até você, {owner}.",
                "Certo, chegando, {owner}.",
                "Entendido. Em rota, {owner}."
        ), o -> o instanceof String);

        MSG_COME_SAD = b.defineList("comeSad", List.of(
                "Tá… indo, {owner}.",
                "Vou, {owner}… só um instante.",
                "Chegando… meio sem energia, {owner}."
        ), o -> o instanceof String);

        // seguir
        MSG_FOLLOW_OTHER = b.defineList("followOther", List.of(
                "Beleza, seguindo {target}.",
                "Ok, vou seguir {target}.",
                "Entendido: seguindo {target}.",
                "Rastreamento de {target} iniciado.",
                "No rastro de {target}!"
        ), o -> o instanceof String);

        MSG_FOLLOW_NEAREST = b.defineList("followNearest", List.of(
                "Seguindo {target}.",
                "Certo, indo atrás de {target}.",
                "Ok, {target} é o mais perto – seguindo."
        ), o -> o instanceof String);

        // stop / go / talk
        MSG_STOP = b.defineList("stop", List.of(
                "Parando agora.",
                "Ok, interrupção feita.",
                "Comando recebido: stop.",
                "Tudo certo, parei.",
                "Parei. O que precisa agora?"
        ), o -> o instanceof String);

        MSG_GO = b.defineList("go", List.of(
                "Indo para {x} {y} {z}.",
                "Rota traçada: {x} {y} {z}.",
                "Movendo até {x} {y} {z}."
        ), o -> o instanceof String);

        MSG_TALK = b.defineList("talk", List.of(
                "Certo.",
                "Ok.",
                "Beleza."
        ), o -> o instanceof String);

        // ===== novas falas =====
        MSG_SLEEP_TRY = b.defineList("sleepTry", List.of(
                "Vou tentar dormir…",
                "Procurando uma cama por perto…",
                "Indo descansar um pouco."
        ), o -> o instanceof String);

        MSG_SLEEP_CLICKED = b.defineList("sleepClicked", List.of(
                "Cama usada (respawn salvo).",
                "Pronto, toquei na cama.",
                "Deitei rapidinho."
        ), o -> o instanceof String);

        MSG_LAY_REGISTERING = b.defineList("layRegistering", List.of(
                "Sem respawn salvo aqui. Vou registrar nessa cama.",
                "Vou setar meu ponto de respawn agora.",
                "Registrando ponto de respawn nesta cama."
        ), o -> o instanceof String);

        MSG_LAY_ALREADY = b.defineList("layAlready", List.of(
                "Já tenho um ponto de respawn salvo por aqui.",
                "Aqui já é meu respawn atual.",
                "Respawn já salvo nessa área."
        ), o -> o instanceof String);

        b.pop();
        SPEC = b.build();
    }

    public static void loadEarly(Path customPath) {
        FMLPaths.CONFIGDIR.get().toFile().mkdirs();
    }
}
