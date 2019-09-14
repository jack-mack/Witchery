package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityParasyticLouse extends EntityMob {

   public EntityParasyticLouse(World par1World) {
      super(par1World);
      this.setSize(0.3F, 0.7F);
   }

   @Override
   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(4.0D);
      this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.6000000238418579D);
      this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(0.0D);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      super.dataWatcher.addObject(20, new Integer(0));
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
      super.writeEntityToNBT(par1NBTTagCompound);
      par1NBTTagCompound.setInteger("BitePotionEffect", this.getBitePotionEffect());
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
      super.readEntityFromNBT(par1NBTTagCompound);
      if(par1NBTTagCompound.hasKey("BitePotionEffect")) {
         this.setBitePotionEffect(par1NBTTagCompound.getInteger("BitePotionEffect"));
      }

   }

   public int getBitePotionEffect() {
      return super.dataWatcher.getWatchableObjectInt(20);
   }

   public void setBitePotionEffect(int par1) {
      super.dataWatcher.updateObject(20, Integer.valueOf(par1));
   }

   @Override
   public String getCommandSenderName() {
      return this.hasCustomNameTag()?this.getCustomNameTag():StatCollector.translateToLocal("entity.witchery.louse.name");
   }

   @Override
   protected boolean canTriggerWalking() {
      return false;
   }

   @Override
   protected Entity findPlayerToAttack() {
      double d0 = 8.0D;
      return super.worldObj.getClosestVulnerablePlayerToEntity(this, d0);
   }

   @Override
   protected String getLivingSound() {
      return "mob.silverfish.say";
   }

   @Override
   protected String getHurtSound() {
      return "mob.silverfish.hit";
   }

   @Override
   protected String getDeathSound() {
      return "mob.silverfish.kill";
   }

   @Override
   public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
      return this.isEntityInvulnerable()?false:super.attackEntityFrom(par1DamageSource, par2);
   }

   @Override
   protected void attackEntity(Entity par1Entity, float par2) {
      if(super.attackTime <= 0 && par2 < 1.2F && par1Entity.boundingBox.maxY > super.boundingBox.minY && par1Entity.boundingBox.minY < super.boundingBox.maxY) {
         super.attackTime = 20;
         this.attackEntityAsMob(par1Entity);
         if(par1Entity instanceof EntityLivingBase && !super.worldObj.isRemote) {
            EntityLivingBase living = (EntityLivingBase)par1Entity;
            int potionEffect = this.getBitePotionEffect();
            if(potionEffect > 0) {
               List list = Items.potionitem.getEffects(potionEffect);
               if(list != null && !list.isEmpty()) {
                  PotionEffect effect = new PotionEffect((PotionEffect)list.get(0));
                  living.addPotionEffect(effect);
               }

               this.setBitePotionEffect(0);
            }
         }
      }

   }

   @Override
   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.playSound("mob.silverfish.step", 0.15F, 1.0F);
   }

   @Override
   protected Item getDropItem() {
      return null;
   }

   @Override
   public void onUpdate() {
      super.renderYawOffset = super.rotationYaw;
      super.onUpdate();
   }

   @Override
   protected void updateEntityActionState() {
      super.updateEntityActionState();
      if(!super.worldObj.isRemote && super.entityToAttack != null && !this.hasPath()) {
         super.entityToAttack = null;
      }

   }

   @Override
   public float getBlockPathWeight(int par1, int par2, int par3) {
      return super.worldObj.getBlock(par1, par2 - 1, par3) == Blocks.stone?10.0F:super.getBlockPathWeight(par1, par2, par3);
   }

   @Override
   protected boolean isValidLightLevel() {
      return true;
   }

   @Override
   public boolean getCanSpawnHere() {
      if(super.getCanSpawnHere()) {
         EntityPlayer entityplayer = super.worldObj.getClosestPlayerToEntity(this, 5.0D);
         return entityplayer == null;
      } else {
         return false;
      }
   }

   @Override
   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.ARTHROPOD;
   }

   @Override
   protected boolean interact(EntityPlayer player) {
      this.setDead();
      if(!super.worldObj.isRemote) {
         ItemStack stack = new ItemStack(Witchery.Items.PARASYTIC_LOUSE);
         EntityItem item = new EntityItem(super.worldObj, super.posX, 0.4D + super.posY, super.posZ, stack);
         super.worldObj.spawnEntityInWorld(item);
         return true;
      } else {
         return super.interact(player);
      }
   }
}
