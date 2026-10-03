package dev.scarday.identicalimbo.bungeecord.platform;

import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import dev.scarday.identicalimbo.api.provider.ManagedLimboServer;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import me.whereareiam.configura.Configura;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public final class BungeeCordLimboServiceTest {
    private ProxyServer proxyServer;
    private Logger logger;
    private BungeeCordLimboService service;

    @BeforeEach
    public void setUp() {
        proxyServer = Mockito.mock(ProxyServer.class);
        logger = Logger.getLogger("test");
        Configura configura = Mockito.mock(Configura.class);
        service = new BungeeCordLimboService(proxyServer, Path.of("tmp"), configura, logger);
    }

    @Test
    public void testGetConnectedPlayers() {
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        ProxiedPlayer p1 = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(p1.getName()).thenReturn("Bob");
        ProxiedPlayer p2 = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(p2.getName()).thenReturn("Alice");

        Mockito.when(serverInfo.getPlayers()).thenReturn(List.of(p1, p2));
        Mockito.when(proxyServer.getServerInfo("auth-1")).thenReturn(serverInfo);

        List<String> players = service.getConnectedPlayers("auth-1");
        Assertions.assertEquals(List.of("Alice", "Bob"), players);
    }

    @Test
    public void testGetConnectedPlayersUnknownServer() {
        Mockito.when(proxyServer.getServerInfo("unknown")).thenReturn(null);
        List<String> players = service.getConnectedPlayers("unknown");
        Assertions.assertTrue(players.isEmpty());
    }

    @Test
    public void testTransferNonExistentPlayerReturnsFalse() {
        UUID uuid = UUID.randomUUID();
        Mockito.when(proxyServer.getPlayer(uuid)).thenReturn(null);

        CompletableFuture<Boolean> future = service.transfer(uuid, "auth-1");
        Assertions.assertFalse(future.join());
    }

    @Test
    public void testTransferTargetNotLimboReturnsFalse() {
        UUID uuid = UUID.randomUUID();
        ProxiedPlayer player = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(proxyServer.getPlayer(uuid)).thenReturn(player);
        Mockito.when(proxyServer.getServerInfo("lobby")).thenReturn(Mockito.mock(ServerInfo.class));

        CompletableFuture<Boolean> future = service.transfer(uuid, "lobby");
        Assertions.assertFalse(future.join());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTransferToLimboServerSuccess() {
        UUID uuid = UUID.randomUUID();
        ProxiedPlayer player = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(player.getUniqueId()).thenReturn(uuid);
        Mockito.when(player.getName()).thenReturn("Alice");
        Mockito.when(proxyServer.getPlayer(uuid)).thenReturn(player);
        Mockito.when(proxyServer.getPlayer("Alice")).thenReturn(player);

        ServerInfo target = Mockito.mock(ServerInfo.class);
        Mockito.when(proxyServer.getServerInfo("auth-1")).thenReturn(target);

        // Put a managed server in activeServers so isLimbo("auth-1") returns true
        VirtualServerDefinition def = new VirtualServerDefinition("auth-1", LimboProviderType.PICOLIMBO, "127.0.0.1", 30066);
        ManagedLimboServer managedServer = Mockito.mock(ManagedLimboServer.class);
        Mockito.when(managedServer.definition()).thenReturn(def);
        service.registerActiveServerForTesting("auth-1", managedServer);

        Mockito.doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.done(true, null);
            return null;
        }).when(player).connect(Mockito.eq(target), Mockito.any(Callback.class));

        CompletableFuture<Boolean> future = service.transfer(uuid, "auth-1");
        Assertions.assertTrue(future.join());

        CompletableFuture<Boolean> futureByName = service.transfer("Alice", "auth-1");
        Assertions.assertTrue(futureByName.join());
    }

    @Test
    public void testFindPlayerUuid() {
        UUID uuid = UUID.randomUUID();
        ProxiedPlayer player = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(player.getUniqueId()).thenReturn(uuid);
        Mockito.when(proxyServer.getPlayer("Player1")).thenReturn(player);
        Mockito.when(proxyServer.getPlayer("Unknown")).thenReturn(null);

        Assertions.assertEquals(uuid, service.findPlayerUuid("Player1"));
        Assertions.assertNull(service.findPlayerUuid("Unknown"));
    }

    @Test
    public void testGetAllPlayerUuids() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        ProxiedPlayer p1 = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(p1.getUniqueId()).thenReturn(u1);
        ProxiedPlayer p2 = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(p2.getUniqueId()).thenReturn(u2);

        Mockito.when(proxyServer.getPlayers()).thenReturn(List.of(p1, p2));

        List<UUID> uuids = service.getAllPlayerUuids();
        Assertions.assertEquals(List.of(u1, u2), uuids);
    }

    @Test
    public void testGetPlayerUuidsConnectedTo() {
        UUID u1 = UUID.randomUUID();
        ProxiedPlayer p1 = Mockito.mock(ProxiedPlayer.class);
        Mockito.when(p1.getUniqueId()).thenReturn(u1);

        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(serverInfo.getPlayers()).thenReturn(List.of(p1));
        Mockito.when(proxyServer.getServerInfo("hub-1")).thenReturn(serverInfo);
        Mockito.when(proxyServer.getServerInfo("unknown")).thenReturn(null);

        Assertions.assertEquals(List.of(u1), service.getPlayerUuidsConnectedTo("hub-1"));
        Assertions.assertTrue(service.getPlayerUuidsConnectedTo("unknown").isEmpty());
    }
}
