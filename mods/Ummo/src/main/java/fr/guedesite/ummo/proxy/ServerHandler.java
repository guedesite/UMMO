package fr.guedesite.ummo.proxy;

import fr.guedesite.ummo.Main;
import fr.guedesite.ummo.proxy.common.ServerEventHandler;
import fr.guedesite.ummo.server.PlayerData;
import fr.guedesite.ummo.server.PlayerDataFactory;
import fr.guedesite.ummo.server.PlayerDataStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerHandler {

	 public static void setup(FMLClientSetupEvent event) {
		 System.out.println("ab 1");
	 }
	
	 public static void setupCommon(FMLCommonSetupEvent event) {
		 System.out.println("ab 2");
		 MinecraftForge.EVENT_BUS.addListener(Main.Instance::registerCommands );
		 MinecraftForge.EVENT_BUS.register(ServerEventHandler.class);
		 CapabilityManager.INSTANCE.register(PlayerData.class, new PlayerDataStorage(), new PlayerDataFactory());
	 }
	 
	 public static void registerCommands(RegisterCommandsEvent event) {
		 
	 }
	
}
