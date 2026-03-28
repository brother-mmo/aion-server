# Local Run Scripts

## Purpose

Add one-command local start and stop scripts for the Windows development stack.

## Touched Files

- `run-local-servers.bat`
- `stop-local-servers.bat`
- `LOCAL_TEST_SETUP.md`

## Entry Points

- `run-local-servers.bat`
- `stop-local-servers.bat`

## What The Start Script Does

- checks that no local login or game server Java process is already running
- ensures the Docker database is up and healthy
- builds server archives with Maven only if the required zip files are missing
- extracts `login-server.zip` and `game-server.zip` into `.local/runtime`
- copies local override configs from `my*.properties`
- starts the login server and game server in separate console windows

## What The Stop Script Does

- stops local Java processes for login, game and chat server mains
- closes the matching console windows
- stops the local Docker MariaDB container without deleting data

## Assumptions

- JDK 25 is installed locally
- Docker Desktop is available and running
- `docker-compose.local.yml` remains the source of truth for the local database
- the game server is tested without the chat server by default

## Risks / Follow-up

- the start script does not auto-rebuild when source files change if the zip files already exist; rerun `mvn package` after code changes
- the stop script identifies local server Java processes by main class name, which is intentionally narrow but should still be reviewed if launch patterns change
