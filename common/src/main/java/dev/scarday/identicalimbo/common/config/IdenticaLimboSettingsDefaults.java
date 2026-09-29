package dev.scarday.identicalimbo.common.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.whereareiam.configura.merge.defaults.DefaultsProvider;
import me.whereareiam.identica.model.CommandDefinition;

public final class IdenticaLimboSettingsDefaults implements DefaultsProvider<IdenticaLimboSettings> {
    private static final String CMD_LIMBO = "limbo";
    private static final String CMD_LIST = "list";
    private static final String CMD_INFO = "info";
    private static final String CMD_RELOAD = "reload";
    private static final String ALIAS_LIMBO = "limbo";
    private static final String ALIAS_LIST = "limbo list";
    private static final String ALIAS_INFO = "limbo info";
    private static final String ALIAS_RELOAD = "limbo reload";
    private static final String PERMISSION_ADMIN = "identica.admin.limbo";
    private static final String DESC_LIMBO = "Manage Identica Limbo servers.";
    private static final String DESC_LIST = "List registered limbo servers.";
    private static final String DESC_INFO = "Show a limbo server.";
    private static final String DESC_RELOAD = "Reload limbo configuration.";
    private static final String USAGE_DEFAULT = "{command} {alias}";
    private static final String USAGE_INFO = "{command} {alias} <name>";
    private static final String ARG_NAME = "name";
    private static final String ARG_NAME_DESC = "Limbo server name";

    @Override
    public IdenticaLimboSettings supply(IdenticaLimboSettings settings) {
        Map<String, CommandDefinition> commands = new LinkedHashMap<>();
        commands.put(CMD_LIMBO, command(List.of(ALIAS_LIMBO), DESC_LIMBO, USAGE_DEFAULT));
        commands.put(CMD_LIST, command(List.of(ALIAS_LIST), DESC_LIST, USAGE_DEFAULT));
        commands.put(CMD_INFO, command(
                List.of(ALIAS_INFO),
                DESC_INFO,
                USAGE_INFO,
                Map.of(ARG_NAME, ARG_NAME_DESC)
        ));
        commands.put(CMD_RELOAD, command(List.of(ALIAS_RELOAD), DESC_RELOAD, USAGE_DEFAULT));
        settings.setCommands(commands);

        LimboCommandMessages messages = new LimboCommandMessages();
        populateDefaults(messages);
        settings.setMessages(messages);
        return settings;
    }

    public static boolean populateDefaults(LimboCommandMessages messages) {
        boolean changed = false;
        if (messages.getUsage() == null) {
            messages.setUsage("<white>Использование: /identica limbo list, /identica limbo info <name>, /identica limbo reload");
            changed = true;
        }
        if (messages.getListEmpty() == null) {
            messages.setListEmpty("<yellow>Виртуальные серверы не зарегистрированы.");
            changed = true;
        }
        if (messages.getListHeader() == null) {
            messages.setListHeader("<green>Виртуальные серверы:");
            changed = true;
        }
        if (messages.getListEntry() == null) {
            messages.setListEntry("<gray>- <name> [<provider>] <host>:<port>");
            changed = true;
        }
        if (messages.getInfoMissing() == null) {
            messages.setInfoMissing("<red>Сервер не найден: <name>");
            changed = true;
        }
        if (messages.getInfoFound() == null) {
            messages.setInfoFound(LimboConfigurationLoader.DEFAULT_INFO_FOUND);
            changed = true;
        }
        if (messages.getReloadSuccess() == null) {
            messages.setReloadSuccess("<green>Конфигурация limbo перезагружена.");
            changed = true;
        }
        if (messages.getReloadFailure() == null) {
            messages.setReloadFailure("<red>Не удалось перезагрузить конфигурацию limbo.");
            changed = true;
        }
        if (messages.getStatus() == null) {
            messages.setStatus(new LimboStatusMessages());
            changed = true;
        } else {
            changed |= messages.getStatus().populateDefaults();
        }
        if (messages.getAddress() == null) {
            messages.setAddress(new LimboAddressMessages());
            changed = true;
        } else {
            changed |= messages.getAddress().populateDefaults();
        }
        if (messages.getPlayers() == null) {
            messages.setPlayers(new LimboPlayerMessages());
            changed = true;
        } else {
            changed |= messages.getPlayers().populateDefaults();
        }
        if (messages.getDetails() == null) {
            messages.setDetails(new LimboDetailMessages());
            changed = true;
        } else {
            changed |= messages.getDetails().populateDefaults();
        }
        if (messages.getDuration() == null) {
            messages.setDuration(new LimboDurationMessages());
            changed = true;
        } else {
            changed |= messages.getDuration().populateDefaults();
        }
        if (messages.getMemory() == null) {
            messages.setMemory(new LimboMemoryMessages());
            changed = true;
        } else {
            changed |= messages.getMemory().populateDefaults();
        }
        return changed;
    }

    private static CommandDefinition command(List<String> aliases, String description, String usage) {
        return command(aliases, description, usage, Map.of());
    }

    private static CommandDefinition command(
            List<String> aliases, String description, String usage, Map<String, String> arguments
    ) {
        return CommandDefinition.builder()
                .enabled(true)
                .aliases(aliases)
                .permission(PERMISSION_ADMIN)
                .description(description)
                .usage(usage)
                .arguments(arguments)
                .build();
    }
}
