package dev.scarday.identicalimbo.common.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.ConfigDocument;
import me.whereareiam.identica.model.CommandDefinition;

@Getter
@Setter
public final class IdenticaLimboSettings extends ConfigDocument {
    private Map<String, CommandDefinition> commands = new LinkedHashMap<>();
    private LimboCommandMessages messages = new LimboCommandMessages();
}
