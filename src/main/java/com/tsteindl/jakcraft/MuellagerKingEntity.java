package com.tsteindl.jakcraft;

import com.yellowbrossproductions.illageandspillage.entities.MagispellerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MuellagerKingEntity extends MagispellerEntity {

    // Set true once the fight-start line has been shown; keeps it to a single announcement.
    private boolean announcedFightStart = false;
    // Previous attack type, used to fire an "on attack" line when a new attack begins.
    private int lastAttackType = 0;
    // Ticks until the Fourier-Welle attack can be used again.
    private int fourierCooldown = 0;

    public MuellagerKingEntity(EntityType<? extends MagispellerEntity> type, Level level) {
        super(type, level);
        // The parent already manages a (purple) boss bar. Recolor it red for the final boss.
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Disable the Magispeller's life-steal attack by removing its goal. The drain/heal in
        // the parent only runs while attackType == 2, which only LifestealGoal.start() sets.
        this.goalSelector.getAvailableGoals()
            .removeIf(wrapped -> wrapped.getGoal().getClass().getSimpleName().toLowerCase().contains("lifesteal"));
        // Add the new harmonic-analysis attack.
        this.goalSelector.addGoal(1, new FourierWaveGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (this.fourierCooldown > 0) {
            this.fourierCooldown--;
        }

        // Fight start: first time the king acquires a target.
        if (!this.announcedFightStart && this.getTarget() != null) {
            this.announcedFightStart = true;
            this.say(Config.FIGHT_START_LINES.get());
        }

        // On each attack: fire a line when attackType transitions from idle (0) to any attack.
        int attackType = this.getAttackType();
        if (attackType != 0 && this.lastAttackType == 0) {
            this.say(Config.ATTACK_LINES.get());
        }
        this.lastAttackType = attackType;
    }

    @Override
    public void die(DamageSource source) {
        this.say(Config.DEATH_LINES.get());
        super.die(source);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource source) {
        if (this.shouldDropLoot() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) && this.lastHurtByPlayerTime > 0) {
            this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack((ItemLike) ModItems.PHD.get())));
        }
        super.dropAllDeathLoot(source);
    }

    // Picks a random line from the given pool and broadcasts it to nearby players' chat.
    void say(List<? extends String> pool) {
        if (!(this.level() instanceof ServerLevel serverLevel) || pool.isEmpty()) {
            return;
        }
        String line = pool.get(this.getRandom().nextInt(pool.size()));
        Component message = Component.literal(this.getDisplayName().getString() + ": " + line)
            .withStyle(ChatFormatting.RED);
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48.0))) {
            player.sendSystemMessage(message);
        }
    }

    // "Fourier-Welle": an expanding harmonic shockwave. A ring of particles grows outward from
    // the king; players caught in the ring's band take magic damage and are knocked back once.
    static class FourierWaveGoal extends Goal {

        private static final int DURATION_TICKS = 30;
        private static final double MAX_RADIUS = 9.0;
        private static final int COOLDOWN_TICKS = 200;

        private final MuellagerKingEntity king;
        private final Set<UUID> alreadyHit = new HashSet<>();
        private int tick;

        FourierWaveGoal(MuellagerKingEntity king) {
            this.king = king;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (this.king.getAttackType() != 0 || this.king.fourierCooldown > 0) {
                return false; // don't interrupt the parent's spells
            }
            if (this.king.distanceToSqr(target) > 16.0 * 16.0) {
                return false;
            }
            return this.king.getRandom().nextInt(60) == 0;
        }

        @Override
        public void start() {
            this.tick = 0;
            this.alreadyHit.clear();
            this.king.fourierCooldown = COOLDOWN_TICKS;
            this.king.getNavigation().stop();
            this.king.say(Config.ATTACK_LINES.get());
        }

        @Override
        public boolean canContinueToUse() {
            return this.tick < DURATION_TICKS;
        }

        @Override
        public void tick() {
            this.king.getNavigation().stop();
            LivingEntity target = this.king.getTarget();
            if (target != null) {
                this.king.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ());
            }

            this.tick++;
            double radius = (this.tick / (double) DURATION_TICKS) * MAX_RADIUS;
            spawnRing(radius);
            applyWave(radius);
        }

        private void spawnRing(double radius) {
            if (!(this.king.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            double centerX = this.king.getX();
            double centerY = this.king.getY() + 0.2;
            double centerZ = this.king.getZ();
            int points = Math.max(8, (int) (radius * 6));
            for (int i = 0; i < points; i++) {
                double angle = (Math.PI * 2.0 * i) / points;
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, centerY, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        private void applyWave(double radius) {
            double inner = radius - 1.0;
            double outer = radius + 1.0;
            for (Player player : this.king.level().getEntitiesOfClass(Player.class, this.king.getBoundingBox().inflate(outer + 1.0))) {
                if (this.alreadyHit.contains(player.getUUID())) {
                    continue;
                }
                double dx = player.getX() - this.king.getX();
                double dz = player.getZ() - this.king.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance >= inner && distance <= outer) {
                    player.hurt(this.king.damageSources().indirectMagic(this.king, this.king), 6.0F);
                    double knockback = 0.9 / Math.max(0.1, distance);
                    player.push(dx * knockback, 0.35, dz * knockback);
                    this.alreadyHit.add(player.getUUID());
                }
            }
        }
    }
}
