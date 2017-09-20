package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.module.Module;
import com.teamwizardry.wizardry.api.spell.module.ModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.RegisterModule;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.fx.LibParticles;
import com.teamwizardry.wizardry.init.ModSounds;
import kotlin.jvm.functions.Function1;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.ENTITY_HIT;
import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.TARGET_HIT;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleEffectDisarm extends ModuleEffect {

	@Nonnull
	@Override
	public String getID() {
		return "effect_disarm";
	}

	@Nonnull
	@Override
	public String getReadableName() {
		return "Disarm";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Will drop the target's held item on the ground";
	}

	@Override
	public boolean run(@Nonnull SpellData spell) {
		Entity targetEntity = spell.getData(ENTITY_HIT);

		if (targetEntity instanceof EntityLivingBase) {
			if (!spell.world.isRemote) {
				if (!tax(this, spell)) return false;

				ItemStack held = ((EntityLivingBase) targetEntity).getHeldItemMainhand();

				if (targetEntity instanceof EntityPlayer) {
					for (int i = 9; i < ((EntityPlayer) targetEntity).inventory.getSizeInventory(); i++) {
						if (((EntityPlayer) targetEntity).inventory.getStackInSlot(i) == ItemStack.EMPTY) {
							((EntityPlayer) targetEntity).inventory.setInventorySlotContents(i, held.copy());
							held.setCount(0);
							return true;
						}
					}

					ItemStack copy = held.copy();
					held.setCount(0);
					EntityItem item = new EntityItem(spell.world, targetEntity.posX, targetEntity.posY + 1, targetEntity.posZ, copy);
					item.setPickupDelay(5);
					spell.world.playSound(null, targetEntity.getPosition(), ModSounds.ELECTRIC_BLAST, SoundCategory.NEUTRAL, 1, 1);
					return spell.world.spawnEntity(item);
				} else {
					ItemStack stack = held.copy();
					held.setCount(0);

					float dropChance = 0;
					
					if (targetEntity instanceof EntityLiving)
					{
						EntityLiving entity = (EntityLiving) targetEntity;
						
						Object o = inventoryHandsDropChances.invoke(entity);
						float[] dropChances;
						if (o instanceof float[])
						{
							dropChances = (float[]) o;
							dropChance = dropChances[EntityEquipmentSlot.MAINHAND.getIndex()];
						}
					}
					
					boolean flag = dropChance > 1.0;

					if (!held.isEmpty() && flag && RandUtil.nextDouble() < dropChance)
					{
						EntityItem item = new EntityItem(spell.world, targetEntity.posX, targetEntity.posY + 1, targetEntity.posZ, stack);
						item.setPickupDelay(5);
						spell.world.playSound(null, targetEntity.getPosition(), ModSounds.ELECTRIC_BLAST, SoundCategory.NEUTRAL, 1, 1);
						return spell.world.spawnEntity(item);
					}
				}
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void runClient(@Nonnull SpellData spell) {
		World world = spell.world;
		Vec3d position = spell.getData(TARGET_HIT);

		if (position == null) return;

		LibParticles.EFFECT_REGENERATE(world, position, getPrimaryColor());
	}

	@Nonnull
	@Override
	public Module copy() {
		return cloneModule(new ModuleEffectDisarm());
	}
	
	private Function1<EntityLiving, Object> inventoryHandsDropChances = MethodHandleHelper.wrapperForGetter(EntityLiving.class, "inventoryHandsDropChances", "field_184655_bs", "bs");
}
