package dev.scarday.identicalimbo.command;

import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
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
public final class LimboRootCommand {
    LimboCommandMessages messages;

    @Definition("limbo")
    @Command("identica limbo")
    public void execute(@NotNull Actor sender) {
        sender.sendMessage(CommandMessageRenderer.render(messages.getUsage(), Map.of()));
    }
}
