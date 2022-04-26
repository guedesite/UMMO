package fr.guedesite.ummo.proxy.common;

import fr.guedesite.ummo.Reference;
import fr.guedesite.ummo.server.PlayerData;
import fr.guedesite.ummo.server.PlayerDataProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEventHandler {

	@SubscribeEvent
	public static void attachCapa(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof PlayerEntity && !event.getObject().level.isClientSide) {
			event.addCapability(new ResourceLocation(Reference.MOD_ID, "playerdata"), new PlayerDataProvider());
		}
	}
	
	@SubscribeEvent
	public static void PlayerLoggedIn(PlayerLoggedInEvent event) {
		if(!event.getPlayer().level.isClientSide) {
			System.out.println("loggin");
			PlayerData capa = event.getPlayer().getCapability(PlayerDataProvider.CAP).orElseThrow(() -> new IllegalArgumentException("at login"));
			System.out.println(capa.getCapa_building_level());
		}
	}
	
	@SubscribeEvent
    public static void LivingDamageEvent(LivingDamageEvent event) {
		if(event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			float amount = player.getHealth() - event.getAmount();
			if(amount <=0) {
				return;
			}
			if(amount <= 7) {
				player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 15, 1, true, false, false, null));
				player.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 30, 1, true, false, false, null));
				if(amount <=3) {
					player.addEffect(new EffectInstance(Effects.WEAKNESS, 30, 1, true, false, false, null));
					if(amount <= 1.1F) {
						player.addEffect(new EffectInstance(Effects.BLINDNESS, 30, 1, true, false, false, null));
					}
				}
			}
		}
		
    }
	
	@SubscribeEvent
    public static void LivingHealEvent(net.minecraftforge.event.entity.living.LivingHealEvent event) {
		if(event.getEntityLiving() instanceof PlayerEntity) {
			if(event.getAmount() != 1) {
				return;
			}
			
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			if(player.getHealth() < 8) {
				event.setCanceled(player.level.random.nextInt((int)((8 - player.getHealth()) * 2) +1 ) != 0);
			}
		}
    }
	
}
