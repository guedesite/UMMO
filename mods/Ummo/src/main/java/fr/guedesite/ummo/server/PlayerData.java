package fr.guedesite.ummo.server;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class PlayerData{

	private int capa_combat_xp,
				capa_combat_level,
				capa_mining_xp,
				capa_mining_level,
				capa_woodcuting_xp,
				capa_woodcuting_level,
				capa_digging_xp,
				capa_digging_level,
				capa_harvesting_xp,
				capa_harvesting_level,
				capa_building_xp,
				capa_building_level,
				capa_general_xp,
				capa_general_level;
	
	public PlayerEntity player;
	
	public PlayerData() {
		this.capa_building_level = new Random().nextInt(1000);
		System.out.println("build capa "+this.capa_building_level);
	}
	
	public void update() {
		player.serializeNBT();
	}
	
	public void readFromNbt(CompoundNBT nbt) {
		Class<? extends PlayerData> cl = this.getClass();
		for(Field f :cl.getDeclaredFields()) {
			if(f.getName().startsWith("capa_")) {
				f.setAccessible(true);
				try {
					f.setInt(this, nbt.getInt(f.getName()));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public CompoundNBT writeToNbt(CompoundNBT nbt) {
		Class<? extends PlayerData> cl = this.getClass();
		for(Field f :cl.getDeclaredFields()) {
			if(f.getName().startsWith("capa_")) {
				f.setAccessible(true);
				try {
					nbt.putInt(f.getName(), f.getInt(this));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return nbt;
	}
	
	
	public int getCapa_combat_xp() {
		return capa_combat_xp;
	}

	public void setCapa_combat_xp(int capa_combat_xp) {
		this.capa_combat_xp = capa_combat_xp;
	}

	public int getCapa_combat_level() {
		return capa_combat_level;
	}

	public void setCapa_combat_level(int capa_combat_level) {
		this.capa_combat_level = capa_combat_level;
	}

	public int getCapa_mining_xp() {
		return capa_mining_xp;
	}

	public void setCapa_mining_xp(int capa_mining_xp) {
		this.capa_mining_xp = capa_mining_xp;
	}

	public int getCapa_mining_level() {
		return capa_mining_level;
	}

	public void setCapa_mining_level(int capa_mining_level) {
		this.capa_mining_level = capa_mining_level;
	}

	public int getCapa_woodcuting_xp() {
		return capa_woodcuting_xp;
	}

	public void setCapa_woodcuting_xp(int capa_woodcuting_xp) {
		this.capa_woodcuting_xp = capa_woodcuting_xp;
	}

	public int getCapa_woodcuting_level() {
		return capa_woodcuting_level;
	}

	public void setCapa_woodcuting_level(int capa_woodcuting_level) {
		this.capa_woodcuting_level = capa_woodcuting_level;
	}

	public int getCapa_digging_xp() {
		return capa_digging_xp;
	}

	public void setCapa_digging_xp(int capa_digging_xp) {
		this.capa_digging_xp = capa_digging_xp;
	}

	public int getCapa_digging_level() {
		return capa_digging_level;
	}

	public void setCapa_digging_level(int capa_digging_level) {
		this.capa_digging_level = capa_digging_level;
	}

	public int getCapa_harvesting_xp() {
		return capa_harvesting_xp;
	}

	public void setCapa_harvesting_xp(int capa_harvesting_xp) {
		this.capa_harvesting_xp = capa_harvesting_xp;
	}

	public int getCapa_harvesting_level() {
		return capa_harvesting_level;
	}

	public void setCapa_harvesting_level(int capa_harvesting_level) {
		this.capa_harvesting_level = capa_harvesting_level;
	}

	public int getCapa_building_xp() {
		return capa_building_xp;
	}

	public void setCapa_building_xp(int capa_building_xp) {
		this.capa_building_xp = capa_building_xp;
	}

	public int getCapa_building_level() {
		return capa_building_level;
	}

	public void setCapa_building_level(int capa_building_level) {
		this.capa_building_level = capa_building_level;
	}

	public int getCapa_general_xp() {
		return capa_general_xp;
	}

	public void setCapa_general_xp(int capa_general_xp) {
		this.capa_general_xp = capa_general_xp;
	}

	public int getCapa_general_level() {
		return capa_general_level;
	}

	public void setCapa_general_level(int capa_general_level) {
		this.capa_general_level = capa_general_level;
	}

}
