package com.aionemu.gameserver.services.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HubConfig;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;

public final class HubExitService {

	private static final Logger log = LoggerFactory.getLogger(HubExitService.class);

	private HubExitService() {
	}

	/**
	 * Sends a player from the isolated hub instance into the configured open-world destination.
	 */
	public static boolean exitToConfiguredOpenWorld(Player player) {
		if (!isInsideHubInstance(player)) {
			log.warn("Rejected explicit hub exit for {} because the player is not inside a hub instance.", player);
			return false;
		}

		try {
			TeleportService.teleportTo(player, HubConfig.HUB_EXIT_WORLD_ID, HubConfig.HUB_EXIT_X, HubConfig.HUB_EXIT_Y, HubConfig.HUB_EXIT_Z,
				HubConfig.HUB_EXIT_HEADING, TeleportAnimation.FADE_OUT_BEAM);
			log.info("Using explicit hub exit for {} -> mapId={}, x={}, y={}, z={}, heading={}.", player, HubConfig.HUB_EXIT_WORLD_ID,
				HubConfig.HUB_EXIT_X, HubConfig.HUB_EXIT_Y, HubConfig.HUB_EXIT_Z, HubConfig.HUB_EXIT_HEADING);
			return true;
		} catch (RuntimeException ex) {
			log.error("Explicit hub exit failed for {}.", player, ex);
			return false;
		}
	}

	public static boolean isInsideHubInstance(Player player) {
		return player != null
			&& player.getPosition() != null
			&& player.getPosition().getMapRegion() != null
			&& player.getPosition().isInstanceMap()
			&& player.getPosition().getWorldMapInstance().getInstanceHandler() instanceof HubInstanceHandler;
	}
}
