package de.platin42.tameablespider.entity;
import de.platin42.tameablespider.CustomOwnerHurtByTargetGoal;
import de.platin42.tameablespider.CustomOwnerHurtTargetGoal;
import de.platin42.tameablespider.TameableSpiderMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Optional;
import java.util.UUID;



public class TameableSpiderEntity extends Spider implements OwnableEntity {
    private static final EntityDataAccessor<@NotNull Boolean> DATA_TAMED =
            SynchedEntityData.defineId(TameableSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<@NotNull Optional<EntityReference<@NotNull LivingEntity>>> DATA_OWNER =
            SynchedEntityData.defineId(TameableSpiderEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);


    public  TameableSpiderEntity(EntityType<? extends @NotNull Spider> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.TEMPT_RANGE, 10.0)
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        // super.registerGoals(); dont register super goals (too much existing logic; allthough _this_ is weird, as well?)

        // Normal AI Goals
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, Ingredient.of(Items.SPIDER_EYE, Items.ROTTEN_FLESH), false));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Target Selection: Owner defense first, self-defense second, untamed hostility last
        this.targetSelector.addGoal(1, new CustomOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CustomOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this,
                Player.class,
                10,
                true,
                false,
                (target, level) -> !this.isTamed()
                        && level.getMaxLocalRawBrightness(target.blockPosition()) < 8        ));
    }

    //tempting and taming are two coins, whoops
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        TameableSpiderMod.LOGGER.info(String.valueOf(this.getHealth()));


        if (!this.isTamed()) {
            if (stack.is(Items.SPIDER_EYE) || stack.is(Items.DIAMOND)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                if (!this.level().isClientSide()) {
                    if (this.random.nextInt(3) == 0) { // 33% chance to tame //todo, maybe offset them, bc their ai makes them endless-spin
                        this.setTamed(true);
                        this.setHomeTo(player.blockPosition(), 16);
                        this.setOwnerUUID(player.getUUID());
                        this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // Smoke particles
                    }
                }
                return InteractionResult.SUCCESS;
            }

        } else { //tamed interactions
            //heal:
            if (stack.is(Items.SPIDER_EYE) || stack.is(Items.DIAMOND) && this.getHealth() < 16) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(1f);
                this.level().broadcastEntityEvent(this, (byte) 7); // Heart particles
            } else {
                return InteractionResult.SUCCESS;
            }
            //todo sit/follow logic?

        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        //dont attack the owner
        if (this.isTamed() && target.getUUID().equals(this.getOwnerUUID())) {
            return false;
        }
        //friendly fire off
        if (this.isTamed() && target instanceof TameableSpiderEntity otherSpider && otherSpider.isTamed()) {
            return false;
        }

        return super.canAttack(target);
    }


    //Save/Load State (NBT) of the entity
    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Tamed", this.isTamed());
        if (this.getOwnerUUID() != null) {
            output.store("Owner", UUIDUtil.CODEC, this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setTamed(input.getBooleanOr("Tamed", false));

        input.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);
    }

    //set synched data so it can initialize (or smth?)
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAMED, false);
        builder.define(DATA_OWNER, Optional.empty());
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) { // Erfolg (Herzen)
            this.spawnTamingParticles(true);
        } else if (id == 6) { // Fehlschlag (Rauch)
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    //State Getters & Setters
    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_TAMED, tamed);
        if (tamed) {
            this.setTarget(null);          // Aborts active pathfinding/attacking
            this.setLastHurtByMob(null);   // Clears "who hit me" memory
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER)
                .map(EntityReference::getUUID)
                .orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            this.entityData.set(DATA_OWNER, Optional.empty());
        } else {
            // EntityReference aus der UUID erzeugen:
            this.entityData.set(DATA_OWNER, Optional.of(EntityReference.of(uuid)));
        }
    }

    @Override
    public @Nullable EntityReference<@NotNull LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNER).orElse(null);
    }

    //(helper)functions
    protected void spawnTamingParticles(boolean success) {
        var particle = success ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; ++i) {
            double vx = this.random.nextGaussian() * 0.02D;
            double vy = this.random.nextGaussian() * 0.02D;
            double vz = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(
                    particle,
                    this.getRandomX(1.0D),
                    this.getRandomY() + 0.5D,
                    this.getRandomZ(1.0D),
                    vx, vy, vz
            );
        }
    }


}
