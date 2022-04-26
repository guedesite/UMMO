package fr.guedesite.ummo.proxy;

import fr.guedesite.ummo.proxy.client.ClientEventHandler;
import fr.guedesite.ummo.proxy.client.gui.UGuiInGame;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientHandler extends ServerHandler{
	
	 public static void setup(FMLClientSetupEvent event) {
		 System.out.println("ac 1");
	 }
	
	 public static void setupCommon(FMLCommonSetupEvent event) {
		 System.out.println("ac 2");
		 ServerHandler.setupCommon(event);
		 MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
		 Minecraft.getInstance().gui = new UGuiInGame(Minecraft.getInstance());
	  }
}
