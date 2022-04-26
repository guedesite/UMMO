package fr.guedesite.ummo.server;

import java.util.concurrent.Callable;

public class PlayerDataFactory implements Callable<PlayerData>{
	@Override
	public PlayerData call() throws Exception {
		System.out.println("generate player data");
		return new PlayerData();
	}

}
