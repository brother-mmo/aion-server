# AGENTS.md

## Project Goal

Build a session-first Hub mode on top of Beyond Aion 4.8.

Core product direction:

- Support a session-first hub mode for MVP development.
- Preserve classic open-world gameplay as an optional mode.
- Keep one shared character progression across hub and open world.
- Prefer additive extensions over invasive rewrites.
- Use feature flags for new behavior whenever practical.
- Avoid deep rewrites unless clearly necessary.

## Working Principles

- When unsure, map code first.
- Do not silently change unrelated gameplay systems.
- Prefer configuration over hardcoding where practical.
- Keep implementations small, scoped, and testable.
- Inspect risky code paths before editing them.
- Preserve existing behavior by default unless the task explicitly changes it.

## Preferred Change Style

- Add new services, handlers, or configuration switches instead of rewriting core flows.
- Isolate hub-specific logic behind explicit entry points.
- Reuse existing extension patterns such as instance handlers, custom services, and data-driven behavior.
- Treat login flow, teleport flow, dialog flow, instance management, and world transitions as high-risk areas.
- For high-risk shared systems, read the current flow before making changes and document assumptions.

## Feature Flag Guidance

- New hub behavior should be gated behind clear feature flags.
- Default behavior should remain compatible with classic open-world mode.
- Config keys and technical documentation must be written in English.
- Prefer toggles that allow side-by-side comparison between classic and hub-first behavior.

## Documentation Rules

- After each task, document the touched files.
- For each implemented feature, create a short markdown implementation note in English.
- Keep implementation notes short and practical:
  - purpose
  - entry points
  - touched files
  - config flags
  - risks or follow-up work
- Keep code comments in English.
- Keep markdown implementation notes and technical documentation in English.

## Editing Rules

- Do not make broad unrelated cleanups while implementing hub work.
- Do not refactor stable shared systems without a concrete need.
- If a task touches risky shared infrastructure, inspect first and minimize the patch.
- If a safer wrapper or extension point exists, prefer it over changing global behavior.
- If a change affects both hub and classic modes, make the mode split explicit.

## Risk Awareness

Inspect before editing these areas:

- player login flow
- character enter-world and spawn flow
- teleportation pipeline
- NPC dialog pipeline
- instance registration and instance lifecycle
- world position and transition systems

Examples of likely risky files include:

- `game-server/src/com/aionemu/gameserver/services/player/PlayerEnterWorldService.java`
- `game-server/src/com/aionemu/gameserver/services/player/PlayerService.java`
- `game-server/src/com/aionemu/gameserver/services/teleport/TeleportService.java`
- `game-server/src/com/aionemu/gameserver/services/DialogService.java`
- `game-server/src/com/aionemu/gameserver/services/instance/InstanceService.java`
- `game-server/src/com/aionemu/gameserver/world/World.java`
- `game-server/src/com/aionemu/gameserver/world/WorldMapInstance.java`

## Recommended Workflow

1. Map the code path before changing behavior.
2. Identify the smallest viable extension point.
3. Add feature-flagged behavior.
4. Keep the patch small and testable.
5. Document touched files and create a short implementation note.

## Language Rules

- The user may communicate in Russian.
- Respond to the user in Russian unless asked otherwise.
- Write code comments, markdown implementation notes, config keys, and technical documentation in English.
