package dev.scarday.identicalimbo.common.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.ConfigDocument;

@Getter
@Setter
public final class VirtualServerDocument extends ConfigDocument {
    private ServerDocument server = new ServerDocument();
    private Map<String, Object> extensions = new HashMap<>();

    @JsonAnySetter
    public void setExtension(String key, Object value) {
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        extensions.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }
}
