package dev.scarday.identicalimbo.command;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.common.LimboService;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.whereareiam.identica.annotation.Command;
import me.whereareiam.identica.annotation.Definition;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public final class LimboListCommand {
    LimboService limboService;
    LimboCommandMessages messages;

    @Definition("list")
    @Command("identica limbo list")
    public void execute(@NotNull Actor sender) {
        List<VirtualServerDefinition> servers = limboService.servers();
        if (servers.isEmpty()) {
            sender.sendMessage(CommandMessageRenderer.render(messages.getListEmpty(), Map.of()));
            return;
        }
        sender.sendMessage(CommandMessageRenderer.render(messages.getListHeader(), Map.of()));
        servers.forEach(server -> sender.sendMessage(CommandMessageRenderer.render(messages.getListEntry(), Map.of(
                "name", server.name(),
                "provider", server.provider().name().toLowerCase(Locale.ROOT),
                "host", server.host(),
                "port", String.valueOf(server.port())
        ))));
    }
}
