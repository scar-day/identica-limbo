package dev.scarday.identicalimbo.common.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class LimboDurationMessages {
    private String days = "д ";
    private String hours = "ч ";
    private String minutes = "м ";
    private String seconds = "с";
    private String zero = "0с";

    public void copyFrom(LimboDurationMessages source) {
        if (source == null) return;
        if (source.days != null) this.days = source.days;
        if (source.hours != null) this.hours = source.hours;
        if (source.minutes != null) this.minutes = source.minutes;
        if (source.seconds != null) this.seconds = source.seconds;
        if (source.zero != null) this.zero = source.zero;
    }

    public boolean populateDefaults() {
        boolean changed = false;
        if (days == null) { days = "д "; changed = true; }
        if (hours == null) { hours = "ч "; changed = true; }
        if (minutes == null) { minutes = "м "; changed = true; }
        if (seconds == null) { seconds = "с"; changed = true; }
        if (zero == null) { zero = "0с"; changed = true; }
        return changed;
    }
}
