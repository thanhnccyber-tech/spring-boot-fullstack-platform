package vn.utetra.security;

/** Chống open redirect: chỉ cho phép chuyển hướng tới đường dẫn nội bộ của website. */
public final class SafeRedirect {

    private SafeRedirect() {}

    public static String localPath(String target, String fallback) {
        if (target == null || target.isBlank()) return fallback;
        String t = target.trim();
        boolean ok = t.startsWith("/")
                && !t.startsWith("//")
                && !t.contains("\\")
                && !t.contains("\r") && !t.contains("\n")
                && !t.toLowerCase().contains("://");
        return ok ? t : fallback;
    }
}
