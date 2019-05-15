package android.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Mimic the android Log function
 */
public class Log {

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static void v(String tag, String msg) {
        System.out.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "V", tag, msg);
    }

    public static void d(String tag, String msg) {
        System.out.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "D", tag, msg);
    }

    public static void i(String tag, String msg) {
        System.out.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "I", tag, msg);
    }

    public static void w(String tag, String msg) {
        System.out.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "W", tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        System.out.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "E", tag, msg);
        if (tr != null) {
            tr.printStackTrace(System.out);
        }
    }
}
