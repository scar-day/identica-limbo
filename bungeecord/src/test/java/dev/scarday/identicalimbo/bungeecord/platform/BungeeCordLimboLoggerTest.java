package dev.scarday.identicalimbo.bungeecord.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class BungeeCordLimboLoggerTest {
    @Test
    public void testLoggerFormatting() {
        Logger jul = Logger.getLogger("test-logger-" + System.currentTimeMillis());
        jul.setUseParentHandlers(false);
        List<LogRecord> records = new ArrayList<>();
        jul.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        });

        BungeeCordLimboLogger limboLogger = new BungeeCordLimboLogger(jul);
        limboLogger.info("Server {} started on port {}", "auth-1", 30066);

        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals("Server auth-1 started on port 30066", records.get(0).getMessage());
    }
}
