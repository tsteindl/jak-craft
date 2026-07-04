package com.tsteindl.jakcraft;

import com.yellowbrossproductions.illageandspillage.entities.IllashooterEntity;
import com.yellowbrossproductions.illageandspillage.entities.MagispellerEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class MuellagerKingEntity extends MagispellerEntity {

    private static final int ATTACK_LINE_GAP_TICKS = 200; // throttle generic attack chatter (~10s)
    private static final int PASSENBRUNNER_LINE_GAP_TICKS = 100;
    private static final int LIFESTEAL_ATTACK_TYPE = 2;

    private static final double VICTORY_PLAYER_RANGE = 64.0;

    private boolean announcedFightStart = false;
    private boolean victoryScheduled = false;
    private int lastAttackType = 0;
    private int fourierCooldown = 0;
    private long lastAttackLineTime = -10000L;
    private long lastPassenbrunnerLineTime = -10000L;
    // Health can only ratchet down: the king never heals (life-steal / self-heal disabled).
    private float healthCap = -1.0F;

    // Our own always-visible boss bar (the parent's bar stays hidden, so we suppress it and use this).
    private final ServerBossEvent bossBar =
        (ServerBossEvent) new ServerBossEvent(Component.literal("Müller"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)
            .setDarkenScreen(true);

    public MuellagerKingEntity(EntityType<? extends MagispellerEntity> type, Level level) {
        super(type, level);
    }

    // +50% max health on top of Illage & Spillage's configured base health. Applied as a
    // MULTIPLY_TOTAL modifier so it scales whatever base value the parent sets (now and each tick).
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("a1b2c3d4-e5f6-4708-9a1b-2c3d4e5f6071");
    private static final double HEALTH_BOOST = 0.5D;

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getModifier(HEALTH_BOOST_UUID) == null) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                HEALTH_BOOST_UUID, "Muellager health boost", HEALTH_BOOST, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        this.setHealth(this.getMaxHealth());
        return result;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Remove parent attacks we disable/replace. NOTE: getAvailableGoals() returns a COPY here,
        // so removeIf on it does nothing - we must call goalSelector.removeGoal() on the real set.
        // We keep DispenserGoal (its "Illashooter" minions become our Passenbrunner) and FakersGoal
        // (the clone / "two kings" - intentional).
        List<Goal> toRemove = new ArrayList<>();
        for (WrappedGoal wrapped : this.goalSelector.getAvailableGoals()) {
            String name = wrapped.getGoal().getClass().getSimpleName().toLowerCase();
            if (name.contains("lifesteal") || name.contains("heal")
                || name.contains("summonvexes")) {
                toRemove.add(wrapped.getGoal());
            }
        }
        toRemove.forEach(this.goalSelector::removeGoal);
        this.goalSelector.addGoal(1, new FourierWaveGoal(this));
        // Lechner = flying vexes (Passenbrunner are the chest/Illashooter minions, named in customServerAiStep).
        this.goalSelector.addGoal(2, new SummonHelpersGoal(this, "Lechner", Config.LECHNER_LINES::get, SummonHelpersGoal.VEX));
    }

    // The parent plays goofy boss music; disable it.
    @Override
    protected boolean canPlayMusic() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.removePlayer(player); // never show the parent's bar; we only use our own
        this.bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void tick() {
        // Cancel any life-steal attack state before the parent's tick logic can act on it.
        if (!this.level().isClientSide && this.getAttackType() == LIFESTEAL_ATTACK_TYPE) {
            this.setAttackType(0);
        }
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.getAttackType() == LIFESTEAL_ATTACK_TYPE) {
            this.setAttackType(0);
        }
        // Never let health increase (bullet-proof against life-steal / self-heal).
        float health = this.getHealth();
        if (this.healthCap < 0.0F) {
            this.healthCap = health;
        } else if (health > this.healthCap) {
            this.setHealth(this.healthCap);
        } else {
            this.healthCap = health;
        }
        // Suppress the parent's boss bar completely (hide it AND clear its players, so it can never
        // show up as a second bar) and drive our own. Runs last so it wins each tick.
        this.bossEvent.setVisible(false);
        this.bossEvent.removeAllPlayers();
        this.bossBar.setName(this.getDisplayName());
        this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // Never be a pillager "captain": strip the ominous banner and patrol-leader status so the
        // king never spawns/renders with the captain banner on its head.
        if (this.isPatrolLeader()) {
            this.setPatrolLeader(false);
        }
        if (this.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BannerItem) {
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        if (this.fourierCooldown > 0) {
            this.fourierCooldown--;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            long gameTime = serverLevel.getGameTime();

            // The king should never slow you down: strip slowness from nearby players.
            for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(24.0))) {
                if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                }
            }

            // Name the chest minions "Passenbrunner" and announce them with a voice line.
            boolean namedNew = false;
            for (IllashooterEntity shooter : serverLevel.getEntitiesOfClass(IllashooterEntity.class, this.getBoundingBox().inflate(32.0))) {
                if (shooter.getCustomName() == null) {
                    shooter.setCustomName(Component.literal("Passenbrunner"));
                    shooter.setCustomNameVisible(true);
                    namedNew = true;
                }
            }
            if (namedNew && gameTime - this.lastPassenbrunnerLineTime >= PASSENBRUNNER_LINE_GAP_TICKS) {
                this.say(Config.PASSENBRUNNER_LINES.get());
                this.lastPassenbrunnerLineTime = gameTime;
            }
        }

        // Fight start: first time the king acquires a target.
        if (!this.announcedFightStart && this.getTarget() != null) {
            this.announcedFightStart = true;
            this.say(Config.FIGHT_START_LINES.get());
        }

        // Generic "on attack" line, throttled so it doesn't spam chat.
        int attackType = this.getAttackType();
        if (attackType != 0 && this.lastAttackType == 0
                && this.level().getGameTime() - this.lastAttackLineTime >= ATTACK_LINE_GAP_TICKS) {
            this.say(Config.ATTACK_LINES.get());
            this.lastAttackLineTime = this.level().getGameTime();
        }
        this.lastAttackType = attackType;
    }

    @Override
    public void die(DamageSource source) {
        this.say(Config.DEATH_LINES.get());
        // Once the king falls to a player, teleport the fighters to the birthday platform after a
        // delay (time to grab the dropped PhD scroll). The lastHurtByPlayerTime check means a plain
        // /kill (e.g. during a map reset) neither drops the scroll nor triggers the teleport.
        if (!this.victoryScheduled && this.lastHurtByPlayerTime > 0
                && this.level() instanceof ServerLevel serverLevel) {
            this.victoryScheduled = true;
            List<ServerPlayer> nearby = serverLevel.getEntitiesOfClass(
                ServerPlayer.class, this.getBoundingBox().inflate(VICTORY_PLAYER_RANGE));
            PopeStoryHandler.scheduleVictoryTeleport(serverLevel, nearby);
        }
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
            this.king.say(Config.FOURIER_LINES.get());
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

    // Summons a wave of named flying "Lechner" vexes that fight for the king, with a voice line.
    static class SummonHelpersGoal extends Goal {

        @FunctionalInterface
        interface Spawner {
            Mob spawn(ServerLevel level, MuellagerKingEntity king, double x, double z);
        }

        static final Spawner VEX = (level, king, x, z) -> {
            Vex vex = EntityType.VEX.create(level);
            if (vex == null) {
                return null;
            }
            double y = king.getY() + 1.0 + king.getRandom().nextInt(3);
            vex.moveTo(x, y, z, king.getRandom().nextFloat() * 360.0F, 0.0F);
            vex.finalizeSpawn(level, level.getCurrentDifficultyAt(king.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            vex.setOwner(king);
            vex.setBoundOrigin(king.blockPosition());
            vex.setLimitedLife(20 * (30 + king.getRandom().nextInt(30)));
            return vex;
        };

        // Kept for easy reuse if a ground/ranged summon is wanted again.
        static final Spawner PILLAGER = (level, king, x, z) -> {
            Pillager pillager = EntityType.PILLAGER.create(level);
            if (pillager == null) {
                return null;
            }
            pillager.moveTo(x, king.getY(), z, king.getRandom().nextFloat() * 360.0F, 0.0F);
            pillager.finalizeSpawn(level, level.getCurrentDifficultyAt(pillager.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
            pillager.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            return pillager;
        };

        private static final int COOLDOWN_TICKS = 320;
        private static final int MIN_COUNT = 2;
        private static final int MAX_COUNT = 4;

        private final MuellagerKingEntity king;
        private final String minionName;
        private final Supplier<List<? extends String>> lines;
        private final Spawner spawner;
        private long nextUseTime;

        SummonHelpersGoal(MuellagerKingEntity king, String minionName, Supplier<List<? extends String>> lines, Spawner spawner) {
            this.king = king;
            this.minionName = minionName;
            this.lines = lines;
            this.spawner = spawner;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (this.king.getAttackType() != 0) {
                return false; // don't interrupt the parent's spells
            }
            if (this.king.level().getGameTime() < this.nextUseTime) {
                return false;
            }
            if (this.king.distanceToSqr(target) > 24.0 * 24.0) {
                return false;
            }
            return this.king.getRandom().nextInt(100) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false; // instant cast
        }

        @Override
        public void start() {
            this.nextUseTime = this.king.level().getGameTime() + COOLDOWN_TICKS;
            this.king.say(this.lines.get());
            if (!(this.king.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            LivingEntity target = this.king.getTarget();
            int count = MIN_COUNT + this.king.getRandom().nextInt(MAX_COUNT - MIN_COUNT + 1);
            for (int i = 0; i < count; i++) {
                double x = this.king.getX() + (this.king.getRandom().nextDouble() - 0.5) * 4.0;
                double z = this.king.getZ() + (this.king.getRandom().nextDouble() - 0.5) * 4.0;
                Mob minion = this.spawner.spawn(serverLevel, this.king, x, z);
                if (minion == null) {
                    continue;
                }
                minion.setCustomName(Component.literal(this.minionName));
                minion.setCustomNameVisible(true);
                if (target != null) {
                    minion.setTarget(target);
                }
                serverLevel.addFreshEntity(minion);
            }
        }
    }
}
