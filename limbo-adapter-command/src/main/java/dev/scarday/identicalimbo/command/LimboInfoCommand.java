package dev.scarday.identicalimbo.command;

import dev.scarday.identicalimbo.api.VirtualServerStatus;
import dev.scarday.identicalimbo.command.info.LimboInfoFormatter;
import dev.scarday.identicalimbo.common.LimboService;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.whereareiam.identica.annotation.Argument;
import me.whereareiam.identica.annotation.Command;
import me.whereareiam.identica.annotation.Definition;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public final class LimboInfoCommand {
    LimboService limboService;
    LimboCommandMessages messages;
    LimboInfoFormatter formatter;

    public LimboInfoCommand(LimboService limboService, LimboCommandMessages messages) {
        this(limboService, messages, new LimboInfoFormatter());
    }

    @Definition("info")
    @Command("identica limbo info <name>")
    public void execute(@NotNull Actor sender, @Argument("name") @NotNull String name) {
        Optional<VirtualServerStatus> statusOpt = limboService.status(name);
        if (statusOpt.isEmpty()) {
            sender.sendMessage(CommandMessageRenderer.render(messages.getInfoMissing(), Map.of("name", name)));
            return;
        }

        Map<String, String> placeholders = formatter.formatPlaceholders(statusOpt.get(), messages);
        sender.sendMessage(CommandMessageRenderer.render(messages.getInfoFound(), placeholders));
    }
}
