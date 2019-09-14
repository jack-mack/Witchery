package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityCovenWitch;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

public class EntityAIMoveToRestrictionAndSit extends EntityAIBase {

   private EntityCovenWitch theEntity;
   private double movePosX;
   private double movePosY;
   private double movePosZ;
   private double movementSpeed;


   public EntityAIMoveToRestrictionAndSit(EntityCovenWitch p_i2347_1_, double p_i2347_2_) {
      this.theEntity = p_i2347_1_;
      this.movementSpeed = p_i2347_2_;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if(this.theEntity.isWithinHomeDistanceCurrentPosition()) {
         return false;
      } else {
         ChunkCoordinates chunkcoordinates = this.theEntity.getHomePosition();
         Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 16, 7, Vec3.createVectorHelper((double)chunkcoordinates.posX, (double)chunkcoordinates.posY, (double)chunkcoordinates.posZ));
         if(vec3 == null) {
            return false;
         } else {
            this.movePosX = vec3.xCoord;
            this.movePosY = vec3.yCoord;
            this.movePosZ = vec3.zCoord;
            return true;
         }
      }
   }

   @Override
   public boolean continueExecuting() {
      return !this.theEntity.getNavigator().noPath();
   }

   @Override
   public void startExecuting() {
      this.theEntity.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
   }

   @Override
   public void resetTask() {
      super.resetTask();
      if(this.theEntity.isWithinHomeDistanceCurrentPosition()) {
         this.theEntity.standStill();
      }

   }
}
