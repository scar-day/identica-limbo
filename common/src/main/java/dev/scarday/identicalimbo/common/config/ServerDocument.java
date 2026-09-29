package dev.scarday.identicalimbo.common.config;

import dev.scarday.identicalimbo.api.LimboProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ServerDocument {
    private String name = "";
    private Boolean enabled;
    private LimboProviderType type = LimboProviderType.PICOLIMBO;
    private String host = "127.0.0.1";
    private int port = 30066;
}
