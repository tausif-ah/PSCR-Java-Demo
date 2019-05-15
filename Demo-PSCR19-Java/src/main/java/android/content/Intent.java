package android.content;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Mimic the android Intent class.
 */
public class Intent {

    private final String action;
    private final HashMap<String, Object> extras = new HashMap<>();

    public Intent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Intent putExtra(String name, Object extra) {
        extras.put(name, extra);
        return this;
    }

    public boolean getBooleanExtra(String name, boolean defaultValue) {
        Object obj = extras.get(name);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return defaultValue;
    }

    public int getIntExtra(String name, int defaultValue) {
        Object obj = extras.get(name);
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return defaultValue;
    }

    public <T> T getParcelableExtra(String name) {
        return (T) extras.get(name);
    }

    public Serializable getSerializableExtra(String name) {
        return (Serializable) extras.get(name);
    }

}
