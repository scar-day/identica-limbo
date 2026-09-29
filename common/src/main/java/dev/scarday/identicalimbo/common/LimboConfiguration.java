package dev.scarday.identicalimbo.common;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import dev.scarday.identicalimbo.common.config.VirtualServerDocument;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import me.whereareiam.identica.model.CommandDefinition;

public record LimboConfiguration(
        List<VirtualServerDefinition> servers,
        Map<String, VirtualServerDocument> serverDocuments,
        Map<String, CommandDefinition> commands,
        LimboCommandMessages messages
) {
    public LimboConfiguration {
        servers = List.copyOf(Objects.requireNonNull(servers, "servers"));
        serverDocuments = Map.copyOf(Objects.requireNonNull(serverDocuments, "serverDocuments"));
        commands = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(commands, "commands")));
        messages = Objects.requireNonNull(messages, "messages");
    }

    public VirtualServerDocument serverDocument(String serverName) {
        if (serverName == null) {
            return null;
        }
        return serverDocuments.get(serverName.toLowerCase(Locale.ROOT));
    }
}
