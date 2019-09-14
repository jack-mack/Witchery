package com.emoniph.witchery.blocks;

import com.emoniph.witchery.blocks.BlockBase;
import com.emoniph.witchery.util.ParticleEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGlowGlobe extends BlockBase {

   public BlockGlowGlobe() {
      super(Material.glass);
      super.registerWithCreateTab = false;
      this.setHardness(0.0F);
      this.setLightLevel(0.9375F);
      this.disableStats();
      float f = 0.1F;
      this.setBlockBounds(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F);
   }

   @Override
   public boolean isOpaqueCube() {
      return false;
   }

   @Override
   public boolean renderAsNormalBlock() {
      return false;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
      return null;
   }

   @Override
   public int getRenderType() {
      return 1;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public int getRenderBlockPass() {
      return 1;
   }

   @Override
   public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return true;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
      if(rand.nextInt(3) != 0) {
         double d0 = (double)((float)x + 0.45F + (float)rand.nextInt(3) * 0.05F);
         double d1 = (double)((float)y + 0.4F + (float)rand.nextInt(4) * 0.1F);
         double d2 = (double)((float)z + 0.45F + (float)rand.nextInt(3) * 0.05F);
         world.spawnParticle(ParticleEffect.FLAME.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   @Override
   public int quantityDropped(Random par1Random) {
      return 0;
   }

   @Override
   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }
}
