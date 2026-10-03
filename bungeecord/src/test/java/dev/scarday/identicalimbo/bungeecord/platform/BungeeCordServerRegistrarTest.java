package dev.scarday.identicalimbo.bungeecord.platform;

import dev.scarday.identicalimbo.api.LimboProviderType;
import dev.scarday.identicalimbo.api.VirtualServerDefinition;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public final class BungeeCordServerRegistrarTest {
    private ProxyServer proxyServer;
    private Map<String, ServerInfo> serversMap;
    private BungeeCordServerRegistrar registrar;

    @BeforeEach
    public void setUp() {
        proxyServer = Mockito.mock(ProxyServer.class);
        serversMap = new HashMap<>();
        Mockito.when(proxyServer.getServers()).thenReturn(serversMap);
        Mockito.when(proxyServer.constructServerInfo(
                Mockito.anyString(),
                Mockito.any(InetSocketAddress.class),
                Mockito.anyString(),
                Mockito.anyBoolean()
        )).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            InetSocketAddress address = invocation.getArgument(1);
            ServerInfo mockInfo = Mockito.mock(ServerInfo.class);
            Mockito.when(mockInfo.getName()).thenReturn(name);
            Mockito.when(mockInfo.getAddress()).thenReturn(address);
            Mockito.when(mockInfo.getSocketAddress()).thenReturn(address);
            return mockInfo;
        });

        registrar = new BungeeCordServerRegistrar(proxyServer);
    }

    @Test
    public void testRegisterServerSuccessfully() {
        VirtualServerDefinition definition = new VirtualServerDefinition("auth-1", LimboProviderType.PICOLIMBO, "127.0.0.1", 30066);
        registrar.register(definition);

        Assertions.assertTrue(serversMap.containsKey("auth-1"));
        ServerInfo registered = serversMap.get("auth-1");
        Assertions.assertNotNull(registered);
        Assertions.assertEquals("auth-1", registered.getName());
    }

    @Test
    public void testRegisterDuplicateServerThrowsException() {
        VirtualServerDefinition definition = new VirtualServerDefinition("auth-1", LimboProviderType.PICOLIMBO, "127.0.0.1", 30066);
        registrar.register(definition);

        Assertions.assertThrows(IllegalStateException.class, () -> registrar.register(definition));
    }

    @Test
    public void testUnregisterServer() {
        VirtualServerDefinition definition = new VirtualServerDefinition("auth-1", LimboProviderType.PICOLIMBO, "127.0.0.1", 30066);
        registrar.register(definition);
        Assertions.assertTrue(serversMap.containsKey("auth-1"));

        registrar.unregister("auth-1");
        Assertions.assertFalse(serversMap.containsKey("auth-1"));
    }

    @Test
    public void testUnregisterAllServers() {
        ServerInfo existingServer = Mockito.mock(ServerInfo.class);
        Mockito.when(existingServer.getName()).thenReturn("lobby");
        serversMap.put("lobby", existingServer);

        VirtualServerDefinition def1 = new VirtualServerDefinition("auth-1", LimboProviderType.PICOLIMBO, "127.0.0.1", 30066);
        VirtualServerDefinition def2 = new VirtualServerDefinition("auth-2", LimboProviderType.PICOLIMBO, "127.0.0.1", 30067);
        registrar.register(def1);
        registrar.register(def2);

        Assertions.assertEquals(3, serversMap.size());

        registrar.unregisterAll();

        Assertions.assertEquals(1, serversMap.size());
        Assertions.assertTrue(serversMap.containsKey("lobby"));
        Assertions.assertFalse(serversMap.containsKey("auth-1"));
        Assertions.assertFalse(serversMap.containsKey("auth-2"));
    }
}
