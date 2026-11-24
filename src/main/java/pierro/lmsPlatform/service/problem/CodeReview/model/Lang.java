package pierro.lmsPlatform.service.problem.CodeReview.model;

import java.util.Locale;
import java.util.Optional;

// Class cấu hình các loại ngôn ngữ mà code review phục vụ
public enum Lang {
    PYTHON("main.py"), C("main.c"), CPP("main.cpp"), JAVA("Main.java");
    
    private final String defaultFile;
    Lang(String f) { this.defaultFile = f; }
    public String defaultFile() { return defaultFile; }

   public static Optional<Lang> fromName(String s) {
        if (s == null) return Optional.empty();
        return switch (s.trim().toLowerCase(Locale.ROOT)) {
            case "python", "python3", "py" -> Optional.of(PYTHON);
            case "c" -> Optional.of(C);
            case "cpp", "c++", "cc" -> Optional.of(CPP);
            case "java" -> Optional.of(JAVA);
            default -> Optional.empty();
        };
    }
    public static Optional<Lang> fromPath(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        if (p.endsWith(".py")) return Optional.of(PYTHON);
        if (p.endsWith(".java")) return Optional.of(JAVA);
        if (p.endsWith(".c") || p.endsWith(".h")) return Optional.of(C);
        if (p.endsWith(".cpp") || p.endsWith(".cc") || p.endsWith(".cxx")
                || p.endsWith(".hpp") || p.endsWith(".hh")) return Optional.of(CPP);
        return Optional.empty();
    }
}