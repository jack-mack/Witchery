package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityTreefyd;
import net.minecraft.entity.ai.EntityAIWander;

public class EntityAITreefydWander extends EntityAIWander {

   private final EntityTreefyd treefyd;


   public EntityAITreefydWander(EntityTreefyd treefyd, double speed) {
      super(treefyd, speed);
      this.treefyd = treefyd;
   }

   @Override
   public boolean shouldExecute() {
      return !this.treefyd.isSentinal() && super.shouldExecute();
   }

   @Override
   public boolean continueExecuting() {
      return !this.treefyd.isSentinal() && super.continueExecuting();
   }
}
