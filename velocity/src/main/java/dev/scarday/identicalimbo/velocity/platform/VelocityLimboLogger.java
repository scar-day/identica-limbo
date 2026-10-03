package dev.scarday.identicalimbo.velocity.platform;

import dev.scarday.identicalimbo.common.logging.LimboLogger;
import org.slf4j.Logger;

public final class VelocityLimboLogger implements LimboLogger {
    private final Logger logger;

    public VelocityLimboLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message, Object... args) {
        if (logger != null) {
            logger.info(message, args);
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (logger != null) {
            logger.warn(message, args);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (logger != null) {
            logger.error(message, throwable);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (logger != null) {
            logger.debug(message, args);
        }
    }
}
