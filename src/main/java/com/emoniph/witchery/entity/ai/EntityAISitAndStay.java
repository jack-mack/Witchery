package com.emoniph.witchery.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAISitAndStay extends EntityAIBase {

   private EntityTameable theEntity;


   public EntityAISitAndStay(EntityTameable par1EntityTameable) {
      this.theEntity = par1EntityTameable;
      this.setMutexBits(5);
   }

   @Override
   public boolean shouldExecute() {
      return this.theEntity.isSitting();
   }

   @Override
   public void startExecuting() {
      this.theEntity.getNavigator().clearPathEntity();
   }

   @Override
   public void resetTask() {}
}
