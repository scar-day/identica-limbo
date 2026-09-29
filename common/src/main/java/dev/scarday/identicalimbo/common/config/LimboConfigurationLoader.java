package dev.scarday.identicalimbo.common.config;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.common.LimboConfiguration;
import dev.scarday.identicalimbo.api.LimboProviderType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import me.whereareiam.configura.Configura;

@RequiredArgsConstructor
public final class LimboConfigurationLoader {
    public static final String CONFIG_FILE = "config";
    public static final String SERVER_DIRECTORY = "limbo";
    private static final String DEFAULT_PICO_FILE = "server1-picolimbo";
    private static final String DEFAULT_PICO_NAME = "auth-1";
    private static final String YML_EXTENSION = ".yml";
    private static final String LEGACY_TOML_YML_EXTENSION = ".toml.yml";
    private static final String COMMAND_INFO = "info";
    private static final String ARG_NAME = "name";
    private static final String ARG_NAME_DESCRIPTION = "Limbo server name";
    private static final String USAGE_INFO = "{command} {alias} <name>";
    private static final String USAGE_DEFAULT = "{command} {alias}";
    private static final String COMMAND_TOKEN = "{command}";
    private static final String LEGACY_LIST_KEY = "limbo-list";
    private static final String LEGACY_INFO_KEY = "limbo-info";
    private static final String LEGACY_RELOAD_KEY = "limbo-reload";
    private static final String LIST_KEY = "list";
    private static final String RELOAD_KEY = "reload";

    private static final Map<String, String> LEGACY_COMMAND_KEYS = Map.of(
            LEGACY_LIST_KEY, LIST_KEY,
            LEGACY_INFO_KEY, COMMAND_INFO,
            LEGACY_RELOAD_KEY, RELOAD_KEY
    );
    private static final List<String> LEGACY_LIMBO_INFO_ALIASES = List.of("limbo info", "limbo info <name>");
    private static final List<String> LIMBO_INFO_ALIASES = List.of("limbo info");
    public static final String DEFAULT_INFO_FOUND =
            "<gray>Информация о сервере <gold><name></gold>:</gray>\n"
                    + "<gray>• Провайдер: <white><provider></white></gray>\n"
                    + "<gray>• Статус: <status></gray>\n"
                    + "<gray>• Адрес: <white><address></white></gray>\n"
                    + "<gray>• Игроки: <white><players></white></gray>\n"
                    + "<gray>• Память: <white><memory></white></gray>\n"
                    + "<gray>• Аптайм: <white><uptime></white></gray>\n"
                    + "<gray>• Детали: <white><details></white></gray>";

    private final Configura configura;

    public LimboConfiguration load(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        removeLegacyDoubleExtensionFiles(dataDirectory);
        Path configFile = dataDirectory.resolve(CONFIG_FILE);
        IdenticaLimboSettings settings = configura
                .withDefaults(IdenticaLimboSettingsDefaults.class)
                .update(configFile, IdenticaLimboSettings.class);
        if (replaceLegacyCommandConfiguration(settings)) {
            configura.save(configFile, settings);
        }

        Path limboDirectory = dataDirectory.resolve(SERVER_DIRECTORY);
        Files.createDirectories(limboDirectory);
        List<Path> files = serverFiles(limboDirectory);
        if (files.isEmpty()) {
            createDefaultServer(limboDirectory.resolve(DEFAULT_PICO_FILE), DEFAULT_PICO_NAME, true, LimboProviderType.PICOLIMBO);
            files = serverFiles(limboDirectory);
        }

        List<VirtualServerDefinition> servers = new ArrayList<>(files.size());
        Map<String, VirtualServerDocument> serverDocuments = new HashMap<>();
        Set<String> names = new HashSet<>();
        for (Path file : files) {
            VirtualServerDocument document = configura
                    .withDefaults(VirtualServerDefaults.class)
                    .update(file, VirtualServerDocument.class);
            ServerDocument server = requireValue(file, "server", document.getServer());
            LimboProviderType provider = requireValue(file, "server.type", server.getType());
            boolean enabled = server.getEnabled() == null || server.getEnabled();
            if (!enabled) {
                continue;
            }
            String name = server.getName() == null || server.getName().isBlank()
                    ? serverName(file)
                    : server.getName().trim();
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate enabled limbo server name: " + name);
            }
            String host = requireValue(file, "server.host", server.getHost());
            VirtualServerDefinition definition = new VirtualServerDefinition(name, provider, host, server.getPort());
            servers.add(definition);
            serverDocuments.put(name.toLowerCase(Locale.ROOT), document);
        }
        return new LimboConfiguration(
                servers, serverDocuments, settings.getCommands(), settings.getMessages()
        );
    }

    private void createDefaultServer(Path file, String name, boolean enabled, LimboProviderType provider)
            throws IOException {
        VirtualServerDocument document = configura
                .withDefaults(VirtualServerDefaults.class)
                .update(file, VirtualServerDocument.class);
        document.getServer().setName(name);
        document.getServer().setEnabled(enabled);
        document.getServer().setType(provider);
        configura.save(file, document);
    }

    private static boolean replaceLegacyCommandConfiguration(IdenticaLimboSettings settings) {
        boolean changed = false;
        Map<String, me.whereareiam.identica.model.CommandDefinition> commands = settings.getCommands();
        for (Map.Entry<String, String> entry : LEGACY_COMMAND_KEYS.entrySet()) {
            var legacy = commands.remove(entry.getKey());
            if (legacy == null) {
                continue;
            }
            commands.putIfAbsent(entry.getValue(), legacy);
            changed = true;
        }

        var info = commands.get(COMMAND_INFO);
        if (info != null) {
            if (info.getAliases() != null && LEGACY_LIMBO_INFO_ALIASES.containsAll(info.getAliases())
                    && info.getAliases().size() == 1 && !LIMBO_INFO_ALIASES.equals(info.getAliases())) {
                info.setAliases(LIMBO_INFO_ALIASES);
                changed = true;
            }
            if (info.getUsage() == null || info.getUsage().isBlank() || !info.getUsage().contains("<name>") || !info.getUsage().contains(COMMAND_TOKEN)) {
                info.setUsage(USAGE_INFO);
                changed = true;
            }
            if (info.getArguments() == null) {
                info.setArguments(new LinkedHashMap<>());
                changed = true;
            }
            if (!info.getArguments().containsKey(ARG_NAME)) {
                info.getArguments().put(ARG_NAME, ARG_NAME_DESCRIPTION);
                changed = true;
            }
        }

        for (Map.Entry<String, me.whereareiam.identica.model.CommandDefinition> entry : commands.entrySet()) {
            var def = entry.getValue();
            if (def != null && (def.getUsage() == null || def.getUsage().isBlank() || !def.getUsage().contains(COMMAND_TOKEN))) {
                def.setUsage(COMMAND_INFO.equals(entry.getKey()) ? USAGE_INFO : USAGE_DEFAULT);
                changed = true;
            }
        }

        var messages = settings.getMessages();
        if (messages != null) {
            if (messages.getUsage() != null && messages.getUsage().contains("/limbo list")) {
                messages.setUsage("<white>Использование: /identica limbo list, /identica limbo info <name>, /identica limbo reload");
                changed = true;
            }
            String infoFound = messages.getInfoFound();
            if (infoFound == null || infoFound.isBlank()
                    || infoFound.equals("<name>: <provider> <host>:<port>")
                    || infoFound.equals("<green><name>: <provider> <host>:<port>")) {
                messages.setInfoFound(DEFAULT_INFO_FOUND);
                changed = true;
            }
            if (IdenticaLimboSettingsDefaults.populateDefaults(messages)) {
                changed = true;
            }
        }
        return changed;
    }

    private static <T> T requireValue(Path file, String field, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid configuration value in " + file + ": " + field + " must not be null");
        }
        return value;
    }

    private static List<Path> serverFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(YML_EXTENSION))
                    .sorted()
                    .toList();
        }
    }

    private static String serverName(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.length() - YML_EXTENSION.length());
    }

    private static void removeLegacyDoubleExtensionFiles(Path dataDirectory) throws IOException {
        Files.deleteIfExists(dataDirectory.resolve("config" + LEGACY_TOML_YML_EXTENSION));
        Path limboDirectory = dataDirectory.resolve(SERVER_DIRECTORY);
        if (!Files.isDirectory(limboDirectory)) {
            return;
        }
        try (var paths = Files.list(limboDirectory)) {
            for (Path path : paths.toList()) {
                if (path.getFileName().toString().endsWith(LEGACY_TOML_YML_EXTENSION)) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
