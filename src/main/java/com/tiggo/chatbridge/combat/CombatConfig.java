package com.tiggo.chatbridge.combat;

import java.util.HashMap;
import java.util.Map;

/** Config padrão em memória para o módulo de combate. */
public class CombatConfig {

    /** Habilita/Desabilita o sistema de combate. */
    public boolean enabled = false;

    /** Cooldown mínimo entre ataques (ms). */
    public int cooldownMs = 300;

    /** Distância máxima para procurar/engajar alvos (blocos). */
    public double distanceBlocks = 12.0;

    /** Vida mínima do jogador para engajar combate. */
    public float minHealthToEngage = 8.0f;

    // Filtros de entidades
    public boolean targetPlayers = false;
    public boolean targetMonsters = true;
    public boolean targetAnimals = false;
    public boolean ignoreCreepers = true;

    // Regras
    public boolean ruleRetaliateForFollowed = true; // revidar quem bater no seguido
    public boolean ruleAssistFollowed = false;      // atacar junto com o seguido
    public boolean requireLineOfSight = true;       // exigir raycast/visão
    public boolean enableStrafe = true;             // strafing leve

    /** Priorização (maior = mais importante) */
    public final Map<String, Integer> priority = new HashMap<>();

    public CombatConfig() {
        priority.put("player", 40);
        priority.put("hostile", 60);
        priority.put("animal", 10);
        priority.put("attackingFollowed", 100);
        priority.put("targetOfFollowed", 80);
    }
}
