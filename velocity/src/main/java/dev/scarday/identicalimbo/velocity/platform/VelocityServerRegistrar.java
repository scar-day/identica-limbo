package dev.scarday.identicalimbo.velocity.platform;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VelocityServerRegistrar implements ServerRegistrar {
    private final ProxyServer proxyServer;
    private final Map<String, ServerInfo> registrations = new HashMap<>();

    @Override
    public synchronized void register(VirtualServerDefinition definition) {
        if (proxyServer.getServer(definition.name()).isPresent()) {
            throw new IllegalStateException("Velocity server already exists: " + definition.name());
        }
        ServerInfo info = new ServerInfo(
                definition.name(),
                InetSocketAddress.createUnresolved(definition.host(), definition.port())
        );
        proxyServer.registerServer(info);
        registrations.put(definition.name(), info);
    }

    @Override
    public synchronized void unregister(String serverName) {
        ServerInfo removed = registrations.remove(serverName);
        if (removed != null) {
            proxyServer.unregisterServer(removed);
        }
    }

    @Override
    public synchronized void unregisterAll() {
        registrations.values().forEach(proxyServer::unregisterServer);
        registrations.clear();
    }
}
