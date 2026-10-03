# Configuration Guide

This guide covers all configuration files used by Identica Limbo: global plugin settings, command messages, and virtual server definitions.

---

## Table of Contents

- [Directory Structure](#directory-structure)
- [Global Configuration (`config.yml`)](#global-configuration-configyml)
  - [Command Settings](#command-settings)
  - [Messages & MiniMessage Localization](#messages--minimessage-localization)
- [Server Configurations (`limbo/*.yml`)](#server-configurations-limboyml)
  - [Base `server` Block](#base-server-block)
  - [`picolimbo` Provider Block](#picolimbo-provider-block)
- [Typed Enums](#typed-enums)
- [Coordinate & Rotation Formats](#coordinate--rotation-formats)

---

## Directory Structure

```text
plugins/identica-limbo/
├── config.yml                      # Global settings, command registration & messages
└── limbo/                          # Virtual Limbo server definitions
    ├── auth-1.yml                  # First virtual server
    ├── auth-2.yml                  # Second virtual server
    └── fallback.yml                # Maintenance/fallback server
```

Every `.yml` file placed inside the `limbo/` folder is parsed as a separate virtual server definition.

---

## Global Configuration (`config.yml`)

### Command Settings

You can customize aliases, descriptions, and required permissions for each subcommand:

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
    description: "Show detailed status of a limbo server."
    usage: "{command} {alias} <name>"
    arguments:
      name: "Limbo server name"
  reload:
    enabled: true
    aliases: ["limbo reload"]
    permission: "identica.admin.limbo"
    description: "Reload limbo configuration."
    usage: "{command} {alias}"
```

### Messages & MiniMessage Localization

Messages support [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting tags:

```yaml
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

## Server Configurations (`limbo/*.yml`)

Each server file consists of a core `server` section and provider-specific sections (e.g. `picolimbo`).

### Base `server` Block

| Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `name` | String | `""` (filename) | Server identifier registered in the proxy. |
| `enabled` | Boolean | `true` | When `false`, the server is ignored and not registered. |
| `type` | String | `"PICOLIMBO"` | Provider engine type (`PICOLIMBO` or custom registered type). |
| `host` | String | `"127.0.0.1"` | IP address to bind and register. |
| `port` | Integer | `30066` | Port number to bind. Must be unique per virtual server. |

### `picolimbo` Provider Block

```yaml
server:
  name: "auth-1"
  enabled: true
  type: "PICOLIMBO"
  host: "127.0.0.1"
  port: 30066

picolimbo:
  paths:
    # Directory to store downloaded release archives (empty = workingDirectory)
    download: ""
    # Path to pre-existing binary (empty = auto-resolved in installation directory)
    executable: ""
    # Working directory for runtime files (empty = OS temp dir)
    workingDirectory: ""
  lifecycle:
    # Automatically start process when proxy starts
    autoStart: true
    # Automatically download binary and schematic from GitHub if missing
    autoDownload: true
  release:
    # GitHub release tag (e.g. "latest", "v1.1.0")
    version: "latest"
  world:
    # Path to custom WorldEdit .schem file (empty = downloads default spawn.schem)
    schematic: ""
    # Chat message sent to the player on join
    welcomeMessage: ""
    # Action bar text displayed to the player
    actionBar: ""
    # Game mode: survival, creative, adventure, spectator
    gameMode: "spectator"
    # World dimension: overworld, nether, end
    dimension: "overworld"
    spawn:
      position: "20.5;17.0;22.5"
      rotation: "-90.0;0.0"
    # Render view distance in chunks (1 to 32)
    viewDistance: 2
    # Lock world time to freeze day/night cycle
    lockTime: false
  forwarding:
    # Method: NONE, MODERN, BUNGEE_GUARD
    method: "MODERN"
    # Secret key (Velocity modern forwarding secret or BungeeGuard token)
    secret: ""
  # Redirect backend stdout and stderr to proxy console
  logging: true
```

---

## Typed Enums

Identica Limbo validates configuration properties using strictly typed enums:

### Game Mode (`gameMode`)
- `spectator` (Default)
- `survival`
- `creative`
- `adventure`

### Dimension (`dimension`)
- `overworld` (Default)
- `nether`
- `end`

### Forwarding Method (`method` / `type`)
- `NONE` (Default) — No player info forwarding.
- `MODERN` — Velocity modern player info forwarding using a shared secret.
- `BUNGEE_GUARD` — BungeeCord / Waterfall token-based forwarding.

---

## Coordinate & Rotation Formats

Coordinates are configured using semicolon-delimited strings:

- **Position**: `"X;Y;Z"` (e.g. `"20.5; 17.0; 22.5"`)
- **Rotation**: `"Yaw;Pitch"` (e.g. `"-90.0; 0.0"`)

> [!NOTE]
> Whitespace around the delimiter is automatically trimmed. The legacy YAML list syntax (`position: [20.5, 17.0, 22.5]`) remains fully supported for backward compatibility.
