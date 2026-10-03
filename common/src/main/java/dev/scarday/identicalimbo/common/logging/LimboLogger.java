package dev.scarday.identicalimbo.common.logging;

public interface LimboLogger {
    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Throwable throwable);

    void debug(String message, Object... args);

    static String format(String message, Object... args) {
        if (args == null || args.length == 0 || message == null || !message.contains("{}")) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message.length() + 32);
        int argIndex = 0;
        int prev = 0;
        int pos;
        while ((pos = message.indexOf("{}", prev)) != -1 && argIndex < args.length) {
            sb.append(message, prev, pos);
            sb.append(args[argIndex++]);
            prev = pos + 2;
        }
        sb.append(message.substring(prev));
        return sb.toString();
    }
}
