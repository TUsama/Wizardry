package com.teamwizardry.wizardry.crafting.irecipies;

import com.teamwizardry.wizardry.api.capability.player.mana.ManaManager;
import com.teamwizardry.wizardry.common.block.fluid.ModFluids;
import com.teamwizardry.wizardry.init.ModItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RecipeManaSyringe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
		boolean foundSyringe = false;
		boolean foundMana = false;

		ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(ModFluids.MANA.getActual(), 1));

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.getItem() == ModItems.SYRINGE && stack.getItemDamage() == 0) {
				foundSyringe = true;
			} else if (stack.getItem() == ModItems.ORB && ManaManager.isManaFull(stack)) {
				foundMana = true;
			} else if (ItemStack.areItemStacksEqual(bucket, stack))
				foundMana = true;
		}
		return foundSyringe && foundMana;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		return new ItemStack(ModItems.SYRINGE, 1, 1);
	}

	@Override
	public boolean canFit(int width, int height) {
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
}
