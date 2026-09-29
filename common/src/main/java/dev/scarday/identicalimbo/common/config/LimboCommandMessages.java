package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboCommandMessages {
    private String usage = "<white>Использование: /limbo list, /limbo info <name>, /limbo reload";
    private String listEmpty = "<yellow>Виртуальные серверы не зарегистрированы.";
    private String listHeader = "<green>Виртуальные серверы:";
    private String listEntry = "<gray>- <name> [<provider>] <host>:<port>";
    private String infoMissing = "<red>Сервер не найден: <name>";
    private String infoFound = LimboConfigurationLoader.DEFAULT_INFO_FOUND;
    private String reloadSuccess = "<green>Конфигурация limbo перезагружена.";
    private String reloadFailure = "<red>Не удалось перезагрузить конфигурацию limbo.";

    private LimboStatusMessages status = new LimboStatusMessages();
    private LimboAddressMessages address = new LimboAddressMessages();
    private LimboPlayerMessages players = new LimboPlayerMessages();
    private LimboDetailMessages details = new LimboDetailMessages();
    private LimboDurationMessages duration = new LimboDurationMessages();
    private LimboMemoryMessages memory = new LimboMemoryMessages();

    public void copyFrom(LimboCommandMessages source) {
        if (source == null) return;
        if (source.usage != null) this.usage = source.usage;
        if (source.listEmpty != null) this.listEmpty = source.listEmpty;
        if (source.listHeader != null) this.listHeader = source.listHeader;
        if (source.listEntry != null) this.listEntry = source.listEntry;
        if (source.infoMissing != null) this.infoMissing = source.infoMissing;
        if (source.infoFound != null) this.infoFound = source.infoFound;
        if (source.reloadSuccess != null) this.reloadSuccess = source.reloadSuccess;
        if (source.reloadFailure != null) this.reloadFailure = source.reloadFailure;
        if (source.status != null) this.status.copyFrom(source.status);
        if (source.address != null) this.address.copyFrom(source.address);
        if (source.players != null) this.players.copyFrom(source.players);
        if (source.details != null) this.details.copyFrom(source.details);
        if (source.duration != null) this.duration.copyFrom(source.duration);
        if (source.memory != null) this.memory.copyFrom(source.memory);
    }
}
