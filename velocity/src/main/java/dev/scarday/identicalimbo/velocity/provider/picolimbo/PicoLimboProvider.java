package dev.scarday.identicalimbo.velocity.provider.picolimbo;

import dev.scarday.identicalimbo.velocity.platform.VelocityLimboLogger;
import org.slf4j.Logger;

public class PicoLimboProvider extends dev.scarday.identicalimbo.common.provider.picolimbo.PicoLimboProvider {
    public PicoLimboProvider(Logger logger) {
        super(new VelocityLimboLogger(logger));
    }

    public PicoLimboProvider(Logger logger, PicoLimboDownloader downloader) {
        super(new VelocityLimboLogger(logger), downloader);
    }
}
