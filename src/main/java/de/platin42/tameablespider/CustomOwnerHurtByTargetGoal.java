package de.platin42.tameablespider;

import de.platin42.tameablespider.entity.TameableSpiderEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class CustomOwnerHurtByTargetGoal extends TargetGoal {
    private final TameableSpiderEntity spider;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public CustomOwnerHurtByTargetGoal(TameableSpiderEntity spider) {
        super(spider, false);
        this.spider = spider;
    }

    @Override
    public boolean canUse() {
        if (!this.spider.isTamed()) {
            return false;
        }
        LivingEntity owner = this.spider.getOwner();
        if (owner == null) {
            return false;
        }
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int i = owner.getLastHurtByMobTimestamp();
        return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.spider.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
