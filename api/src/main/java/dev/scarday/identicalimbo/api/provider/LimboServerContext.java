package dev.scarday.identicalimbo.api.provider;

import java.nio.file.Path;

public interface LimboServerContext {
    ServerRegistrar registrar();

    Path dataDirectory();

    Object document();

    void logInfo(String message, Object... args);

    void logWarn(String message, Object... args);

    void logError(String message, Throwable throwable);
}
