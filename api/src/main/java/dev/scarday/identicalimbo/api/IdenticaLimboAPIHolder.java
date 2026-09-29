package dev.scarday.identicalimbo.api;

import java.util.concurrent.atomic.AtomicReference;

final class IdenticaLimboAPIHolder {
    private static final AtomicReference<IdenticaLimboAPI> INSTANCE = new AtomicReference<>();

    private IdenticaLimboAPIHolder() {
    }

    static IdenticaLimboAPI get() {
        IdenticaLimboAPI api = INSTANCE.get();
        if (api == null) {
            throw new IllegalStateException("IdenticaLimboAPI is not initialized yet");
        }
        return api;
    }

    static void set(IdenticaLimboAPI api) {
        INSTANCE.set(api);
    }
}
