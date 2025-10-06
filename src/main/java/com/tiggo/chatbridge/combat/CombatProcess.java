package com.tiggo.chatbridge.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Processo de combate: escolhe alvo, caminha até ele (Baritone) e ataca. */
public class CombatProcess {

    public static boolean DEBUG = true;

    private LivingEntity currentTarget = null;
    private long nextSwingAt = 0L;
    private long nextStrafeAt = 0L;
    private boolean pausedFollowForCombat = false;
    private long lastDebugAt = 0L;

    /** Chamado a cada tick pelo CombatSystem. */
    void tick() {
        CombatConfig cfg = CombatSystem.CONFIG;
        if (cfg == null || !cfg.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        Player self = mc.player;
        if (self == null || self.isSpectator()) return;

        boolean baritoneOk = BaritoneBridge.isPresent();

        if (self.getHealth() < cfg.minHealthToEngage) {
            releaseFollowPause();
            currentTarget = null;
            debug("Vida baixa: não engajar");
            return;
        }

        chooseBestWeapon(self);

        LivingEntity target = pickTarget(self, cfg);
        boolean hasTarget = target != null && target.isAlive();
        currentTarget = hasTarget ? target : null;

        if (hasTarget && !pausedFollowForCombat) {
            if (baritoneOk) BaritoneBridge.clearGoal();
            pausedFollowForCombat = true;
        } else if (!hasTarget && pausedFollowForCombat) {
            releaseFollowPause();
        }

        if (hasTarget) {
            engage(self, target, cfg, baritoneOk);
            debug("Alvo: " + target.getName().getString() + " dist=" + String.format("%.1f", self.distanceTo(target)));
        } else {
            debug("Sem alvos");
        }
    }

    public void cancelCombatAndRelease() {
        currentTarget = null;
        pausedFollowForCombat = false;
        BaritoneBridge.clearGoal();
    }

    private void releaseFollowPause() { pausedFollowForCombat = false; }

    // ===== Alvo =====
    private LivingEntity pickTarget(Player self, CombatConfig cfg) {
        double dist = cfg.distanceBlocks;
        var box = self.getBoundingBox().inflate(dist, dist/2, dist);

        List<LivingEntity> candidates = new ArrayList<>();
        for (Entity e : self.level().getEntities(self, box, ent -> ent instanceof LivingEntity && ent.isAlive())) {
            LivingEntity le = (LivingEntity) e;
            if (!passesFilters(le, cfg)) continue;
            if (cfg.requireLineOfSight && !hasLineOfSight(self, le)) continue;
            candidates.add(le);
        }
        if (candidates.isEmpty()) return null;

        var followedUUID = FollowHook.getFollowed().orElse(null);
        Player followed = followedUUID == null ? null : self.level().getPlayerByUUID(followedUUID);

        LivingEntity best = null; int bestScore = Integer.MIN_VALUE;
        for (LivingEntity cand : candidates) {
            int score = basePriority(cand, cfg);
            if (followed != null) {
                if (cfg.ruleRetaliateForFollowed && looksAt(cand, followed))
                    score += cfg.priority.getOrDefault("attackingFollowed", 100);
                if (cfg.ruleAssistFollowed && looksAt(followed, cand))
                    score += cfg.priority.getOrDefault("targetOfFollowed", 80);
            }
            // mais perto = melhor
            score -= (int) Math.sqrt(self.distanceToSqr(cand));
            if (score > bestScore) { bestScore = score; best = cand; }
        }

        if (best instanceof Creeper && cfg.ignoreCreepers) return null;
        return best;
    }

    private boolean passesFilters(LivingEntity e, CombatConfig cfg) {
        if (e instanceof Player) return cfg.targetPlayers && !e.isInvisible() && !e.isSpectator();
        if (e instanceof Monster) {
            if (!cfg.targetMonsters) return false;
            if (cfg.ignoreCreepers && e instanceof Creeper) return false;
            return true;
        }
        MobCategory cat = e.getType().getCategory();
        if (cat == MobCategory.CREATURE || cat == MobCategory.AMBIENT || cat == MobCategory.WATER_AMBIENT)
            return cfg.targetAnimals;
        return false;
    }

    private int basePriority(LivingEntity e, CombatConfig cfg) {
        if (e instanceof Player)  return cfg.priority.getOrDefault("player", 40);
        if (e instanceof Monster) return cfg.priority.getOrDefault("hostile", 60);
        return cfg.priority.getOrDefault("animal", 10);
    }

    private boolean hasLineOfSight(Player self, LivingEntity e) {
        Vec3 eye = self.getEyePosition();
        Vec3 tgt = e.getEyePosition();
        var res = self.level().clip(new ClipContext(eye, tgt, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, self));
        return res.getType() == HitResult.Type.MISS;
    }

    private boolean looksAt(LivingEntity a, LivingEntity b) {
        Vec3 dir = a.getLookAngle();
        Vec3 to = b.position().subtract(a.position()).normalize();
        return dir.dot(to) > 0.8;
    }

    // ===== Execução =====
    private void engage(Player self, LivingEntity target, CombatConfig cfg, boolean baritoneOk) {
        double desired = 3.0; // alcance de espada aproximado
        double distSq = self.distanceToSqr(target);

        // aproxima até o alcance
        if (distSq > desired * desired) {
            if (baritoneOk) {
                BlockPos near = target.blockPosition();
                BaritoneBridge.setGoalNear(near, (int) Math.ceil(desired));
            }
            return;
        } else if (baritoneOk) {
            BaritoneBridge.clearGoal();
        }

        // mirar
        lookAt(self, target);

        // strafing leve
        if (cfg.enableStrafe) {
            long now = System.currentTimeMillis();
            if (now >= nextStrafeAt) {
                nextStrafeAt = now + 900 + ThreadLocalRandom.current().nextInt(400);
                double angle = ThreadLocalRandom.current().nextBoolean() ? +25 : -25;
                Vec3 off = rotateAround(target.position().subtract(self.position()), Math.toRadians(angle))
                        .normalize().scale(1.2);
                if (baritoneOk) {
                    BlockPos pos = self.blockPosition().offset((int) off.x, 0, (int) off.z);
                    BaritoneBridge.setGoalNear(pos, 1);
                }
            }
        }

        // cooldown + ataque
        long now = System.currentTimeMillis();
        boolean cooled = self.getAttackStrengthScale(0) >= 0.99f && now >= nextSwingAt;
        if (cooled) {
            Minecraft.getInstance().gameMode.attack(self, target);
            self.swing(InteractionHand.MAIN_HAND);
            nextSwingAt = now + Math.max(200, cfg.cooldownMs);
        }
    }

    private Vec3 rotateAround(Vec3 v, double rad) {
        double c = Math.cos(rad), s = Math.sin(rad);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    private void lookAt(Player self, Entity e) {
        Vec3 d = e.position().subtract(self.position());
        double yaw = Math.toDegrees(Math.atan2(-d.x, d.z));
        double pitch = Math.toDegrees(-Math.atan2(d.y, Math.sqrt(d.x*d.x + d.z*d.z)));
        self.setYRot((float) yaw);
        self.setXRot((float) pitch);
    }

    private void chooseBestWeapon(Player self) {
        int bestSlot = -1;
        double bestDmg = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack st = self.getInventory().items.get(slot);
            if (st.isEmpty()) continue;
            double dmg = estimateDamage(st);
            if (dmg > bestDmg) { bestDmg = dmg; bestSlot = slot; }
        }
        if (bestSlot >= 0 && self.getInventory().selected != bestSlot) {
            self.getInventory().selected = bestSlot;
        }
    }

    private double estimateDamage(ItemStack st) {
        try {
            var map = st.getAttributeModifiers(EquipmentSlot.MAINHAND);
            var list = map.get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if (list == null || list.isEmpty()) return 1.0;
            return list.stream().mapToDouble(m -> {
                try { return m.getAmount(); } catch (Throwable t) { return 0; }
            }).max().orElse(1.0);
        } catch (Throwable t) {
            return 1.0;
        }
    }

    private void debug(String msg) {
        if (!DEBUG) return;
        long now = System.currentTimeMillis();
        if (now - lastDebugAt < 400) return;
        lastDebugAt = now;
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.gui.setOverlayMessage(ComponentCache.cached(msg), false);
        }
    }

    /** Cache simples para componentes do overlay. */
    static class ComponentCache {
        private static String last = "";
        private static net.minecraft.network.chat.Component cached = net.minecraft.network.chat.Component.literal("");
        static net.minecraft.network.chat.Component cached(String s) {
            if (!Objects.equals(last, s)) {
                last = s; cached = net.minecraft.network.chat.Component.literal(s);
            }
            return cached;
        }
    }
}
