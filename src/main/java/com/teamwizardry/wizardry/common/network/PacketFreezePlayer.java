package com.teamwizardry.wizardry.common.network;

import com.teamwizardry.librarianlib.core.LibrarianLib;
import com.teamwizardry.librarianlib.features.network.PacketBase;
import com.teamwizardry.librarianlib.features.saving.Save;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque.
 */
public class PacketFreezePlayer extends PacketBase {

	@Save
	public int countdown;
	@Save
	public int interval;

	public PacketFreezePlayer() {

	}

	public PacketFreezePlayer(int countdown, int interval) {
		this.countdown = countdown;
		this.interval = interval;
	}


	@Override
	public void handle(@Nonnull MessageContext messageContext) {
		EntityPlayer player = LibrarianLib.PROXY.getClientPlayer();
		player.getEntityData().setInteger("strength", countdown);
		player.getEntityData().setInteger("skip_tick", countdown);
		player.getEntityData().setInteger("skip_tick_interval", interval);
		player.getEntityData().setInteger("skip_tick_interval_save", interval);
		player.getEntityData().setDouble("origin_motion_x", player.motionX);
		player.getEntityData().setDouble("origin_motion_y", player.motionY);
		player.getEntityData().setDouble("origin_motion_z", player.motionZ);
	}
}
