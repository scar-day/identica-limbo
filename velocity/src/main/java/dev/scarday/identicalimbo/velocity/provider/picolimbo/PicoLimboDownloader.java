package dev.scarday.identicalimbo.velocity.provider.picolimbo;

import dev.scarday.identicalimbo.velocity.platform.VelocityLimboLogger;
import org.slf4j.Logger;

public class PicoLimboDownloader extends dev.scarday.identicalimbo.common.provider.picolimbo.PicoLimboDownloader {
    public PicoLimboDownloader(Logger logger) {
        super(new VelocityLimboLogger(logger));
    }
}
