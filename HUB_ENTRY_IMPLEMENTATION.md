# Hub Entry Implementation

## Purpose

Add the first real MVP hub feature: a configurable, additive hub-first login routing hook that can be enabled or disabled without breaking classic open-world behavior.

This step intentionally does not implement:

- instance-based hub routing
- hub NPC logic
- matchmaking or session orchestration
- login-server changes
- generic spawn or teleport rewrites

## Touched Files

- `game-server/src/com/aionemu/gameserver/configs/main/HubConfig.java`
- `game-server/src/com/aionemu/gameserver/configs/Config.java`
- `game-server/src/com/aionemu/gameserver/services/hub/HubLoginRoutingService.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/config/main/hub.properties`
- `HUB_ENTRY_IMPLEMENTATION.md`

## Hook Point Used

Hook used:

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- inside `enterWorld(AionConnection client, Player player)`
- before `World.getInstance().storeObject(player)`
- before the classic fortress/vortex login relocation checks
- before `InstanceService.onPlayerLogin(player)` later in the login flow

This was chosen because it is the least invasive place that still allows login-time rerouting before the final enter-world spawn pipeline.

## Config Keys Added

Loaded from:

- `./config/main/hub.properties`

That file is included automatically because `Config.loadProperties()` loads all default property files from:

- `./config/administration/*`
- `./config/main/*`
- `./config/network/*`

Config keys:

- `gameserver.hub.login.routing.enabled`
- `gameserver.hub.login.destination.world_id`
- `gameserver.hub.login.destination.x`
- `gameserver.hub.login.destination.y`
- `gameserver.hub.login.destination.z`
- `gameserver.hub.login.destination.heading`

## Default Behavior When Disabled

- The service is effectively a no-op.
- Login flow remains classic.
- The existing fortress/vortex relocation logic still runs unchanged.
- No hub destination is applied.
- An explicit log line is written indicating that classic login flow was used.

## Logging Behavior

The new routing service logs:

- classic login flow used when routing is disabled
- hub login routing used when routing is enabled
- fallback to classic flow if hub routing fails because of invalid destination configuration

## Implementation Notes

- The routing service uses `World.getInstance().setPosition(...)` directly.
- `TeleportService` internals were intentionally not changed.
- `CM_LEVEL_READY` flow was intentionally not changed.
- Generic spawn logic was intentionally not changed.
- Login-server behavior was intentionally not changed.

## Risks / Follow-up Work

- With hub routing enabled, the player's runtime position is changed before login completes. Because `Player.setPosition(...)` also updates `PlayerCommonData`, logging out from the hub may persist the hub location as the character's latest saved location.
- This is acceptable for the first MVP hook, but return-to-open-world semantics are not implemented yet.
- If hub mode is later disabled after players have logged out in hub space, they may re-enter at their last persisted location unless additional origin tracking is implemented.
- No validation beyond world-position creation is added yet for "is this destination a valid hub design target". This is assumed to be handled by correct config.
- Instance-based hub routing is still pending and should be implemented separately.

## Assumptions

- A normal world-map destination is sufficient for the first MVP step.
- Skipping fortress/vortex relocation when hub routing is active is acceptable because hub routing intentionally overrides the normal login destination.
- Falling back to classic login flow on invalid hub config is safer than failing the login entirely.
