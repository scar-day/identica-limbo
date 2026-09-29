package dev.scarday.identicalimbo.api.provider;

import dev.scarday.identicalimbo.api.LimboProviderType;
import java.util.Collection;
import java.util.Optional;

public interface LimboProviderRegistry {
    void register(LimboProvider provider);

    void unregister(LimboProviderType type);

    Optional<LimboProvider> find(LimboProviderType type);

    Collection<LimboProvider> all();
}
