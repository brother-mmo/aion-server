# Hub Isolation Analysis

## Purpose

Analyze the current MVP hub destination and identify the safest way to isolate hub behavior without breaking classic Beyond Aion 4.8 world content.

This document is analysis only. No gameplay behavior is changed here.

## Current Hub Destination

### Configured destination

- `game-server/config/main/hub.properties`
- `game-server/config/mygs.properties`

Current local hub login destination:

- `world_id = 110010000`
- `x = 1462.5`
- `y = 1326.1`
- `z = 564.1`
- `heading = 0`

### Current map/context analysis

The configured hub destination is **not** a dedicated hub map and **not** an instance map.

It currently points to:

- `110010000 = Sanctum`

Relevant files:

- `game-server/src/com/aionemu/gameserver/world/WorldMapType.java`
- `game-server/data/static_data/world_maps.xml`

Key observation:

- `world_maps.xml` defines `110010000` as `Sanctum`
- `instance="true"` is **not** present for Sanctum
- the map has normal open-world flags such as `RECALL`, `GLIDE`, `RIDE`, `PVP`, `DUEL_SAME_RACE`

This means the current hub destination is a **shared normal world map context**.

### Shared world vs instance behavior

Relevant files:

- `game-server/src/com/aionemu/gameserver/world/WorldMap.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstanceFactory.java`
- `game-server/src/com/aionemu/gameserver/spawnengine/SpawnEngine.java`
- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`

What the code suggests:

- Non-instance maps are created as regular `WorldMap` entries with default world instances/channels.
- `SpawnEngine.spawnAll()` loads static world spawns for every non-instance map.
- Instance-type maps are created and populated through `InstanceService` / `WorldMapInstanceFactory` / `SpawnEngine.spawnInstance(...)`.

Practical conclusion:

- The current hub location is using the **main Sanctum world context**, not a private or hub-specific instance context.
- Any direct content changes to Sanctum spawns or zone behavior would affect the classic map as well.

## Arena/Coliseum Context Around the Current Hub Spot

The current hub coordinates overlap with existing Sanctum arena / PvP-related content.

Relevant files:

- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/data/handlers/zone/pvpZones/PvPAreaZone.java`
- `game-server/data/static_data/zones/zones_110010000.xml`
- `game-server/data/static_data/instance_exit/instance_exit.xml`

Evidence:

- `DialogService` already teleports players to `110010000 1462.5 1326.1 564.1` for Sanctum PvP arena entry.
- `PvPAreaZone` handles `LC1_PVP_SUB_C_110010000` and teleports players out to nearby Sanctum coordinates on leave.
- `zones_110010000.xml` contains:
  - `COLISEUM_110010000`
  - `COLISEUM_1_110010000`
  - `LC1_PVP_SUB_C_110010000`
- `instance_exit.xml` contains exits from `310080000` (Sanctum Underground Arena) back into nearby Sanctum coordinates.

Practical conclusion:

- The current hub location is not an empty safe staging area.
- It is part of an existing arena / coliseum flow and already has built-in entry/exit expectations.

## How NPC Spawns Are Defined For This Location

### Main Sanctum spawns

Relevant files:

- `game-server/data/static_data/spawns/Npcs/110010000_Sanctum.xml`
- `game-server/data/static_data/spawns/Statics/110010000_Sanctum.xml`

These are shared world spawn definitions for Sanctum.

### Additional custom spawns also targeting Sanctum

Relevant files:

- `game-server/data/static_data/spawns/Npcs/Custom/Training_Dummies.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Warehouse_Managers.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Stigma_Masters.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Skill_CD_Reset_Shugo.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Morph_Recipe_Merchants.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Custom_Instance.xml`

### Event spawns also targeting Sanctum

Relevant files:

- `game-server/data/static_data/events/timed_events/retail_events.xml`
- `game-server/data/static_data/events/timed_events/custom_events.xml`

### Nearby spawn evidence

Examples found near the current hub area:

- `game-server/data/static_data/spawns/Npcs/110010000_Sanctum.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/Training_Dummies.xml`

Practical conclusion:

- NPCs in this area are not controlled by one isolated arena file.
- They come from multiple shared sources:
  - base Sanctum spawns
  - custom Sanctum spawns
  - timed event spawns
- Modifying the area directly would likely require touching several shared content sources.

## Would Modifying This Location Directly Affect Classic Gameplay?

### Short answer

Yes, very likely.

### Why it is risky

Sanctum `110010000` is referenced by many classic systems:

- shared NPC/Static spawns
- portal locations
- teleport destinations
- return scroll aliases
- quest teleports
- arena entry/exit logic
- zone handlers
- event spawns

Relevant files/configs likely involved:

- `game-server/data/static_data/portals/portal_loc.xml`
- `game-server/data/static_data/teleport_location.xml`
- `game-server/data/static_data/items/multi_return_item.xml`
- `game-server/data/static_data/items/item_templates.xml`
- `game-server/data/handlers/quest/...`
- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/data/handlers/zone/pvpZones/PvPAreaZone.java`
- `game-server/src/com/aionemu/gameserver/model/gameobjects/player/Player.java`

### Direct modification risk summary

If Sanctum arena coordinates are reused as the hub and we directly suppress or repurpose local content on map `110010000`, the likely side effects are:

- breaking original Sanctum arena usage
- breaking quest teleports or scripted arena flows
- breaking event NPC visibility or timing in Sanctum
- changing player expectations on a classic capital map
- introducing cross-mode coupling between hub mode and classic world behavior

## Isolation Strategy Options

### Option A: Temporary suppression of original NPCs for hub mode only

Description:

- Keep using Sanctum at the current location.
- Hide or despawn original NPCs when hub mode is active.

Pros:

- Smallest short-term visible change.
- Reuses current geometry and coordinates.

Cons:

- High-risk on a shared map.
- Requires filtering multiple spawn sources.
- Needs careful handling of events, quests, and arena scripts.
- Easy to break classic content accidentally.

Assessment:

- **Not the safest MVP path.**

### Option B: Separate hub spawn set on the same shared map

Description:

- Keep the same Sanctum location.
- Add hub-only spawns while trying not to disturb original content.

Pros:

- Additive in content authoring terms.
- Fast to prototype visually.

Cons:

- Original arena NPCs, exits, and PvP zone logic still exist underneath.
- Does not solve map-level contamination.
- Still couples hub mode to shared Sanctum behavior.

Assessment:

- **Safer than deleting original NPCs, but still risky.**

### Option C: Separate instance-based hub

Description:

- Route hub players into a dedicated instance context.
- Use a map that runs as `instance="true"` or introduce a dedicated hub instance map later.
- Populate the hub with its own spawn set only.

Pros:

- Strong isolation from classic world content.
- Cleanest way to avoid original NPCs/exits.
- Fits the session-first product direction well.
- Preserves classic Sanctum behavior unchanged.

Cons:

- Requires proper instance entry/creation flow.
- Needs hub-specific spawn and exit rules.
- Slightly larger MVP step than simple coordinate routing.

Assessment:

- **Safest MVP isolation strategy overall.**

### Option D: Duplicated hub map usage / separate world context

Description:

- Create a dedicated duplicate map usage or a separate world context for hub players.

Pros:

- Architecturally clean if designed carefully.
- Can support hub-only rules and content cleanly.

Cons:

- Larger content and infrastructure effort.
- Depends on how map assets and spawn data are intended to be duplicated in this codebase.

Assessment:

- **Good long-term direction if a dedicated hub map is planned, but heavier than needed for the next MVP step.**

## Recommendation For MVP

### Safest MVP recommendation

Use a **separate instance-based hub** instead of modifying shared Sanctum content directly.

Recommended MVP direction:

1. Keep the current login hook approach in `PlayerEnterWorldService` / `HubLoginRoutingService`.
2. Replace the current direct Sanctum world routing with routing into a hub-specific instance context.
3. Keep hub spawns separate from shared Sanctum spawns.
4. Do not suppress or delete original Sanctum NPCs as the first isolation step.

Why this is the safest MVP path:

- It preserves classic open-world behavior.
- It avoids touching shared Sanctum content files.
- It prevents original arena exits/NPCs from leaking into the hub experience.
- It keeps future hub UX work additive.

### MVP fallback if instance hub is deferred

If a true instance-based hub must be deferred for one interim step, the least risky fallback is:

- keep the current Sanctum routing temporary
- do **not** remove original NPCs yet
- do **not** rewrite Sanctum spawn definitions
- move toward a hub-specific instance as the next isolation milestone

This fallback should be treated as temporary only.

## Recommendation For Long-Term Clean Architecture

Preferred architecture:

- dedicated hub context
- explicit hub-only spawn set
- explicit hub-only exits/transition rules
- classic world flow preserved behind feature flags / routing decisions

Possible long-term shapes:

- a dedicated hub instance map
- a dedicated duplicated hub world context
- a dedicated hub instance handler and spawn package

Avoid as a long-term solution:

- permanent reuse of shared Sanctum arena space as the live hub
- mode-specific spawn suppression on shared capital maps
- hidden coupling between hub logic and classic Sanctum scripts

## Concrete Files/Configs Likely Involved

### Already relevant for current hub entry

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/src/com/aionemu/gameserver/services/hub/HubLoginRoutingService.java`
- `game-server/src/com/aionemu/gameserver/configs/main/HubConfig.java`
- `game-server/config/main/hub.properties`
- `game-server/config/mygs.properties`

### Relevant for current shared-map risk

- `game-server/data/static_data/world_maps.xml`
- `game-server/src/com/aionemu/gameserver/world/WorldMap.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstanceFactory.java`
- `game-server/src/com/aionemu/gameserver/spawnengine/SpawnEngine.java`
- `game-server/data/static_data/spawns/Npcs/110010000_Sanctum.xml`
- `game-server/data/static_data/spawns/Statics/110010000_Sanctum.xml`
- `game-server/data/static_data/spawns/Npcs/Custom/*.xml`
- `game-server/data/static_data/events/timed_events/retail_events.xml`
- `game-server/data/static_data/events/timed_events/custom_events.xml`
- `game-server/data/static_data/zones/zones_110010000.xml`
- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/data/handlers/zone/pvpZones/PvPAreaZone.java`
- `game-server/data/static_data/portals/portal_loc.xml`
- `game-server/data/static_data/teleport_location.xml`
- `game-server/data/static_data/instance_exit/instance_exit.xml`

### Likely relevant for a future isolated hub instance

- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`
- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstanceFactory.java`
- `game-server/data/static_data/spawns/Instances/*.xml`
- `game-server/data/handlers/instance/*.java`

## Needs Code Confirmation

- Whether the best short-term isolated hub should reuse an existing instance map geometry or introduce a new dedicated hub map/context.
- Whether a hub-specific instance should be single shared, per-player, or per-session-group for the intended MVP.
- Whether any current arena-related instance maps can be safely reused without inheriting unwanted original encounter logic.
- Whether any hub-only spawn package should be implemented via static instance spawns, temporary spawns, or a dedicated instance handler.

## Touched Files

- `HUB_ISOLATION_ANALYSIS.md`
