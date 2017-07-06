package com.teamwizardry.wizardry.common.block.wisdomwood;

import com.teamwizardry.librarianlib.features.base.block.BlockModDoor;
import com.teamwizardry.wizardry.init.ModBlocks;
import net.minecraft.block.SoundType;

/**
 * Created by LordSaad.
 */
public class BlockWisdomWoodDoor extends BlockModDoor {

	public BlockWisdomWoodDoor() {
		super("wisdom_wood_door", ModBlocks.WISDOM_WOOD_PLANKS.getDefaultState());
		setSoundType(SoundType.WOOD);
	}
}
