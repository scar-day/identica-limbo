package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboStatusMessages {
    private String active = "<green>Активен</green> <gray>(in-process)</gray>";
    private String running = "<green>Работает</green>";
    private String runningWithPid = "<green>Работает</green> <gray>(PID: {pid})</gray>";
    private String stopped = "<red>Остановлен</red>";
    private String rawActive = "ACTIVE";
    private String rawRunning = "RUNNING";
    private String rawStopped = "STOPPED";

    public void copyFrom(LimboStatusMessages source) {
        if (source == null) return;
        if (source.active != null) this.active = source.active;
        if (source.running != null) this.running = source.running;
        if (source.runningWithPid != null) this.runningWithPid = source.runningWithPid;
        if (source.stopped != null) this.stopped = source.stopped;
        if (source.rawActive != null) this.rawActive = source.rawActive;
        if (source.rawRunning != null) this.rawRunning = source.rawRunning;
        if (source.rawStopped != null) this.rawStopped = source.rawStopped;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (active == null) { active = "<green>Активен</green> <gray>(in-process)</gray>"; changed = true; }
        if (running == null) { running = "<green>Работает</green>"; changed = true; }
        if (runningWithPid == null) { runningWithPid = "<green>Работает</green> <gray>(PID: {pid})</gray>"; changed = true; }
        if (stopped == null) { stopped = "<red>Остановлен</red>"; changed = true; }
        if (rawActive == null) { rawActive = "ACTIVE"; changed = true; }
        if (rawRunning == null) { rawRunning = "RUNNING"; changed = true; }
        if (rawStopped == null) { rawStopped = "STOPPED"; changed = true; }
        return changed;
    }
}
