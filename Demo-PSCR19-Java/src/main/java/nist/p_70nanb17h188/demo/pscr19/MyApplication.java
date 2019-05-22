package nist.p_70nanb17h188.demo.pscr19;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication defaultInstance;

    private MyApplication() {
    }

    public static MyApplication getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new MyApplication();
        }
        return defaultInstance;
    }
}
