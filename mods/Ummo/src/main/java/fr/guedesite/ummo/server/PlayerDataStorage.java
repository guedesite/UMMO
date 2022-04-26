package fr.guedesite.ummo.server;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PlayerDataStorage implements Capability.IStorage<PlayerData> {

	@Override
	public INBT writeNBT(Capability<PlayerData> capability, PlayerData data, Direction side) {
		CompoundNBT nbt = new CompoundNBT();
		data.writeToNbt(nbt);
		return nbt;
	}

	@Override
	public void readNBT(Capability<PlayerData> capability, PlayerData data, Direction side, INBT nbti) {
		CompoundNBT nbt = (CompoundNBT)nbti;
		data.readFromNbt(nbt);
		
	}



}
