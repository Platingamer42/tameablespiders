package de.platin42.tameablespider;

import de.platin42.tameablespider.entity.TameableSpiderEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CustomOwnerHurtTargetGoal extends TargetGoal {
    private final TameableSpiderEntity spider;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public CustomOwnerHurtTargetGoal(TameableSpiderEntity spider) {
        super(spider, false);
        this.spider = spider;
    }

    @Override
    public boolean canUse() {
        if (this.spider.getOwnerUUID() == null) {
            return false;
        }
        LivingEntity owner = this.spider.getOwner();
        if (owner == null) {
            return false;
        }

        this.ownerLastHurt = owner.getLastHurtMob();
        int currentTimestamp = owner.getLastHurtMobTimestamp();

        return currentTimestamp != this.timestamp
                && this.ownerLastHurt != null
                && this.ownerLastHurt.isAlive()
                && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT.ignoreLineOfSight());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.spider.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}