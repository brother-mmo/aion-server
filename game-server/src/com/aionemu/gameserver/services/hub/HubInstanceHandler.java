package com.aionemu.gameserver.services.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldMapInstance;

public class HubInstanceHandler extends GeneralInstanceHandler {

	private static final Logger log = LoggerFactory.getLogger(HubInstanceHandler.class);

	public HubInstanceHandler(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		log.info("Created isolated hub instance {} [{}].", instance.getMapId(), instance.getInstanceId());
	}

	@Override
	public void onPlayerLogin(Player player) {
		log.info("Player {} entered isolated hub instance {} [{}].", player, instance.getMapId(), instance.getInstanceId());
	}

	@Override
	public void leaveInstance(Player player) {
		if (!HubExitService.exitToConfiguredOpenWorld(player))
			log.warn("Explicit hub exit was not completed for {}.", player);
	}
}
