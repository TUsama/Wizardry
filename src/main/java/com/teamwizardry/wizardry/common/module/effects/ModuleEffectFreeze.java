package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.particle.functions.InterpFadeInOut;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.spell.*;
import com.teamwizardry.wizardry.api.util.BlockUtils;
import com.teamwizardry.wizardry.api.util.InterpScale;
import com.teamwizardry.wizardry.api.util.RandUtil;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.*;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleEffectFreeze extends Module implements ITaxing {

	@Nonnull
	@Override
	public ModuleType getModuleType() {
		return ModuleType.EFFECT;
	}

	@Nonnull
	@Override
	public String getID() {
		return "effect_freeze";
	}

	@Nonnull
	@Override
	public String getReadableName() {
		return "Freeze";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Will slow down opponents, extinguish fires, turn water to ice, lava to stone, etc...";
	}

	@Override
	public boolean run(@Nonnull SpellData spell) {
		World world = spell.world;
		Entity targetEntity = spell.getData(ENTITY_HIT);
		BlockPos targetPos = spell.getData(BLOCK_HIT);
		Entity caster = spell.getData(CASTER);

		double range = getModifierPower(spell, Attributes.INCREASE_AOE, 1, 16, true, true) / 2.0;

		if (!tax(this, spell)) return false;

		if (targetEntity != null) {
			targetEntity.setFire(0);
			// TODO: slippery
		}

		if (targetPos != null) {
			for (int x = (int) range; x >= -range; x--)
				for (int y = (int) range; y >= -range; y--)
					for (int z = (int) range; z >= -range; z--) {
						BlockPos pos = targetPos.add(x, y, z);
						double dist = pos.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ());
						if (dist > range) continue;

						for (EnumFacing facing : EnumFacing.VALUES) {
							IBlockState state = world.getBlockState(pos.offset(facing));
							if (state.getBlock() == Blocks.FIRE) {
								BlockUtils.breakBlock(world, pos.offset(facing), state, caster instanceof EntityPlayer ? (EntityPlayerMP) caster : null, false);
							}
						}

						if (world.getBlockState(pos).isTopSolid() && world.isAirBlock(pos.offset(EnumFacing.UP))) {
							int layerSize = (int) (Math.max(1, Math.min(8, Math.max(1, (dist / range) * 6.0))));
							layerSize = Math.max(1, Math.min(layerSize + RandUtil.nextInt(-1, 1), 8));
							BlockUtils.placeBlock(world, pos.offset(EnumFacing.UP), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, layerSize), caster instanceof EntityPlayer ? (EntityPlayerMP) caster : null);
						}

						if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
							BlockUtils.placeBlock(world, pos, Blocks.ICE.getDefaultState(), caster instanceof EntityPlayer ? (EntityPlayerMP) caster : null);
						}
					}
		}
		return true;
	}

	@Override
	public void runClient(@Nonnull SpellData spell) {
		World world = spell.world;
		Vec3d position = spell.getData(TARGET_HIT);

		if (position == null) return;

		ParticleBuilder glitter = new ParticleBuilder(1);
		glitter.setAlphaFunction(new InterpFadeInOut(0.0f, 0.1f));
		glitter.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
		glitter.enableMotionCalculation();
		glitter.setScaleFunction(new InterpScale(1, 0));
		ParticleSpawner.spawn(glitter, world, new StaticInterp<>(position), RandUtil.nextInt(5, 15), 0, (aFloat, particleBuilder) -> {
			double radius = 2;
			double theta = 2.0f * (float) Math.PI * RandUtil.nextFloat();
			double r = radius * RandUtil.nextFloat();
			double x = r * MathHelper.cos((float) theta);
			double z = r * MathHelper.sin((float) theta);
			glitter.setScale(RandUtil.nextFloat());
			glitter.setPositionOffset(new Vec3d(x, RandUtil.nextDouble(-2, 2), z));
			glitter.setLifetime(RandUtil.nextInt(30, 40));
			Vec3d direction = position.add(glitter.getPositionOffset()).subtract(position).normalize();
			glitter.setMotion(direction.scale(RandUtil.nextDouble(0.5, 1.3)));
		});

	}

	@Nonnull
	@Override
	public Module copy() {
		return cloneModule(new ModuleEffectFreeze());
	}
}