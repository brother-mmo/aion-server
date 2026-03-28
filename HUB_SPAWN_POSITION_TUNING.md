# Hub Spawn Position Tuning

## Purpose

Move the current hub login spawn inside `310080000` away from the arena-side starting box so players do not appear directly in front of closed gate geometry.

## Touched Files

- `game-server/config/main/hub.properties`
- `game-server/config/mygs.properties`
- `HUB_SPAWN_POSITION_TUNING.md`

## Change Summary

Updated hub instance spawn coordinates for:

- `gameserver.hub.login.instance.x`
- `gameserver.hub.login.instance.y`
- `gameserver.hub.login.instance.z`
- `gameserver.hub.login.instance.heading`

Old values:

- `232.05 / 237.106 / 158.917 / 1`

New values:

- `275.7 / 215.9 / 160.0 / 30`

## Why This Position

The previous coordinates matched one of the explicit arena spawn clusters from:

- `game-server/data/static_data/spawns/Instances/310080000_Sanctum_Underground_Arena.xml`

That location places the player in an arena-side boxed area near gate geometry.

The new position is a midpoint-style placement between the three known spawn clusters on the map:

- Drynac cluster around `232 / 240 / 159`
- Kirrin cluster around `316 / 240 / 159`
- Tigric cluster around `276 / 167 / 162`

This is intended to be a more neutral central placement for MVP testing.

## Known Limitations

- This is still a reused arena geometry, not a purpose-built hub map.
- The new position is chosen from available server-side coordinate evidence, not from full client-side scene authoring data.
- Additional tuning may still be needed after a live client check.

## Next Recommended Step

If this position is still visually poor, test one of the neutral instance test maps as the next hub candidate instead of continuing to tune arena geometry.
