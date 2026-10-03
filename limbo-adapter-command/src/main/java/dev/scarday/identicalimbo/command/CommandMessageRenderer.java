package dev.scarday.identicalimbo.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class CommandMessageRenderer {
    private CommandMessageRenderer() {
    }

    static Component render(String message, Map<String, String> placeholders) {
        if (message == null || message.isBlank()) return Component.empty();
        if (placeholders == null || placeholders.isEmpty()) return MiniMessage.miniMessage().deserialize(message);

        List<TagResolver> resolvers = new ArrayList<>(placeholders.size());
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            resolvers.add(Placeholder.parsed(entry.getKey(), value));
        }
        return MiniMessage.miniMessage().deserialize(message, TagResolver.resolver(resolvers));
    }
}
