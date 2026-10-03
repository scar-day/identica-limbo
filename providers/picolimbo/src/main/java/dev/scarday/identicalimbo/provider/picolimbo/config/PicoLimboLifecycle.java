package dev.scarday.identicalimbo.provider.picolimbo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboLifecycle {
    private boolean autoStart = true;
    private boolean autoDownload = true;
}
