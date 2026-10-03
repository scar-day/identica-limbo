package dev.scarday.identicalimbo.bungeecord.platform;

import dev.scarday.identicalimbo.common.logging.LimboLogger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BungeeCordLimboLogger implements LimboLogger {
    private final Logger logger;

    public BungeeCordLimboLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message, Object... args) {
        if (logger != null) {
            logger.info(LimboLogger.format(message, args));
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (logger != null) {
            logger.warning(LimboLogger.format(message, args));
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (logger != null) {
            logger.log(Level.SEVERE, message, throwable);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (logger != null) {
            logger.fine(LimboLogger.format(message, args));
        }
    }
}
