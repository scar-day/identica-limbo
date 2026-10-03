package dev.scarday.identicalimbo.provider.picolimbo.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboForwarding {
    private String method = "NONE";
    private String secret = "";
}
