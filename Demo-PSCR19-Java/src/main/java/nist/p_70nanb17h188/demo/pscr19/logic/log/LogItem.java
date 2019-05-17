package nist.p_70nanb17h188.demo.pscr19.logic.log;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class LogItem {
    private static final AtomicLong GLOBAL_SERIAL = new AtomicLong(1);

    public final long id;
    @NonNull
    public final Date time;
    @NonNull
    public final LogType type;
    @NonNull
    public final String tag;
    @NonNull
    public final String message;

    LogItem(@NonNull LogType type, @NonNull String tag, @NonNull String message) {
        id = GLOBAL_SERIAL.getAndIncrement();
        time = new Date();
        this.type = type;
        this.tag = tag;
        this.message = message;
    }

}
