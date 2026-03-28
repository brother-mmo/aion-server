package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class HubConfig {

	@Property(key = "gameserver.hub.login.routing.enabled", defaultValue = "false")
	public static boolean HUB_LOGIN_ROUTING_ENABLED;

	@Property(key = "gameserver.hub.login.instance.enabled", defaultValue = "true")
	public static boolean HUB_LOGIN_INSTANCE_ENABLED;

	@Property(key = "gameserver.hub.login.instance.world_id", defaultValue = "310080000")
	public static int HUB_LOGIN_INSTANCE_WORLD_ID;

	@Property(key = "gameserver.hub.login.instance.x", defaultValue = "232.05")
	public static float HUB_LOGIN_INSTANCE_X;

	@Property(key = "gameserver.hub.login.instance.y", defaultValue = "237.106")
	public static float HUB_LOGIN_INSTANCE_Y;

	@Property(key = "gameserver.hub.login.instance.z", defaultValue = "158.917")
	public static float HUB_LOGIN_INSTANCE_Z;

	@Property(key = "gameserver.hub.login.instance.heading", defaultValue = "1")
	public static byte HUB_LOGIN_INSTANCE_HEADING;

	@Property(key = "gameserver.hub.login.instance.max_players", defaultValue = "100")
	public static int HUB_LOGIN_INSTANCE_MAX_PLAYERS;

	@Property(key = "gameserver.hub.exit.open_world.world_id", defaultValue = "110010000")
	public static int HUB_EXIT_WORLD_ID;

	@Property(key = "gameserver.hub.exit.open_world.x", defaultValue = "1460.36")
	public static float HUB_EXIT_X;

	@Property(key = "gameserver.hub.exit.open_world.y", defaultValue = "1448.84")
	public static float HUB_EXIT_Y;

	@Property(key = "gameserver.hub.exit.open_world.z", defaultValue = "572.87524")
	public static float HUB_EXIT_Z;

	@Property(key = "gameserver.hub.exit.open_world.heading", defaultValue = "7")
	public static byte HUB_EXIT_HEADING;

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
