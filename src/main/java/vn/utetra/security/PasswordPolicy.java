package vn.utetra.security;

/** Quy tắc mật khẩu: 8-72 ký tự, có ít nhất 1 chữ cái và 1 chữ số. (72 là giới hạn của BCrypt) */
public final class PasswordPolicy {

    public static final String MESSAGE =
            "Mật khẩu phải từ 8 đến 72 ký tự, gồm ít nhất 1 chữ cái và 1 chữ số.";

    private PasswordPolicy() {}

    public static boolean isValid(String pwd) {
        if (pwd == null || pwd.length() < 8 || pwd.length() > 72) return false;
        boolean letter = false, digit = false;
        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c)) letter = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return letter && digit;
    }

    public static void require(String pwd) {
        if (!isValid(pwd)) throw new IllegalArgumentException(MESSAGE);
    }
}
