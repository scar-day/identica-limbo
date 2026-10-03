package dev.scarday.identicalimbo.provider.picolimbo.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ChecksumVerifier {
    private ChecksumVerifier() {
    }

    public static void verifySha256(Path file, String digest) throws IOException {
        if (digest == null || !digest.startsWith("sha256:")) {
            return;
        }
        try (InputStream input = Files.newInputStream(file)) {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            input.transferTo(new OutputStream() {
                @Override
                public void write(int value) {
                    sha256.update((byte) value);
                }

                @Override
                public void write(byte[] bytes, int offset, int length) {
                    sha256.update(bytes, offset, length);
                }
            });
            String actual = HexFormat.of().formatHex(sha256.digest());
            String expected = digest.substring("sha256:".length());
            if (!actual.equalsIgnoreCase(expected)) {
                throw new IOException("PicoLimbo archive checksum mismatch");
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new IOException("SHA-256 is unavailable", exception);
        }
    }
}
