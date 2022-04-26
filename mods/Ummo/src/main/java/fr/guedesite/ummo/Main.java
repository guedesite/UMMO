package fr.guedesite.ummo;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Timer;
import java.util.TimerTask;

import fr.guedesite.ummo.server.PlayerData;
import fr.guedesite.ummo.server.PlayerDataStorage;
import fr.guedesite.utils.*;

@Mod( Reference.MOD_ID )
public class Main {
	
	public static final bdd bdd = new bdd();
	
	public static Main Instance;
	
	public Main() {
		Instance = this;
        MinecraftForge.EVENT_BUS.addListener( this::registerCommands );
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> bus.addListener(fr.guedesite.ummo.proxy.ClientHandler::setupCommon));
		DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> bus.addListener(fr.guedesite.ummo.proxy.ServerHandler::setupCommon));
	}
	


	public void registerCommands( RegisterCommandsEvent event )
    {
       
    }


}
