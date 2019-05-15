package nist.p_70nanb17h188.demo.pscr19.logic;

import java.io.PrintStream;

/**
 * Some helper functions that can be used in all classes.
 */
public class Helper {
    private Helper() {
    }

    public static void printStackTrace(PrintStream ps) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            ps.printf("\tat %s.%s(%s:%d)%n", s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
        }
    }

}
