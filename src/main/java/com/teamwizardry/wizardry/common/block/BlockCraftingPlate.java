package com.teamwizardry.wizardry.common.block;

import com.teamwizardry.librarianlib.features.base.block.BlockModContainer;
import com.teamwizardry.librarianlib.features.structure.Structure;
import com.teamwizardry.wizardry.api.block.IStructure;
import com.teamwizardry.wizardry.api.render.ClusterObject;
import com.teamwizardry.wizardry.client.render.block.TileCraftingPlateRenderer;
import com.teamwizardry.wizardry.common.tile.TileCraftingPlate;
import com.teamwizardry.wizardry.init.ModItems;
import com.teamwizardry.wizardry.init.ModStructures;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Saad on 6/10/2016.
 */
public class BlockCraftingPlate extends BlockModContainer implements IStructure {

	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.725, 0.875);

	public BlockCraftingPlate() {
		super("crafting_plate", Material.ROCK);
		setHardness(1.0F);
		setSoundType(SoundType.STONE);
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileCraftingPlate.class, new TileCraftingPlateRenderer());
	}

	@Override
	public int getLightValue(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
		return 15;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState iBlockState) {
		return new TileCraftingPlate();
	}

	private TileCraftingPlate getTE(World world, BlockPos pos) {
		return (TileCraftingPlate) world.getTileEntity(pos);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (!worldIn.isRemote) {
			if (tickStructure(worldIn, playerIn, pos)) {
				if (!(playerIn.getHeldItemMainhand().getItem() == ModItems.MAGIC_WAND)) {
					TileCraftingPlate plate = getTE(worldIn, pos);
					if (plate.isCrafting) return false;
					if (!heldItem.isEmpty()) {
						ItemStack stack = heldItem.copy();
						stack.setCount(1);
						heldItem.setCount(heldItem.getCount() - 1);
						plate.inventory.add(new ClusterObject(plate, stack, worldIn, plate.random));
						playerIn.openContainer.detectAndSendChanges();

					} else if (plate.output != null) {
						playerIn.setHeldItem(hand, plate.output.copy());
						plate.output = null;
						playerIn.openContainer.detectAndSendChanges();

					} else if (!plate.inventory.isEmpty()) {
						playerIn.setHeldItem(hand, plate.inventory.remove(plate.inventory.size() - 1).stack);
						playerIn.openContainer.detectAndSendChanges();
					}
					worldIn.notifyBlockUpdate(pos, state, state, 3);
					return true;
				}
				return false;
			} else return false;
		}
		return true;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public Structure getStructure() {
		return ModStructures.INSTANCE.structures.get("crafting_altar");
	}

	@Override
	public Vec3i offsetToCenter() {
		return new Vec3i(6, 2, 6);
	}
}
