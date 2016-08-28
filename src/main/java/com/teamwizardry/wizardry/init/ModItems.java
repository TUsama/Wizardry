package com.teamwizardry.wizardry.init;

import com.teamwizardry.wizardry.common.item.*;
import com.teamwizardry.wizardry.common.item.pearl.ItemGlassPearl;
import com.teamwizardry.wizardry.common.item.pearl.ItemManaPearl;
import com.teamwizardry.wizardry.common.item.pearl.ItemNacrePearl;
import com.teamwizardry.wizardry.common.item.pearl.ItemQuartzPearl;
import com.teamwizardry.wizardry.common.item.staff.ItemGoldStaff;
import com.teamwizardry.wizardry.common.item.staff.ItemWoodStaff;

/**
 * Created by Saad on 4/9/2016.
 */
public class ModItems {

	public static ItemQuartzPearl PEARL_QUARTZ;
	public static ItemGlassPearl PEARL_GLASS;
	public static ItemManaPearl PEARL_MANA;
	public static ItemNacrePearl PEARL_NACRE;

	public static ItemWoodStaff STAFF_WOOD;
	public static ItemGoldStaff STAFF_GOLD;

	public static ItemRing RING;
	public static ItemBook BOOK;
	public static ItemDevilDust DEVIL_DUST;
	public static ItemManaCake MANA_CAKE;

	public static ItemFairyWings FAIRY_WINGS;
	public static ItemFairyDust FAIRY_DUST;
	public static ItemFairyImbuedApple FAIRY_IMBUED_APPLE;

	public static ItemJar JAR;
	public static ItemUnicornHorn UNICORN_HORN;

	public static ItemDebugger DEBUG;

	public static void init() {
		PEARL_QUARTZ = new ItemQuartzPearl();
		PEARL_GLASS = new ItemGlassPearl();
		PEARL_MANA = new ItemManaPearl();
		PEARL_NACRE = new ItemNacrePearl();

		RING = new ItemRing();
		BOOK = new ItemBook();
		DEBUG = new ItemDebugger();
		DEVIL_DUST = new ItemDevilDust();
		MANA_CAKE = new ItemManaCake();

		STAFF_GOLD = new ItemGoldStaff();
		STAFF_WOOD = new ItemWoodStaff();

		FAIRY_WINGS = new ItemFairyWings();
		FAIRY_DUST = new ItemFairyDust();
		FAIRY_IMBUED_APPLE = new ItemFairyImbuedApple();

		JAR = new ItemJar();
		UNICORN_HORN = new ItemUnicornHorn();
	}
}
