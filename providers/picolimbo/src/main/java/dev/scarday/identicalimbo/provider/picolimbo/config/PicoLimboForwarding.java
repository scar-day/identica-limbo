package dev.scarday.identicalimbo.provider.picolimbo.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import dev.scarday.identicalimbo.provider.picolimbo.config.enums.Forwarding;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PicoLimboForwarding {
    @JsonAlias("type")
    private Forwarding method = Forwarding.NONE;
    private String secret = "";
}
