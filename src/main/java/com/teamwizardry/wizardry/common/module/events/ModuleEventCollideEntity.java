package com.teamwizardry.wizardry.common.module.events;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.module.Module;
import com.teamwizardry.wizardry.api.spell.module.ModuleEvent;
import com.teamwizardry.wizardry.api.spell.module.RegisterModule;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.BLOCK_HIT;
import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.ENTITY_HIT;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleEventCollideEntity extends ModuleEvent {

	@Nonnull
	@Override
	public String getID() {
		return "event_collide_entity";
	}

	@Override
	public boolean run(@Nonnull SpellData spell) {
		Entity entity = spell.getData(ENTITY_HIT);
		spell.removeData(BLOCK_HIT);
		return entity != null && nextModule != null && nextModule.run(spell);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void runClient(@Nonnull SpellData spell) {

	}

	@Nonnull
	@Override
	public Module copy() {
		return cloneModule(new ModuleEventCollideEntity());
	}
}
