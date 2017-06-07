package com.teamwizardry.wizardry.common.module.modifiers;

import com.teamwizardry.wizardry.api.spell.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleModifierException extends Module implements IModifier {

	@Nonnull
	@Override
	public ModuleType getModuleType() {
		return ModuleType.MODIFIER;
	}

	@Nonnull
	@Override
	public String getID() {
		return "modifier_exception";
	}

	@Nonnull
	@Override
	public String getReadableName() {
		return "Exception";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Will ignore the entity chosen";
	}

	@Override
	public boolean run(@Nonnull SpellData spell) {
		return true;
	}

	@Override
	public void runClient(@Nonnull SpellData spell) {

	}

	@Override
	public void apply(@NotNull Module module) {
		// TODO
	}

	@Override
	public double costMultiplier() {
		return 1.2;
	}

	@Nonnull
	@Override
	public Module copy() {
		return cloneModule(new ModuleModifierException());
	}
}