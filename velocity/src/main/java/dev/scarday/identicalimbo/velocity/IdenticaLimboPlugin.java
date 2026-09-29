package dev.scarday.identicalimbo.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.scarday.identicalimbo.command.LimboCommandRegistrar;
import dev.scarday.identicalimbo.velocity.platform.VelocityLimboService;
import java.nio.file.Path;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.identica.IdenticaAPI;
import org.slf4j.Logger;

@Plugin(
        id = "identica-limbo",
        name = "Identica Limbo",
        version = BuildConstants.VERSION,
        authors = {"ScarDay"},
        dependencies = {
                @Dependency(id = "identica")
        }
)
public final class IdenticaLimboPlugin {
    private final ProxyServer proxyServer;
    private final Path dataDirectory;
    private final Logger logger;
    private volatile VelocityLimboService limboService;

    @Inject
    public IdenticaLimboPlugin(ProxyServer proxyServer, @DataDirectory Path dataDirectory, Logger logger) {
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!IdenticaAPI.isInitialized()) {
            logger.error("Identica API is not initialized; Identica Limbo will not start");
            return;
        }
        VelocityLimboService service = null;
        try {
            Configura configura = Config.yaml();
            service = new VelocityLimboService(
                    proxyServer,
                    dataDirectory,
                    configura,
                    logger
            );
            service.start();
            new LimboCommandRegistrar(
                    IdenticaAPI.getCommandService(), service, service.commands(), service.messages()
            ).register();
            limboService = service;
            logger.info("Identica Limbo initialized with {} server(s)", service.servers().size());
        } catch (Exception exception) {
            if (service != null) {
                try {
                    service.close();
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            logger.error("Unable to initialize Identica Limbo", exception);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        VelocityLimboService service = limboService;
        limboService = null;
        if (service != null) {
            service.close();
        }
    }
}
