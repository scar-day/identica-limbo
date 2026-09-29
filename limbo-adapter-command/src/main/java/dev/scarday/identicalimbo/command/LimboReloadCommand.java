package dev.scarday.identicalimbo.command;

import dev.scarday.identicalimbo.common.LimboService;
import dev.scarday.identicalimbo.common.config.LimboCommandMessages;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.whereareiam.identica.annotation.Command;
import java.util.Map;
import me.whereareiam.identica.annotation.Definition;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public final class LimboReloadCommand {
    LimboService limboService;
    LimboCommandMessages messages;

    @Definition("reload")
    @Command("identica limbo reload")
    public void execute(@NotNull Actor sender) {
        try {
            limboService.reload();
            sender.sendMessage(CommandMessageRenderer.render(messages.getReloadSuccess(), Map.of()));
        } catch (RuntimeException exception) {
            sender.sendMessage(CommandMessageRenderer.render(messages.getReloadFailure(), Map.of()));
        }
    }
}
