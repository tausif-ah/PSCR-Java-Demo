package android.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Mimic the android Log function
 */
public class Log {
    
    private static PrintStream output = System.out;
    
    public static void redirectOutput(PrintStream newOut) {
        output = newOut;
    }

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static void v(String tag, String msg) {
        output.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "V", tag, msg);
    }

    public static void d(String tag, String msg) {
        output.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "D", tag, msg);
    }

    public static void i(String tag, String msg) {
        output.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "I", tag, msg);
    }

    public static void w(String tag, String msg) {
        output.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "W", tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        output.printf(Locale.US, "[%s] %s/%s: %s%n", DEFAULT_DATE_FORMAT.format(new Date()), "E", tag, msg);
        if (tr != null) {
            tr.printStackTrace(output);
        }
    }
}
