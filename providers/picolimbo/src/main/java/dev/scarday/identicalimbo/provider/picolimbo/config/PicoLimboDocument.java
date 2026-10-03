package dev.scarday.identicalimbo.provider.picolimbo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboDocument {
    private PicoLimboPaths paths = new PicoLimboPaths();
    private PicoLimboLifecycle lifecycle = new PicoLimboLifecycle();
    private PicoLimboRelease release = new PicoLimboRelease();
    private PicoLimboWorld world = new PicoLimboWorld();
    private PicoLimboForwarding forwarding = new PicoLimboForwarding();
    private boolean logging = true;

    public void validate() {
        if (paths == null) paths = new PicoLimboPaths();
        if (lifecycle == null) lifecycle = new PicoLimboLifecycle();
        if (release == null) release = new PicoLimboRelease();
        if (world == null) world = new PicoLimboWorld();
        if (forwarding == null) forwarding = new PicoLimboForwarding();
        world.validate();
        if (forwarding.getMethod() == null) {
            throw new IllegalArgumentException("picolimbo.forwarding.method must not be null");
        }
        if (release.getVersion() == null || release.getVersion().isBlank()) {
            throw new IllegalArgumentException("picolimbo.release.version must not be blank");
        }
    }

    public static class Paths extends PicoLimboPaths {}
    public static class Lifecycle extends PicoLimboLifecycle {}
    public static class Release extends PicoLimboRelease {}
    public static class World extends PicoLimboWorld {}
    public static class Spawn extends PicoLimboSpawn {}
    public static class Forwarding extends PicoLimboForwarding {}
}
