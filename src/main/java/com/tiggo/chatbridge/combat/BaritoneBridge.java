package com.tiggo.chatbridge.combat;

import net.minecraft.core.BlockPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Acesso ao Baritone via reflexão para evitar NoClassDefFoundError em tempo de carregamento.
 */
public final class BaritoneBridge {

    private static volatile boolean checked = false;
    private static volatile boolean present = false;

    // caches de reflexão
    private static Class<?> clsBaritoneAPI;
    private static Class<?> clsGoalNear;
    private static Method mGetProvider, mGetPrimaryBaritone, mGetCustomGoalProcess, mSetGoal, mSetGoalAndPath;
    private static Constructor<?> ctorGoalNear;

    private BaritoneBridge() {}

    /** Confere se as classes do Baritone estão presentes no classpath. */
    public static boolean isPresent() {
        if (checked) return present;
        synchronized (BaritoneBridge.class) {
            if (checked) return present;
            try {
                clsBaritoneAPI = Class.forName("baritone.api.BaritoneAPI");
                clsGoalNear    = Class.forName("baritone.api.pathing.goals.GoalNear");

                // BaritoneAPI.getProvider().getPrimaryBaritone()
                mGetProvider = clsBaritoneAPI.getMethod("getProvider");
                Class<?> clsProvider = mGetProvider.getReturnType();
                Method mGetPrimary = clsProvider.getMethod("getPrimaryBaritone");
                mGetPrimaryBaritone = mGetPrimary;

                // baritone.getCustomGoalProcess()
                Class<?> clsBaritone = mGetPrimary.getReturnType();
                mGetCustomGoalProcess = clsBaritone.getMethod("getCustomGoalProcess");
                Class<?> clsCustomGoalProc = mGetCustomGoalProcess.getReturnType();

                // setGoal(Goal) / setGoalAndPath(Goal)
                for (Method m : clsCustomGoalProc.getMethods()) {
                    if (m.getName().equals("setGoal") && m.getParameterCount() == 1) mSetGoal = m;
                    if (m.getName().equals("setGoalAndPath") && m.getParameterCount() == 1) mSetGoalAndPath = m;
                }

                // new GoalNear(BlockPos, int)
                ctorGoalNear = clsGoalNear.getConstructor(BlockPos.class, int.class);

                present = clsBaritoneAPI != null && mGetProvider != null && mGetPrimaryBaritone != null
                        && mGetCustomGoalProcess != null && (mSetGoal != null || mSetGoalAndPath != null)
                        && ctorGoalNear != null;
            } catch (Throwable t) {
                present = false;
            } finally {
                checked = true;
            }
            return present;
        }
    }

    /** Limpa o goal atual (pausa pathing). */
    public static void clearGoal() {
        if (!isPresent()) return;
        try {
            Object provider = mGetProvider.invoke(null);
            Object baritone = mGetPrimaryBaritone.invoke(provider);
            Object proc = mGetCustomGoalProcess.invoke(baritone);
            if (mSetGoal != null) {
                mSetGoal.invoke(proc, new Object[]{null});
            }
        } catch (Throwable ignored) {}
    }

    /** Define um GoalNear e inicia path até lá. */
    public static void setGoalNear(BlockPos pos, int range) {
        if (!isPresent()) return;
        try {
            Object provider = mGetProvider.invoke(null);
            Object baritone = mGetPrimaryBaritone.invoke(provider);
            Object proc = mGetCustomGoalProcess.invoke(baritone);
            Object goal = ctorGoalNear.newInstance(pos, range);
            if (mSetGoalAndPath != null) {
                mSetGoalAndPath.invoke(proc, goal);
            }
        } catch (Throwable ignored) {}
    }
}
