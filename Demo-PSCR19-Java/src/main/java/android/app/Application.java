package android.app;

import android.content.Context;

/**
 * Mimic the android Application class.
 */
public class Application {
    private final Context context = new Context();
    public Context getApplicationContext() {
        return context;
    }
}
