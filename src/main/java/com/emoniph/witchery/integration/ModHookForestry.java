package com.emoniph.witchery.integration;

import com.emoniph.witchery.integration.ModHook;
import net.minecraft.entity.EntityLivingBase;

public class ModHookForestry extends ModHook {

   @Override
   public String getModID() {
      return "Forestry";
   }

   @Override
   protected void doInit() {}

   @Override
   protected void doPostInit() {}

   @Override
   protected void doReduceMagicPower(EntityLivingBase entity, float factor) {}
}
