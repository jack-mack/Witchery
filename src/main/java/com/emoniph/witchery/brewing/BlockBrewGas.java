package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.brewing.ModifiersEffect;
import com.emoniph.witchery.brewing.TileEntityBrewFluid;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.EntityPosition;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class BlockBrewGas extends BlockBaseContainer implements IFluidBlock {

   public static final Material MATERIAL = (new Material(MapColor.airColor) {
      @Override
      public boolean isSolid() {
         return false;
      }
      @Override
      public boolean blocksMovement() {
         return false;
      }
   }).setReplaceable();


   public BlockBrewGas() {
      super(MATERIAL, TileEntityBrewFluid.class);
      super.registerWithCreateTab = false;
      this.setTickRandomly(true);
      this.disableStats();
      this.setBlockUnbreakable();
   }

   @Override
   public boolean isAir(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   @Override
   public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
      TileEntityBrewFluid gas = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
      return gas != null?gas.color:3385907;
   }

   @Override
   public int getRenderColor(int metadata) {
      return 3385907;
   }

   @Override
   public int getBlockColor() {
      return 3385907;
   }

   @Override
   public boolean renderAsNormalBlock() {
      return false;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
      return null;
   }

   @Override
   public boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_) {
      return false;
   }

   @Override
   public boolean isOpaqueCube() {
      return false;
   }

   @Override
   public Item getItemDropped(int metadata, Random rand, int fortune) {
      return null;
   }

   @Override
   public int quantityDropped(Random par1Random) {
      return 0;
   }

   @Override
   public int tickRate(World world) {
      return 5;
   }

   @Override
   public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
      Block block = world.getBlock(x, y, z);
      return block == this?false:super.shouldSideBeRendered(world, x, y, z, side);
   }

   @Override
   public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) {
      return false;
   }

   @Override
   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   @Override
   public Fluid getFluid() {
      return Witchery.Fluids.BREW_GAS;
   }

   @Override
   public int getRenderType() {
      return Witchery.proxy.getGasRenderId();
   }

   @Override
   public int getRenderBlockPass() {
      return 1;
   }

   @Override
   public void onBlockAdded(World world, int x, int y, int z) {
      world.scheduleBlockUpdate(x, y, z, this, 5);
   }

   public void onNeighborBlockChange(World world, int x, int y, int z, int side) {
      this.onBlockAdded(world, x, y, z);
   }

   @Override
   public FluidStack drain(World world, int x, int y, int z, boolean doDrain) {
      return null;
   }

   @Override
   public boolean canDrain(World world, int x, int y, int z) {
      return false;
   }

   @Override
   public float getFilledPercentage(World world, int x, int y, int z) {
      return 0.0F;
   }

   private boolean isTargetBlock(Block block) {
      return block != null && block != Blocks.air && block != this;
   }

   @Override
   public void updateTick(World world, int x, int y, int z, Random rand) {
      if(!world.isRemote) {
         int initialMetadata = world.getBlockMetadata(x, y, z);
         TileEntityBrewFluid oldTile = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if(oldTile == null) {
            world.setBlockToAir(x, y, z);
            return;
         }

         int maxMeta = oldTile.expansion;
         if(oldTile.incRunTicks() > 120) {
            world.setBlockToAir(x, y, z);
            return;
         }

         if(initialMetadata >= maxMeta) {
            label54: {
               if(oldTile != null) {
                  if(oldTile.duration != 0 && (oldTile.duration <= 0 || rand.nextInt(oldTile.duration) != 0)) {
                     break label54;
                  }
               } else if(rand.nextInt(40) != 0) {
                  break label54;
               }

               world.setBlockToAir(x, y, z);
               return;
            }
         } else {
            int metadata = initialMetadata;
            double[] pb = new double[]{0.2D, 0.4D, 0.8D, 0.8D, 0.8D, 0.8D};
            int[] dX = new int[]{0, 0, 1, -1, 0, 0};
            int[] dY = new int[]{1, -1, 0, 0, 0, 0};
            int[] dZ = new int[]{0, 0, 0, 0, 1, -1};
            boolean expanded = false;
            if(oldTile != null) {
               for(int i = 0; i < pb.length && metadata < maxMeta; ++i) {
                  if(rand.nextDouble() < pb[i]) {
                     int newX = x + dX[i];
                     int newY = y + dY[i];
                     int newZ = z + dZ[i];
                     Block block = world.getBlock(newX, newY, newZ);
                     if(block == Blocks.air || block == Blocks.snow_layer) {
                        world.setBlock(newX, newY, newZ, this, Math.min(metadata + 1, maxMeta), 3);
                        TileEntityBrewFluid newTile = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, newX, newY, newZ, TileEntityBrewFluid.class);
                        newTile.nbtEffect = (NBTTagCompound)oldTile.nbtEffect.copy();
                        newTile.expansion = oldTile.expansion;
                        newTile.color = oldTile.color;
                        newTile.duration = oldTile.duration;
                        newTile.thrower = oldTile.thrower;
                        expanded = true;
                     }
                  }
               }
            }

            if(expanded) {
               world.setBlockMetadataWithNotify(x, y, z, Math.min(metadata + 1, maxMeta), 3);
            }
         }

         world.scheduleBlockUpdate(x, y, z, this, 5);
      }

   }

   @Override
   public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
      if(entity != null && entity instanceof EntityLivingBase && !world.isRemote && world.rand.nextInt(10) == 4) {
         TileEntityBrewFluid gas = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if(gas != null && gas.nbtEffect != null) {
            EntityLivingBase living = (EntityLivingBase)entity;
            ModifiersEffect modifiers = new ModifiersEffect(0.25D, 0.5D, false, new EntityPosition(x, y, z), false, 0, world.getPlayerEntityByName(gas.thrower));
            modifiers.protectedFromNegativePotions = living.isPotionActive(Witchery.Potions.GAS_MASK);
            WitcheryBrewRegistry.INSTANCE.applyToEntity(world, living, gas.nbtEffect, modifiers);
         }
      }

   }

}
