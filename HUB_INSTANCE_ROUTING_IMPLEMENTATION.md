# Hub Instance Routing Implementation

## Purpose

Move hub login routing off the shared Sanctum world map and into an isolated instance-based hub context, while preserving classic open-world behavior when hub routing is disabled.

## Chosen Instance Strategy

Use a dedicated hub routing service that creates or reuses a shared hub instance on an existing `instance="true"` map.

The chosen implementation path is intentionally additive:

- keep the existing `PlayerEnterWorldService` hook
- keep the existing `gameserver.hub.login.routing.enabled` feature flag
- add a hub-specific instance routing layer
- avoid changing generic `InstanceService` logic
- avoid touching shared Sanctum spawns, arena scripts, or open-world spawn definitions

## Chosen Map/Context And Why

Chosen map:

- `310080000 = Sanctum_Underground_Arena`

Why this map was chosen for MVP:

- it is already an `instance="true"` world map
- it has valid geometry and valid known coordinates
- it does not have a dedicated instance handler in the current repository, so it can be reused with a custom hub handler
- it allows isolation from shared Sanctum world behavior without changing shared Sanctum content

Important implementation detail:

- the hub instance is created through `InstanceService.getNextAvailableInstance(...)`
- a custom `HubInstanceHandler` supplier is passed in
- this uses the existing instance creation pattern
- because a custom supplier is used, regular static instance spawns are not loaded through the default `SpawnEngine.spawnInstance(...)` path

That keeps the MVP hub instance effectively empty for now.

## Hook Path Used

Existing hook path kept:

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `enterWorld(AionConnection client, Player player)`

Routing flow now:

1. `PlayerEnterWorldService` calls `HubLoginRoutingService.routePlayerOnLogin(player)`
2. `HubLoginRoutingService` checks the feature flags
3. if instance routing is enabled, it delegates to `HubInstanceRoutingService`
4. `HubInstanceRoutingService` resolves or creates the isolated hub instance
5. the player is registered to that instance and positioned into it before normal enter-world continuation

## Config Keys

Loaded from:

- `./config/main/hub.properties`
- optionally overridden by `./config/mygs.properties`

Existing key kept:

- `gameserver.hub.login.routing.enabled`

New primary instance-routing keys:

- `gameserver.hub.login.instance.enabled`
- `gameserver.hub.login.instance.world_id`
- `gameserver.hub.login.instance.x`
- `gameserver.hub.login.instance.y`
- `gameserver.hub.login.instance.z`
- `gameserver.hub.login.instance.heading`
- `gameserver.hub.login.instance.max_players`

Legacy shared-world fallback keys kept for backward understanding:

- `gameserver.hub.login.destination.world_id`
- `gameserver.hub.login.destination.x`
- `gameserver.hub.login.destination.y`
- `gameserver.hub.login.destination.z`
- `gameserver.hub.login.destination.heading`

## Behavior When Feature Flag Is Disabled

When `gameserver.hub.login.routing.enabled = false`:

- behavior remains the classic login flow
- no hub routing is applied
- `PlayerEnterWorldService` continues as before

## Touched Files

- `game-server/src/com/aionemu/gameserver/services/hub/HubLoginRoutingService.java`
- `game-server/src/com/aionemu/gameserver/services/hub/HubInstanceRoutingService.java`
- `game-server/src/com/aionemu/gameserver/services/hub/HubInstanceHandler.java`
- `game-server/src/com/aionemu/gameserver/configs/main/HubConfig.java`
- `game-server/config/main/hub.properties`
- `game-server/config/mygs.properties`
- `HUB_INSTANCE_ROUTING_IMPLEMENTATION.md`

## Known Limitations

- The hub currently reuses an existing arena instance map geometry for isolation. It is an isolated context, but not yet a purpose-built hub map.
- The hub instance is intentionally empty for now. No hub NPCs, no hub exits, and no session UX have been added yet.
- If hub routing is disabled later while a character was previously saved inside the hub instance map, normal instance-login rules may relocate that character through the instance exit flow rather than restoring a custom hub return path.
- The implementation currently prefers a shared reusable hub instance and will create a new one only when needed.
- The chosen instance map reuse assumes that `310080000` remains safe to use with a custom handler supplier and without normal instance spawns.

## Follow-Up Tasks Before Hub NPC Work

1. Decide whether the hub should remain a shared reusable instance or become a per-session / per-player hub context.
2. Add explicit hub exit / return semantics so players do not depend on the reused arena instance exit behavior.
3. Add hub-specific spawn content in a hub-owned path, not in shared Sanctum content.
4. Decide whether the MVP should continue reusing `310080000` or move to a more dedicated hub map/context later.
5. Add safeguards for mode switching so disabling hub routing after hub usage has a clearly defined player return destination.

## Assumptions

- `310080000` is the safest existing minimal-risk instance map reuse path currently available because it is already instance-based and can be instantiated with a custom handler supplier.
- Reusing that map with a custom handler supplier is safer than editing shared Sanctum content or adding global branches to generic instance logic.
