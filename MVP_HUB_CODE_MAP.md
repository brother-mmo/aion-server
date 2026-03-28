# MVP Hub Code Map

## Goal of This Mapping Pass

Prepare for a new session-first Hub mode without changing gameplay behavior yet.

Working assumptions for this pass:

- Hub mode should be additive, not a rewrite of open-world flow.
- Classic open-world behavior must remain available.
- Character progression stays shared between hub and open world.
- Login routing should happen before the player is fully spawned into the world, not as a post-login correction teleport.

## Repository Structure at a Glance

- `login-server/`
  - Account authentication, server selection, session key handoff to the game server.
- `game-server/`
  - Main runtime logic for player lifecycle, world state, teleports, dialogs, instances, zones, and handlers.
- `game-server/src/com/aionemu/gameserver/network/`
  - Client packet entry points and login/chat-server integration.
- `game-server/src/com/aionemu/gameserver/services/`
  - Central orchestration layer for player login, teleportation, dialogs, instance handling, housing, etc.
- `game-server/src/com/aionemu/gameserver/world/`
  - Core world, world maps, instances, positions, regions, zone hookup.
- `game-server/src/com/aionemu/gameserver/controllers/`
  - Runtime behavior for players, NPCs, and visible objects.
- `game-server/src/com/aionemu/gameserver/instance/`
  - Instance engine and handler interfaces/base classes.
- `game-server/data/handlers/`
  - Script-style zone and instance handlers, likely the safest pattern for isolated gameplay extensions.
- `game-server/data/static_data/`
  - XML-driven world maps, teleports, portals, spawn/start positions, instance exits, NPC templates.
- `game-server/src/com/aionemu/gameserver/custom/`
  - Existing custom feature examples. `custom/pvpmap` is especially useful as a pattern for isolated session-style content.

## 1. Player Login Flow

### Likely code paths

- `login-server/src/com/aionemu/loginserver/network/aion/clientpackets/CM_SERVER_LIST.java`
- `login-server/src/com/aionemu/loginserver/network/aion/clientpackets/CM_PLAY.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_L2AUTH_LOGIN_CHECK.java`
- `game-server/src/com/aionemu/gameserver/network/loginserver/LoginServer.java`
- `game-server/src/com/aionemu/gameserver/network/loginserver/clientpackets/CM_ACOUNT_AUTH_RESPONSE.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_CHARACTER_LIST.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_MAY_LOGIN_INTO_GAME.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_ENTER_WORLD.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_CHARACTER_PASSKEY.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_CHARACTER_EDIT.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerService.java`
- `game-server/src/com/aionemu/gameserver/services/AccountService.java`

### Flow summary

1. Login server validates account and selected game server.
2. Game server re-validates the session handoff via `CM_L2AUTH_LOGIN_CHECK -> LoginServer`.
3. Character list is sent.
4. Character enter request reaches `CM_ENTER_WORLD`.
5. `PlayerEnterWorldService.enterWorld(...)` loads the character, attaches connection/account state, validates reentry/ban/passkey conditions, then begins full login initialization.

### What looks safe to extend

- Add a dedicated hub routing service and call it from `PlayerEnterWorldService`.
- Add new configuration or feature flag checks near login routing, not inside low-level auth/session code.
- Use a new package such as `game-server/src/com/aionemu/gameserver/services/hub/` or `.../custom/hub/`.

### What looks risky to modify

- `login-server` session-key and `SM_PLAY_OK` / `CM_L2AUTH_LOGIN_CHECK` handshake.
- `game-server/src/com/aionemu/gameserver/network/loginserver/LoginServer.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerService.java` if persistence loading rules are changed globally.
- Character/account auth state transitions in `AionConnection`.

### Notes

- Hub login routing should almost certainly live on the game-server side, after the character is loaded, not in the login-server handshake.
- `custom/pvpmap` shows that isolated session-like systems already exist without replacing the global login flow.

### Uncertainties

- Exact reconnect behavior when a player disconnects from hub versus open world needs code confirmation.
- Whether any downstream systems assume the persisted `PlayerCommonData` position must always match the first map shown on login needs code confirmation.

## 2. Character Spawn / Enter World

### Likely code paths

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_LEVEL_READY.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_TELEPORT_ANIMATION_DONE.java`
- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`
- `game-server/src/com/aionemu/gameserver/controllers/PlayerController.java`

### Flow summary

- `PlayerEnterWorldService` prepares the loaded player and sends initial login packets.
- `CM_LEVEL_READY` is the actual "map is loaded, spawn me now" moment.
- `World.getInstance().spawn(activePlayer)` performs the world spawn.
- `PlayerController.onEnterWorld()` and `QuestEngine.onEnterWorld(...)` run post-spawn behavior.

### What looks safe to extend

- Introduce a small routing/bootstrap step before the player reaches `CM_LEVEL_READY`.
- Add hub-specific post-spawn behavior in a dedicated service or instance handler, not inside generic `World.spawn(...)`.
- If hub is instance-based, prefer a dedicated handler class over adding special cases into generic player/world controllers.

### What looks risky to modify

- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`
- `game-server/src/com/aionemu/gameserver/controllers/PlayerController.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_LEVEL_READY.java`

### Notes

- Spawning late and teleporting immediately after login would work, but it is a worse user experience and touches more systems.
- Routing before spawn is cleaner and keeps one authoritative initial world position per login.

### Uncertainties

- Whether some visual/login packets depend on the pre-route map before `CM_LEVEL_READY` needs code confirmation.

## 3. Teleportation

### Likely code paths

- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_TELEPORT_SELECT.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_BIND_POINT_TELEPORT.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_TELEPORT_ANIMATION_DONE.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_CHANGE_CHANNEL.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_HOUSE_TELEPORT.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/TeleportService.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/BindPointTeleportService.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/PortalService.java`

### Important data-driven support

- `game-server/data/static_data/npc_teleporter.xml`
- `game-server/data/static_data/teleport_location.xml` or equivalent telelocation data backing `DataManager.TELELOCATION_DATA`
- `game-server/data/static_data/portals/portal_template2.xml`
- `game-server/data/static_data/player_initial_data.xml`
- `game-server/data/static_data/instance_exits.xml`

### Flow summary

- Standard NPC teleport: `CM_TELEPORT_SELECT -> TeleportService.validateTeleporterAndGetTemplate(...) -> TeleportService.teleport(...)`
- Bind point teleport: `CM_BIND_POINT_TELEPORT -> BindPointTeleportService`
- Portal/instance transfer: `PortalService.port(...)`
- Actual relocation pipeline: `TeleportService.sendLoc(...) -> SpawnTask -> World.setPosition(...)`

### What looks safe to extend

- Build a dedicated `HubTeleportService` that delegates to `TeleportService.teleportTo(...)`.
- Add hub return/exit locations through new data or new wrapper services, not by changing generic teleport math.
- Use `PortalService` and portal data if the hub should be entered through explicit portal/NPC interactions.

### What looks risky to modify

- `game-server/src/com/aionemu/gameserver/services/teleport/TeleportService.java`
  - Especially `sendLoc(...)`, `spawnOnSameMap(...)`, and inner `SpawnTask`.
- Global bind-point behavior in `BindPointTeleportService`.
- Global channel switching in `CM_CHANGE_CHANNEL` / `TeleportService.changeChannel(...)`.

### Notes

- `TeleportService` is central infrastructure. Prefer wrapping it, not branching inside it for hub logic unless absolutely necessary.
- `PortalService` already contains group/alliance/instance entry rules and is a good place to integrate hub exits or controlled returns later.

### Uncertainties

- Exact telelocation XML file naming for this fork should be rechecked in the next pass.
- If hub uses a world with channels/twins, channel-change interaction needs code confirmation.

## 4. NPC Dialog Handling

### Likely code paths

- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_SHOW_DIALOG.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_DIALOG_SELECT.java`
- `game-server/src/com/aionemu/gameserver/controllers/NpcController.java`
- `game-server/src/com/aionemu/gameserver/controllers/CreatureController.java`
- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/src/com/aionemu/gameserver/questEngine/QuestEngine.java`
- `game-server/src/com/aionemu/gameserver/dataholders/Portal2Data.java`
- `game-server/src/com/aionemu/gameserver/model/DialogAction.java`

### Flow summary

- Open dialog: `CM_SHOW_DIALOG -> NpcController.onDialogRequest(...)`
- Select dialog action: `CM_DIALOG_SELECT -> NpcController.onDialogSelect(...)`
- Then one of:
  - NPC AI handles it
  - `DialogService` handles function dialogs
  - `QuestEngine` handles quest dialogs
  - `Portal2Data` / `PortalService` may participate for portal-style NPCs

### What looks safe to extend

- Add dedicated hub NPC AI or dedicated hub dialog handler path.
- Add new portal-driven dialog entries through portal data if hub travel should be exposed by a portal NPC.
- Reuse the pattern of isolated handlers/scripts in `game-server/data/handlers/`.

### What looks risky to modify

- The large switch in `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- Generic `QuestEngine.onDialog(...)` behavior
- Generic NPC interaction validation in `CM_DIALOG_SELECT`

### Notes

- `DialogService` is broad and shared by many game systems.
- If hub needs one or two dedicated NPCs, isolated AI/handler code is safer than adding more global switch cases.

### Uncertainties

- Which existing dialog action IDs are easiest to reuse or extend for hub-only NPCs needs code confirmation.
- Whether this fork prefers AI-script-driven dialog logic or centralized `DialogService` additions for new custom content needs code confirmation.

## 5. Instance Entry / Instance Management

### Likely code paths

- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`
- `game-server/src/com/aionemu/gameserver/instance/InstanceEngine.java`
- `game-server/src/com/aionemu/gameserver/instance/handlers/InstanceHandler.java`
- `game-server/src/com/aionemu/gameserver/instance/handlers/GeneralInstanceHandler.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_INSTANCE_LEAVE.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_INSTANCE_INFO.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/PortalService.java`
- `game-server/data/handlers/instance/`

### Flow summary

- New instance creation: `InstanceService.getNextAvailableInstance(...)`
- Handler binding: `InstanceEngine.getNewInstanceHandler(...)`
- Player login and instance restoration: `InstanceService.onPlayerLogin(...)`
- Enter/leave callbacks:
  - `InstanceService.onEnterInstance(...)`
  - `InstanceService.onLeaveInstance(...)`
  - `InstanceHandler.leaveInstance(...)`
  - `InstanceHandler.onEnterInstance(...)`
  - `InstanceHandler.onLeaveInstance(...)`

### What looks safe to extend

- Create a dedicated `HubInstanceHandler` if hub is an instance.
- Use `InstanceService.getNextAvailableInstance(...)` with a dedicated handler supplier, similar to `custom/pvpmap`.
- Keep hub rules isolated in instance handler callbacks instead of patching `GeneralInstanceHandler`.

### What looks risky to modify

- `InstanceService.onPlayerLogin(...)`
- Generic registration rules in `InstanceService` and `PortalService`
- `GeneralInstanceHandler` if hub-specific assumptions leak into all instances
- Global instance cooldown and registration logic

### Notes

- Existing code already supports special-purpose instance handlers and custom instance services.
- A session-first hub implemented as a dedicated shared instance looks feasible with current infrastructure.

### Uncertainties

- Whether the hub should be:
  - a shared non-personal instance,
  - a personal instance,
  - or a normal world map
  needs code confirmation and design confirmation.
- If hub is a shared instance, max player count, cleanup rules, and reconnect rules need code confirmation.

## 6. World Transitions

### Likely code paths

- `game-server/src/com/aionemu/gameserver/services/teleport/TeleportService.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/PortalService.java`
- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`
- `game-server/src/com/aionemu/gameserver/controllers/PlayerController.java`
- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_CHANGE_CHANNEL.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_WINDSTREAM.java`
- `game-server/src/com/aionemu/gameserver/network/aion/clientpackets/CM_HOUSE_TELEPORT.java`

### Flow summary

- Most world/instance relocation eventually passes through `TeleportService`.
- True map change behavior is handled in `TeleportService.SpawnTask`.
- Zone enter/leave callbacks flow through `PlayerController.onEnterZone(...)` / `onLeaveZone(...)`.
- Instance enter/leave callbacks flow through `InstanceService`.

### What looks safe to extend

- Add a dedicated service to capture and restore "origin world position" for hub entry/exit.
- Add hub-specific enter/leave logic in a dedicated hub service or instance handler.
- Use wrapper methods around `TeleportService.teleportTo(...)` for all hub transitions.

### What looks risky to modify

- `World.updatePosition(...)`, `World.setPosition(...)`, or `World.spawn(...)` globally
- Zone revalidation and `WorldMapInstance` bookkeeping
- Generic player enter/leave zone behavior in `PlayerController`

### Notes

- Hub mode should likely own its own "return destination" model instead of overloading bind points or persisted logout position.
- `TeleportService.moveToInstanceExit(...)` and `PortalService.transfer(...)` are good references for controlled return paths.

### Uncertainties

- Some transitions may bypass `TeleportService` and use direct `World.setPosition(...)` during edge cases such as login relocation, revive logic, or siege relocation. This needs code confirmation before implementing full hub return semantics.

## First Recommended Implementation Hook for "Redirect Player to Hub on Login"

### Recommended hook

Add a single additive call inside:

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`

Specifically:

- After the character is fully loaded into a `Player`
- Before the player is fully initialized for world entry
- Before `InstanceService.onPlayerLogin(player)`
- Before any final spawn/map-load sequence is completed

### Best first insertion point

Most likely inside `PlayerEnterWorldService.enterWorld(AionConnection client, Player player)`:

- after `Player player = PlayerService.getPlayer(...)` has completed
- before `World.getInstance().storeObject(player)` and before the fortress/vortex relocation logic

This gives the new hook the cleanest control over:

- initial map/world selection
- initial instance selection
- whether classic open-world position should be preserved or replaced
- keeping the rest of login initialization unchanged

### Why this looks best

- It is early enough to avoid a visible "spawn in open world, then teleport to hub" double-step.
- It is late enough that account, character, and persistence data are already available.
- It avoids touching the login-server handshake.
- It keeps `CM_LEVEL_READY`, `World.spawn(...)`, and generic teleport logic unchanged.

### Recommended additive shape

Create a new service such as:

- `game-server/src/com/aionemu/gameserver/services/hub/HubLoginRoutingService.java`

Possible responsibilities:

- `shouldRouteToHub(Player player)`
- `routeOnLogin(Player player)`
- `captureOpenWorldReturnPoint(Player player)` if needed
- `resolveHubInstance(Player player)` if hub is instance-based

### Things to avoid for the first implementation

- Do not add hub-specific branches deep inside `TeleportService`.
- Do not change login-server session/auth behavior.
- Do not rewrite `CM_LEVEL_READY`.
- Do not replace persisted player position logic globally.

### Needs code confirmation

- Whether routing should happen before or after existing fortress/vortex login relocation rules.
- Whether hub entry should preserve the original logout location as a separate stored return point.
- Whether hub instance ownership should be player-owned, shared, or static.

## Safe Extension Patterns Observed in This Fork

- Isolated custom service plus dedicated handler:
  - `game-server/src/com/aionemu/gameserver/custom/pvpmap/PvpMapService.java`
  - `game-server/src/com/aionemu/gameserver/custom/pvpmap/PvpMapHandler.java`
- Dedicated instance handlers loaded through `InstanceEngine`
- Data-driven teleports and portals via static XML
- Zone and instance scripts under `game-server/data/handlers/`

These are strong candidates for Hub implementation style.

## High-Risk Shared Systems

- `game-server/src/com/aionemu/gameserver/services/teleport/TeleportService.java`
- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerService.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`
- `login-server/src/com/aionemu/loginserver/network/aion/clientpackets/CM_PLAY.java`

These are valid hook points, but not good places for broad invasive rewrites.

## Short Recommended Step 1 Implementation Plan

1. Add a new hub routing module with no behavior enabled by default.
2. Insert one additive hook in `PlayerEnterWorldService.enterWorld(...)` that delegates to the new module.
3. Make the hook return "no-op" unless an explicit hub mode/config/eligibility condition is met.
4. Decide and document the hub target model:
   - shared instance,
   - personal instance,
   - or normal world map.
5. Add a small origin/return-position model if hub login must later return the player to open world cleanly.
6. Only after the hook exists, implement a minimal hub destination and a controlled login redirect behind a feature flag.
