package com.emoniph.witchery.integration;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.integration.ModHook;
import net.minecraft.entity.EntityLivingBase;

public class ModHookTinkersConstruct extends ModHook {

   @Override
   public String getModID() {
      return "TConstruct";
   }

   @Override
   protected void doInit() {
      Witchery.modHooks.isTinkersPresent = true;
   }

   @Override
   protected void doPostInit() {}

   @Override
   protected void doReduceMagicPower(EntityLivingBase entity, float factor) {}
}
