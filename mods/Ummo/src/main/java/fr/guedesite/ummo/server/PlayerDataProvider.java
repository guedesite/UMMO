package fr.guedesite.ummo.server;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerDataProvider implements ICapabilitySerializable<CompoundNBT>{

	@CapabilityInject(PlayerData.class)
	public static final Capability<PlayerData> CAP = null;

	private LazyOptional<PlayerData> instance = LazyOptional.of(CAP::getDefaultInstance);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return CAP.orEmpty(cap, instance);
	}

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) CAP.getStorage().writeNBT(CAP, instance.orElseThrow(() -> new IllegalArgumentException("at serialize")), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		CAP.getStorage().readNBT(CAP, instance.orElseThrow(() -> new IllegalArgumentException("at deserialize")), null, nbt);
	}

}
