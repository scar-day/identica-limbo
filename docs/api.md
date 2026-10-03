# Developer API (`IdenticaLimboAPI`)

Identica Limbo provides a non-blocking, asynchronous public Java API for programmatically managing virtual Limbo servers, routing players, and querying live telemetry.

---

## Table of Contents

- [Dependency Setup](#dependency-setup)
- [Obtaining the API Instance](#obtaining-the-api-instance)
- [Player Transfers & Evacuation](#player-transfers--evacuation)
- [Status & Telemetry Queries](#status--telemetry-queries)
- [Provider Registry](#provider-registry)
- [Programmatic Reload](#programmatic-reload)

---

## Dependency Setup

Add the API dependency to your project:

### Gradle (`build.gradle.kts`)
```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("dev.scarday:identicalimbo-api:1.0.0")
}
```

### Maven (`pom.xml`)
```xml
<dependency>
    <groupId>dev.scarday</groupId>
    <artifactId>identicalimbo-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

## Obtaining the API Instance

Access the global singleton via `IdenticaLimboAPI.get()`:

```java
import dev.scarday.identicalimbo.api.IdenticaLimboAPI;

IdenticaLimboAPI api = IdenticaLimboAPI.get();
```

---

## Player Transfers & Evacuation

All transfer operations execute asynchronously and return `CompletableFuture` objects:

### 1. Transfer Player by UUID
```java
UUID uuid = player.getUniqueId();

api.transfer(uuid, "auth-1").thenAccept(success -> {
    if (success) {
        System.out.println("Player was successfully connected to auth-1");
    } else {
        System.out.println("Failed to transfer player to auth-1");
    }
});
```

### 2. Transfer Player by Username
```java
api.transfer("PlayerName", "auth-1");
```

### 3. Evacuate All Players Proxy-Wide to Limbo
Ideal for proxy reboots, network maintenance, or backend downtimes:
```java
api.transferAll("auth-1").thenAccept(evacuatedUuids -> {
    System.out.println("Evacuated " + evacuatedUuids.size() + " players to auth-1.");
});
```

### 4. Evacuate Players from a Specific Server
Ideal when restarting an individual game or lobby server:
```java
api.transferAll("hub-1", "auth-1").thenAccept(evacuatedUuids -> {
    System.out.println("Evacuated " + evacuatedUuids.size() + " players from hub-1 to auth-1.");
});
```

---

## Status & Telemetry Queries

### Check if Server is a Limbo Server
```java
boolean isLimbo = api.isLimbo("auth-1");
```

### Query Player Counts
```java
// Total players connected to a specific limbo instance
int online = api.getOnlineCount("auth-1");

// Total players across all virtual limbo servers combined
int total = api.getTotalOnlineCount();

// List player usernames on a limbo server
List<String> usernames = api.getConnectedPlayers("auth-1");
```

### Live Server Telemetry
Retrieve detailed status information about a managed or registered limbo server:

```java
api.status("auth-1").ifPresent(status -> {
    System.out.println("Name: " + status.name());
    System.out.println("Provider: " + status.provider());
    System.out.println("Running: " + status.running());
    System.out.println("Process PID: " + status.pid().orElse(null));
    System.out.println("Memory RSS: " + status.memoryBytes() + " bytes");
    System.out.println("Started At: " + status.startedAt());
    System.out.println("Dimension: " + status.dimension());
    System.out.println("Game Mode: " + status.gameMode());
    System.out.println("Schematic: " + status.schematic());
});
```

---

## Provider Registry

Access the runtime provider registry to inspect, register, or unregister providers dynamically:

```java
import dev.scarday.identicalimbo.api.provider.LimboProviderRegistry;
import dev.scarday.identicalimbo.api.LimboProviderType;

LimboProviderRegistry registry = api.providers();

// Check if a provider type is registered
boolean registered = registry.find(LimboProviderType.PICOLIMBO).isPresent();

// Unregister a provider type
registry.unregister(LimboProviderType.of("CUSTOM_LIMBO"));
```

---

## Programmatic Reload

Reload all configuration files and synchronize managed backend processes:

```java
api.reload();
```
