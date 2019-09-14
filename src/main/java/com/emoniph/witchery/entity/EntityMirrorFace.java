package com.emoniph.witchery.entity;

import com.emoniph.witchery.util.TimeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityMirrorFace extends EntityLiving {

   private long ticksAlive = 0L;


   public EntityMirrorFace(World world) {
      super(world);
      super.isImmuneToFire = true;
      this.setSize(0.5F, 0.5F);
      super.noClip = true;
      super.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F, 0.4F));
   }

   @Override
   protected void entityInit() {
      super.entityInit();
   }

   @Override
   protected int decreaseAirSupply(int air) {
      return air;
   }

   @Override
   protected float getSoundVolume() {
      return 0.8F;
   }

   @Override
   protected float getSoundPitch() {
      return 1.0F;
   }

   @Override
   public int getTalkInterval() {
      return 80;
   }

   @Override
   protected String getLivingSound() {
      return null;
   }

   @Override
   protected String getHurtSound() {
      return null;
   }

   @Override
   protected String getDeathSound() {
      return null;
   }

   @Override
   public boolean canBePushed() {
      return false;
   }

   @Override
   protected void collideWithEntity(Entity par1Entity) {}

   @Override
   protected void collideWithNearbyEntities() {}

   @Override
   protected boolean isAIEnabled() {
      return true;
   }

   @Override
   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.ticksAlive = Math.max((long)super.ticksExisted, ++this.ticksAlive);
      if(this.ticksAlive > (long)TimeUtil.secsToTicks(10) && !super.worldObj.isRemote) {
         this.setDead();
      }

   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      super.motionY = 0.0D;
   }

   @Override
   protected void dropFewItems(boolean par1, int par2) {}

   @Override
   protected boolean canTriggerWalking() {
      return false;
   }

   @Override
   protected void fall(float par1) {}

   @Override
   protected void updateFallState(double par1, boolean par3) {}

   @Override
   public boolean doesEntityNotTriggerPressurePlate() {
      return true;
   }

   @Override
   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      return false;
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbtRoot) {
      super.readEntityFromNBT(nbtRoot);
      this.ticksAlive = nbtRoot.getLong("WITCTicksAlive");
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbtRoot) {
      super.writeEntityToNBT(nbtRoot);
      nbtRoot.setLong("WITCTicksAlive", this.ticksAlive);
   }
}
