package dev.scarday.identicalimbo.common.config;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;

public final class VirtualServerDefaults implements DefaultsProvider<VirtualServerDocument> {
    @Override
    public VirtualServerDocument supply(VirtualServerDocument server) {
        server.setServer(new ServerDocument());
        server.setPicolimbo(new PicoLimboDocument());
        return server;
    }
}
