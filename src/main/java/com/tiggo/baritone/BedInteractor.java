package com.tiggo.chatbridge.baritone;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.atomic.AtomicBoolean;

public class BedInteractor {

    private final BaritoneService baritone;
    private final Minecraft mc = Minecraft.getInstance();

    // sleep loop control
    private final AtomicBoolean sleepLoopActive = new AtomicBoolean(false);

    // last respawn memory
    private volatile BlockPos lastRespawnBed = null;
    private volatile ResourceLocation lastRespawnDim = null;

    public BedInteractor(BaritoneService baritone) {
        this.baritone = baritone;
    }

    // ===== public API =====

    /** Try every ~3s to go to nearest bed and click it. Stops when sleeping or on stopSleepLoop(). */
    public void startSleepLoop() {
        if (sleepLoopActive.getAndSet(true)) {
            baritone.infoClient("[ChatBridge] Já estou tentando dormir…");
            return;
        }
        new Thread(() -> {
            while (sleepLoopActive.get()) {
                mc.execute(this::trySleepOnce);
                for (int i = 0; i < 30 && sleepLoopActive.get(); i++) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }
        }, "tr_chatbridge_sleep_loop").start();
    }

    public void stopSleepLoop() {
        sleepLoopActive.set(false);
    }

    /** One shot: interact with nearest bed to save respawn (records memory if true). */
    public void interactOnceAtNearestBed(boolean recordAsRespawn) {
        mc.execute(() -> {
            LocalPlayer me = mc.player;
            if (me == null || mc.level == null) return;
            BlockPos bed = findNearestBedPos(24);
            if (bed == null) {
                baritone.infoClient("[ChatBridge] Nenhuma cama próxima encontrada.");
                return;
            }
            baritone.sendBaritone("#path " + bed.getX() + " " + bed.getY() + " " + bed.getZ());
            waitUntilCloseThenClick(bed, 2.5, () -> {
                if (recordAsRespawn) rememberRespawn(bed);
            });
        });
    }

    public boolean hasNearbyRespawn(int radius) {
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null || lastRespawnBed == null || lastRespawnDim == null) return false;
        return lastRespawnDim.equals(currentDimension())
                && me.blockPosition().closerThan(lastRespawnBed, Math.max(1, radius));
    }

    public void rememberRespawn(BlockPos bedPos) {
        lastRespawnBed = bedPos == null ? null : bedPos.immutable();
        lastRespawnDim = bedPos == null ? null : currentDimension();
    }

    // ===== internal =====

    private void trySleepOnce() {
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null) { stopSleepLoop(); return; }

        if (me.isSleepingLongEnough()) { // already sleeping
            stopSleepLoop();
            return;
        }

        BlockPos bed = findNearestBedPos(24);
        if (bed == null) {
            baritone.infoClient("[ChatBridge] Nenhuma cama próxima encontrada.");
            stopSleepLoop();
            return;
        }

        baritone.sendBaritone("#path " + bed.getX() + " " + bed.getY() + " " + bed.getZ());
        waitUntilCloseThenClick(bed, 2.5, () -> rememberRespawn(bed));
    }

    private BlockPos findNearestBedPos(int radius) {
        LocalPlayer me = mc.player;
        if (me == null || mc.level == null) return null;

        BlockPos mePos = me.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        int r = Math.max(1, radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = mePos.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(p);
                    if (st.getBlock() instanceof BedBlock) {
                        double d2 = me.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                        if (d2 < bestDist) {
                            bestDist = d2;
                            best = p.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private void waitUntilCloseThenClick(BlockPos pos, double reach, Runnable onClickDone) {
        new Thread(() -> {
            boolean clicked = false;
            while (!clicked) {
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
                mc.execute(() -> {
                    LocalPlayer me = mc.player;
                    if (me == null || mc.level == null) return;
                    double d2 = me.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (d2 <= reach * reach) {
                        lookAtBlockCenter(me, pos);
                        if (mc.gameMode != null) {
                            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                            mc.gameMode.useItemOn(me, InteractionHand.MAIN_HAND, hit); // SINGLE click
                        }
                        if (onClickDone != null) onClickDone.run();
                    }
                });
                clicked = true; // one-shot
            }
        }, "tr_chatbridge_bed_click_once").start();
    }

    private void lookAtBlockCenter(LocalPlayer me, BlockPos pos) {
        double dx = pos.getX() + 0.5 - me.getX();
        double dy = pos.getY() + 0.5 - me.getEyeY();
        double dz = pos.getZ() + 0.5 - me.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz)));
        me.setYRot(yaw);
        me.setXRot(pitch);
        me.setYHeadRot(yaw);
        me.setYBodyRot(yaw);
    }

    private ResourceLocation currentDimension() {
        Level lvl = mc.level;
        return (lvl != null) ? lvl.dimension().location() : new ResourceLocation("minecraft", "overworld");
    }
}
