package com.aionemu.gameserver.services.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HubConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.World;

public final class HubLoginRoutingService {

	private static final Logger log = LoggerFactory.getLogger(HubLoginRoutingService.class);

	private HubLoginRoutingService() {
	}

	/**
	 * Applies the configured login routing mode. Returns true when the player was moved to the configured hub destination.
	 */
	public static boolean routePlayerOnLogin(Player player) {
		if (!HubConfig.HUB_LOGIN_ROUTING_ENABLED) {
			log.info("Using classic login flow for {} (hub login routing disabled).", player);
			return false;
		}

		if (HubConfig.HUB_LOGIN_INSTANCE_ENABLED)
			return HubInstanceRoutingService.routePlayerOnLogin(player);

		try {
			World.getInstance().setPosition(player, HubConfig.HUB_LOGIN_WORLD_ID, HubConfig.HUB_LOGIN_X, HubConfig.HUB_LOGIN_Y, HubConfig.HUB_LOGIN_Z,
				HubConfig.HUB_LOGIN_HEADING);
			log.info("Using legacy hub world login routing for {} -> mapId={}, x={}, y={}, z={}, heading={}.", player, HubConfig.HUB_LOGIN_WORLD_ID,
				HubConfig.HUB_LOGIN_X, HubConfig.HUB_LOGIN_Y, HubConfig.HUB_LOGIN_Z, HubConfig.HUB_LOGIN_HEADING);
			return true;
		} catch (RuntimeException ex) {
			log.error("Legacy hub world login routing failed for {}. Falling back to classic login flow.", player, ex);
			return false;
		}
	}
}
