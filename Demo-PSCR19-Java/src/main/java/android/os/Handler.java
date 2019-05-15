package android.os;

import android.support.annotation.NonNull;

/**
 * Mimic the android Handler class.
 */
public class Handler {

    @NonNull
    private final Looper looper;

    public Handler(@NonNull Looper looper) {
        this.looper = looper;
    }

    public void post(Runnable action) {
        looper.addAction(action);
    }

    public void postDelayed(Runnable action, long delay) {
        looper.addActionAt(action, System.currentTimeMillis() + delay);
    }
    
    public void postAt(Runnable action, long time) {
        looper.addActionAt(action, time);
    }
}
