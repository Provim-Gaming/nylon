/*
 * Nylon
 * Copyright (C) 2023, 2024 Provim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.provim.nylontest.entities;

import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.provim.nylon.api.AjEntity;
import org.provim.nylon.data.AjLoader;
import org.provim.nylon.data.model.nylon.NylonModel;
import org.provim.nylon.holders.entity.EntityHolder;
import org.provim.nylon.holders.entity.living.LivingEntityHolder;
import org.provim.nylontest.registries.SoundRegistry;
import org.provim.nylontest.util.AnimationHelper;

public class RedstoneGolem extends Monster implements AjEntity {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("provim", "redstone_golem");
    public static final NylonModel MODEL = AjLoader.require(ID);
    private final EntityHolder<RedstoneGolem> holder;

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 1.1)
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_KNOCKBACK, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 16.0);
    }

    @Override
    public EntityHolder<RedstoneGolem> getHolder() {
        return this.holder;
    }

    public RedstoneGolem(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);

        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.2, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity livingEntity) {
                if (this.getTicksUntilNextAttack() == 18) {
                    RedstoneGolem.this.holder.getAnimator().playAnimation("melee", 10);
                }
                super.checkAndPerformAttack(livingEntity);
            }
        });

        this.goalSelector.addGoal(3, new TemptGoal(this, 0.2, Ingredient.of(Items.REDSTONE, Items.REDSTONE_BLOCK), false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.2));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public int getBaseExperienceReward() {
        return 18;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtColor(this, this.holder);
        }
    }

    @Override
    @NotNull
    protected AABB getAttackBoundingBox() {
        AABB aABB = super.getAttackBoundingBox();
        return aABB.inflate(0.5, 0.0, 0.5);
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.STONE_STEP, 0.15F, 1.0F);
    }
}