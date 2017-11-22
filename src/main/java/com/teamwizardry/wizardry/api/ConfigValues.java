package com.teamwizardry.wizardry.api;

import com.teamwizardry.librarianlib.features.config.ConfigDoubleRange;
import com.teamwizardry.librarianlib.features.config.ConfigIntRange;
import com.teamwizardry.librarianlib.features.config.ConfigProperty;

/**
 * Created by LordSaad.
 */
public class ConfigValues {

	@ConfigProperty(category = "general", comment = "If enabled, will inform you of new updates to the mod.")
	public static boolean versionCheckerEnabled = true;

	@ConfigProperty(category = "world", comment = "Whitelisted dimensions for mana pool generation.")
	public static int[] manaPoolDimWhitelist = {0};
	
	@ConfigIntRange(min = 0, max = Integer.MAX_VALUE)
	@ConfigProperty(category = "world", comment = "How rare the mana pool is in terms of 1 in X. Set to 0 to disable generation")
	public static int manaPoolRarity = 25;

	@ConfigProperty(category = "world", comment = "If you have a dimension ID conflict with this mod and something else, change this number")
	public static int underworldID = 42;

	@ConfigProperty(category = "world", comment = "The maximum possible distance required for 2 mana interacting blocks to link to each other")
	public static int networkLinkDistance = 32;

	@ConfigProperty(category = "items", comment = "The maximum limit a cape can give a player in terms of mana/burnout buffers")
	public static int maxCapeCap = 5000;

	@ConfigDoubleRange(min = 1, max = 2)
	@ConfigProperty(category = "spells", comment = "The multiplier a spell gets for a perfect or ancient quality pearl. [1,2]\n" +
			"This will be multiplied by the quality value of the pearl, which is 1.0 for perfect pearls and greater for ancient pearls.")
	public static double perfectPearlMultiplier = 1.2;

	@ConfigDoubleRange(min = 0.001, max = 0.1)
	@ConfigProperty(category = "spells", comment = "The multiplier a spell gets, as a flat rate, for a depleted quality pearl. [0.001,0.1]")
	public static double damagedPearlMultiplier = 0.05;
}
