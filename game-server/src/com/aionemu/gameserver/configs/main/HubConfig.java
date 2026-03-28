package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class HubConfig {

	@Property(key = "gameserver.hub.login.routing.enabled", defaultValue = "false")
	public static boolean HUB_LOGIN_ROUTING_ENABLED;

	@Property(key = "gameserver.hub.login.destination.world_id", defaultValue = "110010000")
	public static int HUB_LOGIN_WORLD_ID;

	@Property(key = "gameserver.hub.login.destination.x", defaultValue = "1462.5")
	public static float HUB_LOGIN_X;

	@Property(key = "gameserver.hub.login.destination.y", defaultValue = "1326.1")
	public static float HUB_LOGIN_Y;

	@Property(key = "gameserver.hub.login.destination.z", defaultValue = "564.1")
	public static float HUB_LOGIN_Z;

	@Property(key = "gameserver.hub.login.destination.heading", defaultValue = "0")
	public static byte HUB_LOGIN_HEADING;
}
