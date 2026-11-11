package pierro.lmsPlatform.service.judge;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class OutputChecker {

    /** Chỉ chấp nhận số thập phân thuần: không NaN/Infinity, không hậu tố d/f, không hex-float. */
    // Không dùng `\\d+\\.?\\d*` (hai lượng từ liền kề cùng khớp chữ số -> backtracking O(n^2) với token dài).
    private static final Pattern NUMBER = Pattern.compile("[-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?");
    private static final Pattern INTEGER = Pattern.compile("[-+]?\\d+");

    /**
     * @param epsilon 0 -> so khớp theo dòng (bỏ khoảng trắng cuối dòng, thống nhất \r\n, bỏ dòng trống cuối)
     *                >0 -> so từng token; token là số thực thì cho phép sai số tương đối/tuyệt đối epsilon;
     *                      hai token cùng là số NGUYÊN thì luôn so chính xác (không áp epsilon)
     */
    public boolean check(String actual, String expected, double epsilon) {
        if (epsilon > 0) return checkTokens(actual, expected, epsilon);
        return normalize(actual).equals(normalize(expected));
    }

    static String normalize(String s) {
        if (s == null) return "";
        String[] lines = s.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i].stripTrailing());
        }
        return sb.toString().stripTrailing();
    }

    private static boolean checkTokens(String actual, String expected, double eps) {
        String[] a = tokens(actual);
        String[] b = tokens(expected);
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(b[i])) continue;
            if (INTEGER.matcher(a[i]).matches() && INTEGER.matcher(b[i]).matches()) {
                if (canonInt(a[i]).equals(canonInt(b[i]))) continue;   // "+5" == "007" == "5"
                return false;
            }
            if (!NUMBER.matcher(a[i]).matches() || !NUMBER.matcher(b[i]).matches()) return false;
            double x, y;
            try {
                x = Double.parseDouble(a[i]);
                y = Double.parseDouble(b[i]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y)) return false;
            // viết dạng !(<=) để NaN (nếu có) luôn bị loại
            if (!(Math.abs(x - y) <= eps * Math.max(1.0, Math.abs(y)))) return false;
        }
        return true;
    }

    /** Chuẩn hóa số nguyên (bỏ dấu +, số 0 đầu, -0) để so chuỗi; tuyến tính, không dùng BigInteger. */
    private static String canonInt(String s) {
        boolean neg = s.charAt(0) == '-';
        int i = (s.charAt(0) == '-' || s.charAt(0) == '+') ? 1 : 0;
        while (i < s.length() - 1 && s.charAt(i) == '0') i++;
        String digits = s.substring(i);
        return digits.equals("0") ? "0" : (neg ? "-" + digits : digits);
    }

    private static String[] tokens(String s) {
        String t = s == null ? "" : s.trim();
        return t.isEmpty() ? new String[0] : t.split("\\s+");
    }
}
