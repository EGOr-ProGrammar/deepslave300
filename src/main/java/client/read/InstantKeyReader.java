package client.read;

import java.io.IOException;
import java.io.InputStream;

/**
 * Примитивный raw‑читатель клавиш.
 *  - Unix: использует `stty -icanon -echo` и читает по байту из System.in.
 *  - Windows: использует режим "quick edit off" и читает байты; всё равно зависит от консоли.
 */
public final class InstantKeyReader {

    private static boolean initialized = false;
    private static boolean rawEnabled = false;
    private static String originalStty;

    private InstantKeyReader() {}


    /** Включает raw‑режим (нужно вызвать один раз при старте клиента). */
    public static void enableRawMode() throws IOException {
        if (initialized) return;
        initialized = true;

        if (isUnix()) {
            originalStty = execAndCapture("sh", "-c", "stty -g");
            exec("sh", "-c", "stty -icanon -echo min 1 time 0");
            rawEnabled = true;
        } else if (isWindows()) {
            rawEnabled = true;
        }
    }

    /** Выключает raw‑режим (вызывать при выходе из программы). */
    public static void disableRawMode() throws IOException {
        if (!rawEnabled) return;
        if (isUnix() && originalStty != null) {
            exec("sh", "-c", "stty " + originalStty);
        }
        rawEnabled = false;
    }

    /**
     * Блокирующее чтение одного символа.
     * Возвращает код char (0–65535) или -1 при EOF.
     */
    public static int readKey() throws IOException {
        enableRawMode();
        InputStream in = System.in;
        return in.read();
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("mac");
    }

    private static void exec(String... cmd) throws IOException {
        try {
            new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static String execAndCapture(String... cmd) throws IOException {
        try {
            Process p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();
            byte[] buf = p.getInputStream().readAllBytes();
            p.waitFor();
            return new String(buf).trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }
}

