package com.aionemu.gameserver.services.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.HubConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

public final class HubInstanceRoutingService {

	private static final Logger log = LoggerFactory.getLogger(HubInstanceRoutingService.class);
	private static final Object HUB_INSTANCE_MONITOR = new Object();
	private static volatile int activeHubInstanceId;

	private HubInstanceRoutingService() {
	}

	/**
	 * Routes the player into an isolated hub instance without using shared open-world Sanctum state.
	 */
	public static boolean routePlayerOnLogin(Player player) {
		try {
			WorldMapInstance hubInstance = resolveOrCreateHubInstance();
			hubInstance.register(player.getObjectId());
			hubInstance.setStartPos(createHubStartPosition());
			World.getInstance().setPosition(player, hubInstance.getMapId(), hubInstance.getInstanceId(), HubConfig.HUB_LOGIN_INSTANCE_X,
				HubConfig.HUB_LOGIN_INSTANCE_Y, HubConfig.HUB_LOGIN_INSTANCE_Z, HubConfig.HUB_LOGIN_INSTANCE_HEADING);
			log.info("Using hub instance login routing for {} -> mapId={}, instanceId={}, x={}, y={}, z={}, heading={}.", player, hubInstance.getMapId(),
				hubInstance.getInstanceId(), HubConfig.HUB_LOGIN_INSTANCE_X, HubConfig.HUB_LOGIN_INSTANCE_Y, HubConfig.HUB_LOGIN_INSTANCE_Z,
				HubConfig.HUB_LOGIN_INSTANCE_HEADING);
			return true;
		} catch (RuntimeException ex) {
			log.error("Hub instance login routing failed for {}. Falling back to classic login flow.", player, ex);
			return false;
		}
	}

	private static WorldMapInstance resolveOrCreateHubInstance() {
		WorldMap map = World.getInstance().getWorldMap(HubConfig.HUB_LOGIN_INSTANCE_WORLD_ID);
		if (map == null)
			throw new IllegalStateException("Unknown hub instance world: " + HubConfig.HUB_LOGIN_INSTANCE_WORLD_ID);
		if (!map.isInstanceType())
			throw new IllegalStateException("Hub instance world must be an instance map: " + HubConfig.HUB_LOGIN_INSTANCE_WORLD_ID);

		synchronized (HUB_INSTANCE_MONITOR) {
			WorldMapInstance activeInstance = getActiveHubInstance(map);
			if (activeInstance != null)
				return activeInstance;

			for (WorldMapInstance existingInstance : map) {
				if (existingInstance.getInstanceHandler() instanceof HubInstanceHandler && !existingInstance.isFull()) {
					activeHubInstanceId = existingInstance.getInstanceId();
					return existingInstance;
				}
			}

			WorldMapInstance createdInstance = InstanceService.getNextAvailableInstance(HubConfig.HUB_LOGIN_INSTANCE_WORLD_ID, 0, (byte) 0,
				HubInstanceHandler::new, HubConfig.HUB_LOGIN_INSTANCE_MAX_PLAYERS, true);
			createdInstance.setStartPos(createHubStartPosition());
			activeHubInstanceId = createdInstance.getInstanceId();
			return createdInstance;
		}
	}

	private static WorldMapInstance getActiveHubInstance(WorldMap map) {
		if (activeHubInstanceId == 0)
			return null;

		WorldMapInstance instance = map.getWorldMapInstance(activeHubInstanceId);
		if (instance == null || !(instance.getInstanceHandler() instanceof HubInstanceHandler) || instance.isFull()) {
			activeHubInstanceId = 0;
			return null;
		}
		return instance;
	}

	private static WorldPosition createHubStartPosition() {
		return new WorldPosition(HubConfig.HUB_LOGIN_INSTANCE_WORLD_ID, HubConfig.HUB_LOGIN_INSTANCE_X, HubConfig.HUB_LOGIN_INSTANCE_Y,
			HubConfig.HUB_LOGIN_INSTANCE_Z, HubConfig.HUB_LOGIN_INSTANCE_HEADING);
	}
}
