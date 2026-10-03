package dev.scarday.identicalimbo.common.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import me.whereareiam.configura.merge.defaults.DefaultsProvider;

public final class VirtualServerDefaults implements DefaultsProvider<VirtualServerDocument> {
    private static final List<Consumer<VirtualServerDocument>> CONTRIBUTORS = new CopyOnWriteArrayList<>();

    public static void registerContributor(Consumer<VirtualServerDocument> contributor) {
        if (contributor != null) {
            CONTRIBUTORS.add(contributor);
        }
    }

    @Override
    public VirtualServerDocument supply(VirtualServerDocument server) {
        server.setServer(new ServerDocument());
        for (Consumer<VirtualServerDocument> contributor : CONTRIBUTORS) {
            contributor.accept(server);
        }
        return server;
    }
}
