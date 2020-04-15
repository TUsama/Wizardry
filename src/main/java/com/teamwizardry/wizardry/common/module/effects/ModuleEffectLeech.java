
package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.math.interpolate.numeric.InterpFloatInOut;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.NBTConstants;
import com.teamwizardry.wizardry.api.capability.player.mana.ManaManager;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterModule;
import com.teamwizardry.wizardry.api.spell.attribute.AttributeRegistry;
import com.teamwizardry.wizardry.api.spell.module.IModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceEffect;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque.
 */

@RegisterModule(ID="effect_leech")
public class ModuleEffectLeech implements IModuleEffect {

	@Override
	public String[] compatibleModifiers() {
		return new String[]{"modifier_increase_potency"};
	}

	@Override
	public boolean run(@NotNull World world, ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Entity targetEntity = spell.getVictim(world);
		Entity caster = spell.getCaster(world);

		double potency = spellRing.getAttributeValue(world, AttributeRegistry.POTENCY, spell);

		if (!spellRing.taxCaster(world, spell, true)) return false;

		if (targetEntity instanceof EntityLivingBase) {
			int invTime = targetEntity.hurtResistantTime;
			targetEntity.hurtResistantTime = 0;
			if (targetEntity instanceof EntityPlayer) {

				double targetMana = ManaManager.getMana(targetEntity);

				targetEntity.attackEntityFrom(DamageSource.MAGIC, (float) potency);
				if (targetEntity.isDead) {
					targetMana /= 2;
					targetMana = MathHelper.clamp(targetMana, targetMana, spellRing.getManaDrain(spell) * 2);
					ManaManager.forObject(caster).addMana(targetMana).close();
				}

			} else if (targetEntity instanceof EntityWitch) {

				double targetMana = spellRing.getManaDrain(spell) * 2;

				targetEntity.attackEntityFrom(DamageSource.MAGIC, (float) potency);
				if (targetEntity.isDead) {
					ManaManager.forObject(caster).addMana(targetMana).close();
				}

			} else {
				if (caster instanceof EntityLivingBase) ((EntityLivingBase) caster).setLastAttackedEntity(targetEntity);

				if (caster != null)
					targetEntity.attackEntityFrom(new EntityDamageSource("magic", caster).setDamageBypassesArmor().setMagicDamage(), (float) potency);
				else
					targetEntity.attackEntityFrom(DamageSource.MAGIC, (float) potency);

			}
			targetEntity.hurtResistantTime = invTime;
		}

		Vec3d target = spell.getTargetWithFallback(world);
		if (target != null)
			world.playSound(null, new BlockPos(target), ModSounds.CHAINY_ZAP, SoundCategory.NEUTRAL, 0.5f, 1f);
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderSpell(World world, ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Vec3d position = spell.getTarget(world);

		if (position == null) return;

		ParticleBuilder glitter = new ParticleBuilder(10);
		glitter.setRender(new ResourceLocation(Wizardry.MODID, NBTConstants.MISC.SPARKLE_BLURRED));
		glitter.setCollision(true);
		glitter.setCanBounce(true);
		glitter.enableMotionCalculation();
		glitter.setAcceleration(new Vec3d(0, RandUtil.nextDouble(-0.05, -0.035), 0));

		ParticleSpawner.spawn(glitter, world, new StaticInterp<>(position), 80, 0, (i, builder) -> {
			builder.setLifetime(RandUtil.nextInt(30, 60));
			builder.addMotion(new Vec3d(RandUtil.nextDouble(-0.05, 0.05), RandUtil.nextDouble(0.01, 0.02), RandUtil.nextDouble(-0.05, 0.05)));
			builder.setScale((float) RandUtil.nextDouble(0.3, 0.5));
			builder.setAlphaFunction(new InterpFloatInOut(0.0f, 0.3f));
			builder.setColor(RandUtil.nextBoolean() ? spellRing.getPrimaryColor() : spellRing.getSecondaryColor());
		});
	}
}
