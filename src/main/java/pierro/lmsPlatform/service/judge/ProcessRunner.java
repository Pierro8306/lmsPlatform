package pierro.lmsPlatform.service.judge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Chạy process ngoài: đọc stdout/stderr song song (tránh deadlock),
 * giới hạn dung lượng giữ lại (tránh tràn RAM), có timeout + hook khi timeout/interrupt.
 */
final class ProcessRunner {
    private ProcessRunner() {}

    record Result(String stdout, String stderr, int exitCode, boolean timedOut) {}

    static Result run(List<String> cmd, long timeoutMs, int maxBytesPerStream, Runnable onTimeout)
            throws IOException, InterruptedException {

        Process p = new ProcessBuilder(cmd).start();
        p.getOutputStream().close(); // không có stdin

        Collector out = new Collector(p.getInputStream(), maxBytesPerStream);
        Collector err = new Collector(p.getErrorStream(), maxBytesPerStream);
        out.start();
        err.start();

        boolean finished;
        try {
            finished = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            p.destroyForcibly();           // không để process mồ côi khi bị interrupt/shutdown
            throw e;
        }
        if (!finished) {
            if (onTimeout != null) onTimeout.run();
            p.destroyForcibly();
            p.waitFor(5, TimeUnit.SECONDS);
        }
        out.join(2000);
        err.join(2000);
        return new Result(out.text(), err.text(), finished ? p.exitValue() : -1, !finished);
    }

    /** Đọc hết stream nhưng chỉ giữ tối đa `max` byte đầu. */
    private static final class Collector extends Thread {
        private final InputStream in;
        private final int max;
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

        Collector(InputStream in, int max) {
            this.in = in;
            this.max = max;
            setDaemon(true);
        }

        @Override
        public void run() {
            byte[] chunk = new byte[8192];
            try {
                int n;
                while ((n = in.read(chunk)) != -1) {
                    int room = max - buf.size();
                    if (room > 0) buf.write(chunk, 0, Math.min(n, room));
                }
            } catch (IOException ignored) {}
        }

        String text() {
            return buf.toString(StandardCharsets.UTF_8);
        }
    }
}
