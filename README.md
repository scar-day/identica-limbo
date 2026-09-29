# Identica Limbo

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Velocity](https://img.shields.io/badge/Velocity-3.5.0--SNAPSHOT-00dc82.svg)](https://papermc.io/software/velocity)
[![Identica](https://img.shields.io/badge/Identica-Addon-blue.svg)](https://github.com/whereareiam)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A high-performance **Velocity** proxy addon for **Identica** that dynamically manages virtual Limbo servers. It isolates unauthenticated or pending players within lightweight Limbo instances while minimizing resource overhead.

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Directory Layout](#directory-layout)
- [Commands & Permissions](#commands--permissions)
- [Configuration](#configuration)
  - [Global Configuration (`config.yml`)](#global-configuration-configyml)
  - [Server Configuration (`limbo/*.yml`)](#server-configuration-limboyml)
- [Developer API (`IdenticaLimboAPI`)](#developer-api-identicalimboapi)
  - [Player Transfers](#player-transfers)
  - [Status & Inspection](#status--inspection)
- [Extending Providers (`LimboProvider` SPI)](#extending-providers-limboprovider-spi)
- [Building from Source](#building-from-source)

---

## Features

- **Embedded Managed Backend**: Built-in support for **PicoLimbo** (`PICOLIMBO`) with automated binary download, life-cycle management, and clean shutdown.
- **Provider SPI (`LimboProvider`)**: Fully modular backend SPI. Easily plug in alternative Limbo engines (such as NanoLimbo, custom TCP proxies, or remote containerized services) without touching core logic.
- **Identica Command Integration**: Deeply integrated into Identica's command dispatch tree under `/identica limbo`.
- **Public API (`IdenticaLimboAPI`)**: Non-blocking asynchronous API for player routing, bulk transfers during maintenance/reboots, and live telemetry.
- **Semicolon Coordinate Format**: Clean, human-readable spawn positions and rotations (`"X;Y;Z"` and `"Yaw;Pitch"`).
- **Zero Runtime Reflection Overhead**: Native Velocity and Adventure MiniMessage support with full JVM metric reporting.

---

## Requirements

- **Proxy**: [Velocity 3.4.0+](https://papermc.io/software/velocity)
- **Runtime**: **Java 21** or newer
- **Core Dependency**: **Identica** plugin must be installed and enabled on the proxy
- **Network**: Outgoing HTTPS access to GitHub (required only if `autoDownload: true` is enabled for PicoLimbo)

> [!NOTE]
> Identica Limbo is designed primarily for Velocity. The `bungeecord` module is currently a stub placeholder and does not run standalone.

---

## Installation

1. Build or download the latest artifact: `build/IdenticaLimbo-VELOCITY-1.0.0.jar`.
2. Drop the JAR file into your proxy's `plugins/` directory alongside `Identica`.
3. Start the proxy once to generate initial configurations, then stop it.
4. Customize `plugins/identica-limbo/config.yml` and server definitions in `plugins/identica-limbo/limbo/`.
5. Start your proxy.

---

## Directory Layout

```text
plugins/identica-limbo/
├── config.yml                      # Main configuration and message localization
└── limbo/
    └── server1-picolimbo.yml       # Virtual Limbo server definition
```

Each `.yml` file located in the `limbo/` directory represents a separate virtual server registered in Velocity. You can define as many virtual servers as needed (e.g., `auth-1.yml`, `auth-2.yml`, `maintenance.yml`).

---

## Commands & Permissions

All commands are registered as subcommands under the root `/identica` command.

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/identica limbo` | `identica.admin.limbo` | Displays plugin command usage and help. |
| `/identica limbo list` | `identica.admin.limbo` | Lists all registered virtual Limbo servers. |
| `/identica limbo info <name>` | `identica.admin.limbo` | Displays detailed status, memory, uptime, and players. |
| `/identica limbo reload` | `identica.admin.limbo` | Hot-reloads configuration and restarts managed processes. |

---

## Configuration

### Global Configuration (`config.yml`)

The primary configuration file manages command registrations and MiniMessage-formatted responses.

```yaml
commands:
  limbo:
    enabled: true
    aliases: ["limbo"]
    permission: "identica.admin.limbo"
    description: "Manage Identica Limbo servers."
    usage: "{command} {alias}"
  list:
    enabled: true
    aliases: ["limbo list"]
    permission: "identica.admin.limbo"
    description: "List registered limbo servers."
    usage: "{command} {alias}"
  info:
    enabled: true
    aliases: ["limbo info"]
    permission: "identica.admin.limbo"
    description: "Show a limbo server."
    usage: "{command} {alias} <name>"
    arguments:
      name: "Limbo server name"
  reload:
    enabled: true
    aliases: ["limbo reload"]
    permission: "identica.admin.limbo"
    description: "Reload limbo configuration."
    usage: "{command} {alias}"

messages:
  usage: "<white>Usage: /identica limbo list, /identica limbo info <name>, /identica limbo reload"
  listEmpty: "<yellow>No virtual servers registered."
  listHeader: "<green>Virtual servers:"
  listEntry: "<gray>- <name> [<provider>] <host>:<port>"
  infoMissing: "<red>Server not found: <name>"
  infoFound: |
    <gray>Server Information for <gold><name></gold>:</gray>
    <gray>• Provider: <white><provider></white></gray>
    <gray>• Status: <status></gray>
    <gray>• Address: <white><address></white></gray>
    <gray>• Players: <white><players></white></gray>
    <gray>• Memory: <white><memory></white></gray>
    <gray>• Uptime: <white><uptime></white></gray>
    <gray>• Details: <white><details></white></gray>
  reloadSuccess: "<green>Limbo configuration successfully reloaded."
  reloadFailure: "<red>Failed to reload Limbo configuration."
  status:
    active: "<green>Active</green> <gray>(in-process)</gray>"
    running: "<green>Running</green>"
    runningWithPid: "<green>Running</green> <gray>(PID: {pid})</gray>"
    stopped: "<red>Stopped</red>"
    rawActive: "ACTIVE"
    rawRunning: "RUNNING"
    rawStopped: "STOPPED"
  address:
    virtual: "Virtual (in-proxy)"
  players:
    empty: "0"
    listEmpty: "none"
    withNames: "{count} <gray>({names})</gray>"
  details:
    dimension: "World: {dimension}"
    gameMode: "Mode: {gamemode}"
    schematic: "Schematic: {schematic}"
    separator: " | "
    none: "—"
  duration:
    days: "d "
    hours: "h "
    minutes: "m "
    seconds: "s"
    zero: "0s"
  memory:
    bytes: "{value} B"
    kilobytes: "{value} KB"
    megabytes: "{value} MB"
    gigabytes: "{value} GB"
    jvm: "{used} / {max} (JVM)"
    notAvailable: "N/A"
```

---

### Server Configuration (`limbo/*.yml`)

Example definition for a PicoLimbo backend (`server1-picolimbo.yml`):

```yaml
server:
  name: "auth-1"
  enabled: true
  type: "PICOLIMBO"
  host: "127.0.0.1"
  port: 30066

picolimbo:
  paths:
    download: ""
    executable: ""
    workingDirectory: ""
  lifecycle:
    autoStart: true
    autoDownload: true
  release:
    version: "latest"
  world:
    schematic: ""
    welcomeMessage: ""
    actionBar: ""
    gameMode: "spectator"
    dimension: "overworld"
    spawn:
      position: "20.5;17.0;22.5"
      rotation: "-90.0;0.0"
    viewDistance: 2
    lockTime: false
  forwarding:
    method: "MODERN"
    secret: ""
  logging: true
```

#### Coordinate Format
Spawn coordinates use a semicolon-delimited string format (`X;Y;Z` and `Yaw;Pitch`):
- `position: "20.5;17.0;22.5"`
- `rotation: "-90.0;0.0"`

> [!TIP]
> Whitespace around the delimiter is automatically trimmed (e.g., `"20.5; 17.0; 22.5"` is fully valid). Legacy YAML array syntax `[20.5, 17.0, 22.5]` remains backwards-compatible.

---

## Developer API (`IdenticaLimboAPI`)

Access the global singleton through `IdenticaLimboAPI.get()`.

### Player Transfers

```java
import dev.scarday.identicalimbo.api.IdenticaLimboAPI;

IdenticaLimboAPI api = IdenticaLimboAPI.get();

// Transfer a player by UUID
api.transfer(playerUuid, "auth-1").thenAccept(success -> {
    if (success) {
        // Player successfully connected to auth-1
    }
});

// Transfer a player by username
api.transfer("PlayerName", "auth-1");

// Evacuate all players across the proxy into Limbo (e.g., during network maintenance)
api.transferAll("auth-1").thenAccept(evacuatedUuids -> {
    System.out.println("Evacuated " + evacuatedUuids.size() + " players to Limbo.");
});

// Evacuate players from a specific backend server (e.g., when restarting a hub)
api.transferAll("hub-1", "auth-1");
```

### Status & Inspection

```java
// Check if a registered Velocity server is a virtual Limbo server
boolean isLimbo = api.isLimbo("auth-1");

// Query online counts
int authOnline = api.getOnlineCount("auth-1");
int totalLimboOnline = api.getTotalOnlineCount();

// List player names currently on a Limbo server
List<String> players = api.getConnectedPlayers("auth-1");

// Retrieve full server telemetry (PID, memory, uptime, world details)
api.status("auth-1").ifPresent(status -> {
    System.out.println("Running: " + status.running());
    System.out.println("PID: " + status.pid());
    System.out.println("Memory: " + status.memoryBytes() + " bytes");
});
```

---

## Extending Providers (`LimboProvider` SPI)

You can introduce new Limbo backends (e.g., NanoLimbo, custom TCP forwarders, Docker containers) without modifying core plugin code.

### 1. Implement `ManagedLimboServer`

```java
public class CustomLimboServer implements ManagedLimboServer {
    private final VirtualServerDefinition definition;

    public CustomLimboServer(VirtualServerDefinition definition) {
        this.definition = definition;
    }

    @Override
    public VirtualServerDefinition definition() {
        return definition;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public Optional<Long> pid() {
        return Optional.empty();
    }

    @Override
    public long memoryBytes() {
        return -1L;
    }

    @Override
    public Instant startedAt() {
        return Instant.now();
    }

    @Override
    public String dimension() { return "overworld"; }

    @Override
    public String gameMode() { return "spectator"; }

    @Override
    public String schematic() { return null; }

    @Override
    public void close() {
        // Cleanup resources
    }
}
```

### 2. Implement `LimboProvider`

```java
public class CustomLimboProvider implements LimboProvider {
    @Override
    public LimboProviderType type() {
        return LimboProviderType.of("CUSTOM_LIMBO");
    }

    @Override
    public ManagedLimboServer createServer(VirtualServerDefinition definition, LimboServerContext context) {
        return new CustomLimboServer(definition);
    }
}
```

### 3. Register the Provider

```java
IdenticaLimboAPI.get().providers().register(new CustomLimboProvider());
```

After registration, any file in `limbo/*.yml` can target this provider:
```yaml
server:
  name: "custom-1"
  type: "CUSTOM_LIMBO"
  port: 30070
```

---

## Building from Source

### Prerequisites
- JDK 21+
- Git

### Build Command

```bash
./gradlew clean check build
```

Compiled artifacts are placed directly into the root `build/` directory:

```text
build/
├── IdenticaLimbo-VELOCITY-1.0.0.jar
└── IdenticaLimbo-BUNGEECORD-1.0.0.jar
```
