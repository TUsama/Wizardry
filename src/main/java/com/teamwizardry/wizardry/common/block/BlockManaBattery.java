package com.teamwizardry.wizardry.common.block;

import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.block.IManaAcceptor;
import com.teamwizardry.wizardry.common.tile.TileManaBattery;
import com.teamwizardry.wizardry.common.tile.TilePedestal;
import com.teamwizardry.wizardry.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class BlockManaBattery extends Block implements ITileEntityProvider, IManaAcceptor {

	public BlockManaBattery() {
		super(Material.GROUND);
		setUnlocalizedName("mana_battery");
		setRegistryName("mana_battery");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this), getRegistryName());
		GameRegistry.registerTileEntity(TileManaBattery.class, "mana_battery");
		setCreativeTab(Wizardry.tab);
		setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileManaBattery();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		ArrayList<BlockPos> pedestals = new ArrayList<>();
		for (int i = -6; i < 6; i++) {
			for (int j = -6; j < 6; j++) {

				BlockPos pedPos = new BlockPos(pos.getX() + i, pos.getY() - 2, pos.getZ() + j);
				IBlockState block = worldIn.getBlockState(pedPos);
				if (block.getBlock() != ModBlocks.PEDESTAL) continue;
				TilePedestal pedestal = (TilePedestal) worldIn.getTileEntity(pedPos);
				if (pedestals.contains(pedPos)) continue;
				if (pedestal == null) return false;
				if (pedestal.getManaPearl() == null) return false;

				BlockPos oppPos = new BlockPos(pos.getX() - i, pedPos.getY(), pos.getZ() - j);
				IBlockState oppBlock = worldIn.getBlockState(oppPos);
				if (oppBlock.getBlock() != ModBlocks.PEDESTAL) return false;
				TilePedestal oppPed = (TilePedestal) worldIn.getTileEntity(oppPos);
				if (pedestals.contains(oppPos)) continue;
				if (oppPed == null) return false;
				if (oppPed.getManaPearl() == null) return false;

				pedestals.add(pedPos);
				pedestals.add(oppPos);
			}
		}
		return !pedestals.isEmpty();

	}

	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		this.updateTick(worldIn, pos, state, random);

	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
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
}
