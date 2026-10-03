# Implementing a Custom Limbo Provider

Identica Limbo is designed with a pluggable Service Provider Interface (`LimboProvider`). This allows developers to integrate alternative limbo backends (such as NanoLimbo, custom Netty servers, or dockerized instances).

---

## Table of Contents

- [Core Architecture & Contracts](#core-architecture--contracts)
  - [`LimboProviderType`](#limboprovidertype)
  - [`ManagedLimboServer`](#managedlimboserver)
  - [`LimboProvider`](#limboprovider)
- [Method 1: Internal Subproject (`providers/<name>`)](#method-1-internal-subproject-providersname)
  - [Step 1: Module Setup](#step-1-module-setup)
  - [Step 2: Configuration Mapping](#step-2-configuration-mapping)
  - [Step 3: Server Implementation](#step-3-server-implementation)
  - [Step 4: Provider Implementation](#step-4-provider-implementation)
  - [Step 5: Registration in Proxy Services](#step-5-registration-in-proxy-services)
- [Method 2: External Plugin via Public API](#method-2-external-plugin-via-public-api)
  - [Standalone Provider Implementation](#standalone-provider-implementation)
  - [Registering at Runtime](#registering-at-runtime)
  - [Server YAML Configuration](#server-yaml-configuration)

---

## Core Architecture & Contracts

Creating a provider requires implementing three main abstractions from `:api`:

### `LimboProviderType`
Identifies the type of your provider (e.g. `PICOLIMBO`, `NANOLIMBO`, `CUSTOM`). Can be created using:
```java
LimboProviderType MY_TYPE = LimboProviderType.of("MY_PROVIDER");
```

### `ManagedLimboServer`
Represents an active or stopped limbo server instance. It exposes metadata, PID, memory usage, uptime, and a `close()` method for graceful termination.

### `LimboProvider`
Factory responsible for parsing configuration, preparing binaries or network connections, launching the server, registering it into the proxy's server registrar, and returning a `ManagedLimboServer`.

---

## Method 1: Internal Subproject (`providers/<name>`)

If you are developing a new backend provider directly in the Identica Limbo repository (similar to `providers/picolimbo`):

### Step 1: Module Setup

1. Create a directory `providers/<name>` (e.g. `providers/nanolimbo`).
2. Add `providers/nanolimbo/build.gradle.kts`:
   ```kotlin
   plugins {
       `java-library`
   }

   dependencies {
       compileOnly(project(":api"))
       compileOnly(project(":common"))
       compileOnly(libs.lombok)
       annotationProcessor(libs.lombok)
       compileOnly(libs.jackson.databind)
   }
   ```
3. Include the module in `settings.gradle.kts`:
   ```kotlin
   include(
       ":api",
       ":common",
       ":limbo-adapter-command",
       ":providers:picolimbo",
       ":providers:nanolimbo", // Added
       ":velocity",
       ":bungeecord"
   )
   ```

### Step 2: Configuration Mapping

Create a document class for your provider's custom section:

```java
package dev.scarday.identicalimbo.provider.nanolimbo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NanoLimboDocument {
    private String jarPath = "nanolimbo.jar";
    private int maxPlayers = 100;
}
```

Register its default representation in `VirtualServerDefaults`:

```java
static {
    VirtualServerDefaults.registerContributor(doc -> {
        if (doc.getExtensions() != null) {
            doc.getExtensions().putIfAbsent("nanolimbo", new NanoLimboDocument());
        }
    });
}
```

### Step 3: Server Implementation

Implement `ManagedLimboServer`:

```java
package dev.scarday.identicalimbo.provider.nanolimbo;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import java.time.Instant;
import java.util.Optional;

public class NanoLimboServer implements ManagedLimboServer {
    private final VirtualServerDefinition definition;
    private Process process;
    private final Instant startedAt;

    public NanoLimboServer(VirtualServerDefinition definition, Process process, Instant startedAt) {
        this.definition = definition;
        this.process = process;
        this.startedAt = startedAt;
    }

    @Override
    public VirtualServerDefinition definition() {
        return definition;
    }

    @Override
    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    @Override
    public Optional<Long> pid() {
        return isRunning() ? Optional.of(process.pid()) : Optional.empty();
    }

    @Override
    public long memoryBytes() {
        return -1L;
    }

    @Override
    public Instant startedAt() {
        return startedAt;
    }

    @Override
    public String dimension() {
        return "overworld";
    }

    @Override
    public String gameMode() {
        return "spectator";
    }

    @Override
    public String schematic() {
        return null;
    }

    @Override
    public void close() {
        if (process != null) {
            process.destroyForcibly();
            process = null;
        }
    }
}
```

### Step 4: Provider Implementation

Implement `LimboProvider`:

```java
package dev.scarday.identicalimbo.provider.nanolimbo;

import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.LimboProvider;
import dev.scarday.identicalimbo.api.provider.LimboServerContext;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import dev.scarday.identicalimbo.common.config.VirtualServerDefaults;
import dev.scarday.identicalimbo.common.config.VirtualServerDocument;
import java.time.Instant;

public class NanoLimboProvider implements LimboProvider {
    public static final LimboProviderType TYPE = LimboProviderType.of("NANOLIMBO");

    static {
        VirtualServerDefaults.registerContributor(doc -> {
            if (doc.getExtensions() != null) {
                doc.getExtensions().putIfAbsent("nanolimbo", new NanoLimboDocument());
            }
        });
    }

    @Override
    public LimboProviderType type() {
        return TYPE;
    }

    @Override
    public ManagedLimboServer createServer(VirtualServerDefinition definition, LimboServerContext context) throws Exception {
        VirtualServerDocument doc = (VirtualServerDocument) context.document();
        Object rawConfig = doc.getExtensions().get("nanolimbo");

        // 1. Start process
        Process process = new ProcessBuilder("java", "-jar", "nanolimbo.jar").start();
        Instant startedAt = Instant.now();

        // 2. Register server with proxy platform
        context.registrar().register(definition);
        context.logInfo("Started NanoLimbo server {}", definition.name());

        return new NanoLimboServer(definition, process, startedAt);
    }
}
```

### Step 5: Registration in Proxy Services

1. Add dependency to `velocity/build.gradle.kts` and `bungeecord/build.gradle.kts`:
   ```kotlin
   implementation(project(":providers:nanolimbo"))
   ```
2. In `VelocityLimboService` and `BungeeCordLimboService`:
   ```java
   providerRegistry.register(new NanoLimboProvider());
   ```

---

## Method 2: External Plugin via Public API

Any external proxy plugin can register providers dynamically at runtime via `IdenticaLimboAPI`.

### Standalone Provider Implementation

```java
import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.LimboProvider;
import dev.scarday.identicalimbo.api.provider.LimboServerContext;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;

public class DockerLimboProvider implements LimboProvider {
    public static final LimboProviderType TYPE = LimboProviderType.of("DOCKER");

    @Override
    public LimboProviderType type() {
        return TYPE;
    }

    @Override
    public ManagedLimboServer createServer(VirtualServerDefinition definition, LimboServerContext context) {
        // Spin up Docker container or connect to remote endpoint
        context.registrar().register(definition);
        return new DockerLimboServer(definition);
    }
}
```

### Registering at Runtime

```java
import dev.scarday.identicalimbo.api.IdenticaLimboAPI;

public class MyPlugin extends Plugin {
    @Override
    public void onEnable() {
        IdenticaLimboAPI.get().providers().register(new DockerLimboProvider());
    }
}
```

### Server YAML Configuration

In `plugins/identica-limbo/limbo/docker-auth.yml`:

```yaml
server:
  name: "docker-auth"
  enabled: true
  type: "DOCKER"
  host: "10.0.0.15"
  port: 25565
```
