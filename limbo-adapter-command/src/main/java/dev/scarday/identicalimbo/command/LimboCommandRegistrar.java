package dev.scarday.identicalimbo.command;

import dev.scarday.identicalimbo.common.LimboService;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.whereareiam.identica.command.CommandService;
import me.whereareiam.identica.model.CommandDefinition;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@AllArgsConstructor
public final class LimboCommandRegistrar {
    CommandService commandService;
    LimboService limboService;
    Map<String, CommandDefinition> commandDefinitions;
    LimboCommandMessages messages;

    public void register() {
        commandService.registerCommandInstances(
                commandDefinitions,
                new LimboRootCommand(messages),
                new LimboListCommand(limboService, messages),
                new LimboInfoCommand(limboService, messages),
                new LimboReloadCommand(limboService, messages)
        );
    }
}
