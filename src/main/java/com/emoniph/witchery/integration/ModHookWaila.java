package com.emoniph.witchery.integration;

import com.emoniph.witchery.integration.ModHook;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.entity.EntityLivingBase;

public class ModHookWaila extends ModHook {

   @Override
   public String getModID() {
      return "Waila";
   }

   @Override
   protected void doInit() {
      FMLInterModComms.sendMessage(this.getModID(), "register", "com.emoniph.witchery.integration.ModHookWailaRegistrar.callbackRegister");
   }

   @Override
   protected void doPostInit() {}

   @Override
   protected void doReduceMagicPower(EntityLivingBase entity, float factor) {}
}
