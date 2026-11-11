package pierro.lmsPlatform.DTO.Request.Judge;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Cấu hình từng ngôn ngữ. Thêm ngôn ngữ mới = thêm 1 dòng vào REGISTRY.
 * - compileCmd = null nếu ngôn ngữ thông dịch.
 * - {MEM_MB} trong runCmd sẽ được thay bằng giới hạn bộ nhớ (dùng cho -Xmx của Java).
 * - memoryOverheadMb: cộng thêm cho container (shell/timeout/tmpfs; JVM cần thêm metaspace, thread...).
 * - Image phải có: sh, timeout (coreutils), setpriv (util-linux), date.
 */
public record LanguageConfig(
        String id,
        String image,
        String sourceFile,
        String compileCmd,
        String runCmd,
        int memoryOverheadMb,
        int pidsLimit) {

    private static final LanguageConfig CPP = new LanguageConfig(
            "cpp", "gcc:13", "main.cpp",
            "g++ -O2 -std=c++17 -o /app/main.out /app/main.cpp",
            "/app/main.out", 16, 64);

    private static final LanguageConfig PYTHON = new LanguageConfig(
            "python", "python:3.12-slim", "main.py",
            null,
            "python3 /app/main.py", 16, 64);

    private static final LanguageConfig JAVA = new LanguageConfig(
            "java", "eclipse-temurin:21-jdk", "Main.java",
            "javac -d /app /app/Main.java",
            "java -Xmx{MEM_MB}m -Xss64m -XX:+UseSerialGC -XX:-UsePerfData -cp /app Main", 160, 128);

    private static final Map<String, LanguageConfig> REGISTRY = Map.of(
            "cpp", CPP, "c++", CPP,
            "python", PYTHON, "py", PYTHON,
            "java", JAVA);

    public static Optional<LanguageConfig> find(String id) {
        return id == null ? Optional.empty()
                : Optional.ofNullable(REGISTRY.get(id.toLowerCase(Locale.ROOT).trim()));
    }

    /** Tất cả cấu hình ngôn ngữ (không trùng) - dùng để pre-pull image lúc khởi động. */
    public static List<LanguageConfig> all() {
        return REGISTRY.values().stream().distinct().toList();
    }

    public boolean needsCompile() {
        return compileCmd != null;
    }
}
