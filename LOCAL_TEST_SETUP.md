# Local Test Setup

## Purpose

Provide a repeatable local setup for testing the Beyond Aion 4.8 Hub MVP on Windows with:

- Java and Maven installed on the host
- MariaDB running in Docker
- local override configs in `my*.properties`
- hub login routing enabled for local verification

## Touched Files

- `docker-compose.local.yml`
- `docker/db/init/10-aion-init.sh`
- `login-server/config/myls.properties`
- `game-server/config/mygs.properties`
- `chat-server/config/mycs.properties`

## Host Tools

- JDK 25
- Maven 3.9+
- Docker Desktop with Docker Compose

## Database

Start the local database:

```powershell
docker compose -f docker-compose.local.yml up -d
```

This creates and initializes:

- `aion_ls`
- `aion_gs`
- `aion_cs`

It also registers the default game server in `aion_ls.gameservers` with:

- `id = 1`
- `mask = 127.0.0.1`
- `password = 1234`

If you need a fresh database initialization, remove the volume first:

```powershell
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d
```

## Config Overrides

The following override files are loaded automatically and take precedence over default configs:

- `login-server/config/myls.properties`
- `game-server/config/mygs.properties`
- `chat-server/config/mycs.properties`

The local overrides:

- point all servers to the Docker database on `127.0.0.1:3306`
- use `aion / aion` as database credentials
- force the local game server connect address to `127.0.0.1:7777`
- enable hub login routing for local testing

To switch back to classic login behavior locally, set:

```properties
gameserver.hub.login.routing.enabled = false
```

## Running the Servers

### One-command local scripts

From the repository root:

```bat
run-local-servers.bat
```

To stop the local stack:

```bat
stop-local-servers.bat
```

The start script:

- starts or resumes the Docker database
- checks for required packaged server archives
- extracts local runtime folders into `.local/runtime`
- copies `my*.properties` overrides
- opens separate console windows for login and game server

### IntelliJ / IDE run mode

Use these main classes with the module directory as working directory:

- `com.aionemu.loginserver.LoginServer` with working directory `login-server`
- `com.aionemu.gameserver.GameServer` with working directory `game-server`
- `com.aionemu.chatserver.ChatServer` with working directory `chat-server` (optional, because chat is disabled by default in game server config)

### Maven package mode

Build everything from the repository root:

```powershell
mvn package
```

## Client Test

Use the patched Aion 4.8 client and start it against the login server:

```batch
start /affinity 7FFFFFFF "" "bin64\AION.bin" -ip:127.0.0.1 -port:2106 -cc:2 -lang:ENG -loginex
```

## Notes

- This setup is intentionally local-first and loopback-only.
- The chat server is optional for the first MVP hub checks.
- The hub destination is currently a plain world position, not an instance-based hub.
- If the game server must be reachable from another machine, update the login server `gameservers.mask` entry and the advertised game server connect address.
