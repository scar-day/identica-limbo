package dev.scarday.identicalimbo.velocity.provider.picolimbo;

import dev.scarday.identicalimbo.velocity.platform.VelocityLimboLogger;
import org.slf4j.Logger;

@Deprecated
public class PicoLimboDownloader extends dev.scarday.identicalimbo.provider.picolimbo.PicoLimboDownloader {
    public PicoLimboDownloader(Logger logger) {
        super(new VelocityLimboLogger(logger));
    }
}
