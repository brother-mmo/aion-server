# Hub Exit Foundation

## Purpose

Add an explicit hub-owned path from the isolated hub instance into classic open world, without depending on the reused arena `instance_exit.xml` behavior.

This step provides a safe callable foundation for future hub NPC/dialog code.

## Exit Path Used

Primary explicit path:

- `game-server/src/com/aionemu/gameserver/services/hub/HubExitService.java`
- `HubExitService.exitToConfiguredOpenWorld(Player player)`

Hub-specific instance leave path:

- `game-server/src/com/aionemu/gameserver/services/hub/HubInstanceHandler.java`
- `leaveInstance(Player player)`

Current behavior:

- future hub NPC/dialog code can call `HubExitService.exitToConfiguredOpenWorld(player)`
- `CM_INSTANCE_LEAVE` inside the hub instance now goes through `HubInstanceHandler.leaveInstance(player)`
- `HubInstanceHandler.leaveInstance(player)` delegates to `HubExitService`

That makes the hub exit path explicit and hub-owned.

## Config Keys Added

Loaded from:

- `./config/main/hub.properties`
- optionally overridden by `./config/mygs.properties`

New keys:

- `gameserver.hub.exit.open_world.world_id`
- `gameserver.hub.exit.open_world.x`
- `gameserver.hub.exit.open_world.y`
- `gameserver.hub.exit.open_world.z`
- `gameserver.hub.exit.open_world.heading`

## Touched Files

- `game-server/src/com/aionemu/gameserver/services/hub/HubExitService.java`
- `game-server/src/com/aionemu/gameserver/services/hub/HubInstanceHandler.java`
- `game-server/src/com/aionemu/gameserver/configs/main/HubConfig.java`
- `game-server/config/main/hub.properties`
- `game-server/config/mygs.properties`
- `HUB_EXIT_FOUNDATION.md`

## What Behavior Is Now Explicit

Explicit now:

- hub instance exit target is configuration-driven
- hub exit can be invoked through a dedicated service
- leaving the hub instance through `CM_INSTANCE_LEAVE` no longer depends on the reused arena exit mapping

Still pending:

- hub NPC/dialog UX that calls the service
- return-to-hub from open world
- hub-specific exit choices or branching destinations
- race-aware or faction-aware open-world exit targets
- preserving a hub origin / open-world return stack

## Known Limitations

- The default exit destination is a single configured open-world position. It is not yet dynamic per race, class, or progression state.
- The current default exit target assumes the current MVP hub flow is Sanctum-side.
- If explicit hub exit fails because of invalid config or invalid call context, the handler does not fall back to the reused arena exit flow.
- This step does not add any NPCs or UI that expose the exit to players yet. It only creates the callable foundation and the handler override.

## Next Recommended Step Before Hub NPC Or Session UX

1. Add one minimal hub-controlled interaction that calls `HubExitService.exitToConfiguredOpenWorld(player)`.
2. Define whether the open-world exit should be single-target or race-aware.
3. Add explicit origin/return semantics so hub and open-world travel become reversible in a controlled way.
4. Decide whether leaving hub should unregister players from the shared hub instance or simply rely on the existing instance lifecycle.

## Assumptions

- The least invasive approach is to keep global `TeleportService` and global instance exit logic untouched, and only override `leaveInstance(Player)` for the hub-specific handler.
- A single configurable open-world destination is sufficient as the MVP foundation before hub NPC work begins.
