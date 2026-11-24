package pierro.lmsPlatform.service.problem.CodeReview.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Chạy các công cụ tĩnh phân tích trong Docker, lấy kết quả thô trả lại cho Backend Spring Boot.
@Component
public class ToolContainer {
    public record Out(String stdout, String stderr, int exit, boolean timedOut) {}
    private static final int MAX_BYTES = 4 * 1024 * 1024;

    @Value("${codereview.tools.image:codereview-tools:1}") private String image;

    public Out run(Path srcDir, List<String> toolCmd, long timeoutMs) throws IOException, InterruptedException {
        String name = "cr_" + UUID.randomUUID().toString().replace("-", "");
        String src = srcDir.toAbsolutePath().toString().replace("\\", "/");
        List<String> cmd = new ArrayList<>(List.of(
                "docker", "run", "--rm", "--name", name,
                "--network", "none", "--label", "codereview=1",
                "--memory", "1g", "--memory-swap", "1g", "--cpus", "1", "--pids-limit", "256",
                "--cap-drop", "ALL", "--security-opt", "no-new-privileges",
                "--tmpfs", "/tmp:rw,size=256m",
                "-e", "HOME=/tmp", "-e", "JAVA_TOOL_OPTIONS=-Xmx700m",
                "--mount", "type=bind,src=" + src + ",dst=/src,readonly", "-w", "/src",
                image));
        cmd.addAll(toolCmd);

        Path o = Files.createTempFile("cr_out_", ".txt");
        Path e = Files.createTempFile("cr_err_", ".txt");
        try {
            Process p = new ProcessBuilder(cmd).redirectOutput(o.toFile()).redirectError(e.toFile()).start();
            boolean done = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!done) {
                new ProcessBuilder("docker", "kill", name).start().waitFor(5, TimeUnit.SECONDS);
                p.destroyForcibly();
            }
            Out out = new Out(read(o), read(e), done ? p.exitValue() : -1, !done);

            // Lỗi hạ tầng không được trả về như "tool chạy xong, không có vấn đề gì":
            // nếu không, báo cáo rỗng sẽ bị coi là đầy đủ và bị cache.
            if (done && isInfraFailure(out.exit())) {
                throw new IOException("docker/tool failed (exit=" + out.exit() + "): " + clip(out.stderr()));
            }
            return out;
        } finally {
            Files.deleteIfExists(o);
            Files.deleteIfExists(e);
        }
    }

    /** 125: docker run lỗi (thiếu image, daemon...), 126/127: không chạy được hoặc không có lệnh, 137: bị kill (OOM). */
    private static boolean isInfraFailure(int exit) {
        return exit == 125 || exit == 126 || exit == 127 || exit == 137;
    }

    // giới hạn nội dung thông báo lỗi
    private static String clip(String s) {
        String t = s == null ? "" : s.strip();
        return t.length() > 300 ? t.substring(0, 300) : t;
    }

    private static String read(Path p) throws IOException {
        try (InputStream in = Files.newInputStream(p)) {
            return new String(in.readNBytes(MAX_BYTES), StandardCharsets.UTF_8);
        }
    }
}