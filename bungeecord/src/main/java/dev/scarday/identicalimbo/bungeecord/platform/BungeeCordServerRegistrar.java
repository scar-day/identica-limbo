package dev.scarday.identicalimbo.bungeecord.platform;

import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.ServerRegistrar;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

@RequiredArgsConstructor
public final class BungeeCordServerRegistrar implements ServerRegistrar {
    private final ProxyServer proxyServer;
    private final Map<String, ServerInfo> registrations = new HashMap<>();

    @Override
    public synchronized void register(VirtualServerDefinition definition) {
        if (proxyServer.getServers().containsKey(definition.name())) {
            throw new IllegalStateException("BungeeCord server already exists: " + definition.name());
        }
        ServerInfo info = proxyServer.constructServerInfo(
                definition.name(),
                InetSocketAddress.createUnresolved(definition.host(), definition.port()),
                "",
                false
        );
        proxyServer.getServers().put(definition.name(), info);
        registrations.put(definition.name(), info);
    }

    @Override
    public synchronized void unregister(String serverName) {
        ServerInfo removed = registrations.remove(serverName);
        if (removed != null) {
            proxyServer.getServers().remove(serverName);
        }
    }

    @Override
    public synchronized void unregisterAll() {
        for (String name : registrations.keySet()) {
            proxyServer.getServers().remove(name);
        }
        registrations.clear();
    }
}
