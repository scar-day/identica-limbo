package dev.scarday.identicalimbo.api.provider;

import dev.scarday.identicalimbo.api.LimboProviderType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleLimboProviderRegistry implements LimboProviderRegistry {
    private final Map<LimboProviderType, LimboProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void register(LimboProvider provider) {
        Objects.requireNonNull(provider, "provider");
        providers.put(provider.type(), provider);
    }

    @Override
    public void unregister(LimboProviderType type) {
        if (type != null) {
            providers.remove(type);
        }
    }

    @Override
    public Optional<LimboProvider> find(LimboProviderType type) {
        if (type == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(type));
    }

    @Override
    public Collection<LimboProvider> all() {
        return List.copyOf(providers.values());
    }
}
