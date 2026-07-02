# Bunker

Bunker is a backend for a multiplayer "Bunker" party game (players build a character, hide/reveal traits, use artifacts, and vote to expel opponents from a shelter). The project is a **learning project**, built primarily to practice backend development, Redis, and WebSocket communication. The idea for the game itself came up spontaneously.

## Project Status

- No frontend yet — the backend is REST/WebSocket only, meant to be consumed by a client that doesn't exist in this repository.
- Actively developed; minor bugs are being fixed as they're discovered.
- Basic application-wide logging is implemented (see [Logging](#logging)).

## Tech Stack

- **Java 21**, **Spring Boot 4**
- **Spring Security** + **JWT** (`jjwt`) for authentication
- **Spring Data JPA** + **PostgreSQL** for persistent storage
- **Spring Data Redis** for caching
- **Spring WebSocket** (STOMP) for real-time game events
- **Docker Compose** for local Postgres and Redis instances
- **Lombok**

## Architecture Overview

- `controller/` — REST controllers (auth, rooms, players, characteristics, artifacts) and a WebSocket controller (`ArtifactWebController`) for real-time characteristic updates.
- `service/` — business logic (auth, game flow, room/player management, sessions).
- `model/` — JPA entities (`Player`, `Room`, `RoomPlayer`, `Hero`, `Characteristic` and its subtypes, `Effect`, `User`, etc.).
- `repository/` — Spring Data JPA repositories.
- `dto/` — request/response payloads grouped by domain.
- `config/` — Security, JWT filter, Redis, and WebSocket configuration.

### WebSocket

STOMP endpoint is registered at `/ws`, with a simple broker on `/topic` and application prefix `/app`. Used for pushing live game/characteristic updates to connected players.

### Caching

Redis is used as a Spring Cache backend (`RedisCacheManager`), with cached entries stored as JSON and a default TTL of 1 day.

## REST API (overview)

| Base path | Purpose |
|---|---|
| `/api/auth` | Registration and login (`/register`, `/login`) |
| `/api/room` | Room creation and listing (`/create`, `/all_rooms`, `/continueToGame/{roomId}`) |
| `/api/roomPlayer` | In-room player actions (`/continue_game`, `/left_game/{roomId}`, `/expulsion/{roomId}`, `/results/{roomId}`, `/next_move/{roomId}`) |
| `/api/player` | Player data (`/hero/{roomId}`, `/randomArtifacts/{roomId}`, `/heroArtifacts/{roomId}`, `/artifacts`) |
| `/api/characteristic` | Player characteristics (`/characteristic/{roomId}`) |
| `/api/randomArtifact/using` | Artifact effects (`/purification`, `/protection`, `/espionage`, `/stun`, `/stealing`, `/curse`) |

Note: this table lists endpoint paths as declared in the controllers, not full request/response contracts.

## Database

Data is persisted in PostgreSQL. Core entities include `User`, `Room`, `RoomPlayer`, `Player`, `Hero`, `Characteristic` (with subtypes such as `Figure`, `Hobby`, `Secret`, `StateOfHealth`, `PhysicalCondition`, `PsychologicalState`), `Effect`/`Effects`, and artifact catalogs (`ArtifactHeroCatalog`, `ArtifactRandomCatalog`).
A full schema diagram

<img width="1727" height="1664" alt="БункерПроект (1)" src="https://github.com/user-attachments/assets/9d67d4c3-f141-408a-9705-4c3da175675d" />


## Logging

The application uses Logback with a rolling file appender:
- Logs are written to `logs/app.log`.
- Daily rotation to `logs/app-yyyy-MM-dd.log`, keeping up to 30 days of history.
- Minimum logged level: `INFO`.

## Running Locally

### Prerequisites

- JDK 21
- Docker (for Postgres and Redis)


- No frontend/client is provided.
- Some features may contain bugs; issues are fixed incrementally as they're found.

## License

This is a learning project without a formal license. Feel free to explore the code, but it is not intended for production use.
