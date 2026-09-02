package net.cobbleservertools.block;

import com.mojang.serialization.MapCodec;
import net.cobbleservertools.block.entity.VendingMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CeladonVendingMachineBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final MapCodec<CeladonVendingMachineBlock> CODEC = simpleCodec(CeladonVendingMachineBlock::new);
   private static final double[][] NORTH_BOXES = new double[][]{
      {0.0, 2.0, 0.0, 16.0, 4.0, 16.0},
      {13.0, 4.0, 0.0, 16.0, 8.0, 16.0},
      {0.0, 4.0, 0.0, 7.0, 8.0, 16.0},
      {6.0, 4.0, 6.0, 13.0, 8.0, 16.0},
      {0.0, 8.0, 0.0, 16.0, 32.0, 16.0},
      {12.0, 0.0, 0.0, 16.0, 2.0, 16.0},
      {0.0, 0.0, 0.0, 4.0, 2.0, 16.0}
   };
   private static final VoxelShape NORTH = buildRotatedShape(0);
   private static final VoxelShape EAST = buildRotatedShape(1);
   private static final VoxelShape SOUTH = buildRotatedShape(2);
   private static final VoxelShape WEST = buildRotatedShape(3);

   public CeladonVendingMachineBlock(Properties var1) {
      super(var1);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new VendingMachineBlockEntity(var1, var2);
   }

   public void createBlockStateDefinition(Builder<Block, BlockState> var1) {
      var1.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext var1) {
      return (BlockState)this.defaultBlockState().setValue(FACING, var1.getHorizontalDirection().getOpposite());
   }

   public RenderShape getRenderShape(BlockState var1) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      Direction var5 = (Direction)var1.getValue(FACING);

      return switch (var5) {
         case EAST -> EAST;
         case SOUTH -> SOUTH;
         case WEST -> WEST;
         default -> NORTH;
      };
   }

   private static VoxelShape buildRotatedShape(int var0) {
      VoxelShape var1 = Shapes.empty();

      for (double[] var5 : NORTH_BOXES) {
         double var6 = var5[0];
         double var8 = var5[1];
         double var10 = var5[2];
         double var12 = var5[3];
         double var14 = var5[4];
         double var16 = var5[5];

         for (int var18 = 0; var18 < var0; var18++) {
            double var19 = 16.0 - var16;
            double var21 = var6;
            double var23 = 16.0 - var10;
            double var25 = var12;
            var6 = var19;
            var10 = var21;
            var12 = var23;
            var16 = var25;
         }

         var1 = Shapes.or(var1, box(var6, var8, var10, var12, var14, var16));
      }

      return var1;
   }

   public InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      if (!var2.isClientSide && var4 instanceof ServerPlayer var6 && var2.getBlockEntity(var3) instanceof VendingMachineBlockEntity var7) {
         var7.openFor(var6);
      }

      return InteractionResult.SUCCESS;
   }

   public void onRemove(BlockState var1, Level var2, BlockPos var3, BlockState var4, boolean var5) {
      if (!var1.is(var4.getBlock())) {
         if (!var2.isClientSide && var2.getBlockEntity(var3) instanceof VendingMachineBlockEntity var6) {
            var6.discardNpc();
         }

         super.onRemove(var1, var2, var3, var4, var5);
      }
   }
}
