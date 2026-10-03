package dev.scarday.identicalimbo.bungeecord;

import dev.scarday.identicalimbo.bungeecord.platform.BungeeCordLimboService;
import dev.scarday.identicalimbo.command.LimboCommandRegistrar;
import java.util.logging.Level;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.identica.IdenticaAPI;
import net.md_5.bungee.api.plugin.Plugin;

public final class IdenticaLimboPlugin extends Plugin {
    private volatile BungeeCordLimboService limboService;

    @Override
    public void onEnable() {
        if (!IdenticaAPI.isInitialized()) {
            getLogger().severe("Identica API is not initialized; Identica Limbo will not start");
            return;
        }

        BungeeCordLimboService service = null;
        try {
            Configura configura = Config.yaml();
            service = new BungeeCordLimboService(
                    getProxy(),
                    getDataFolder().toPath(),
                    configura,
                    getLogger()
            );
            service.start();
            new LimboCommandRegistrar(
                    IdenticaAPI.getCommandService(), service, service.commands(), service.messages()
            ).register();
            limboService = service;
            getLogger().info("Identica Limbo initialized with " + service.servers().size() + " server(s)");
        } catch (Exception exception) {
            if (service != null) {
                try {
                    service.close();
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            getLogger().log(Level.SEVERE, "Unable to initialize Identica Limbo", exception);
        }
    }

    @Override
    public void onDisable() {
        BungeeCordLimboService service = limboService;
        limboService = null;
        if (service != null) {
            service.close();
        }
    }
}
