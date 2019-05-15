package android.content;

import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Mimic the android IntentFilter class.
 */
public class IntentFilter {

    private final HashSet<String> actions = new HashSet<>();

    public void addAction(String action) {
        actions.add(action);
    }
    
    public void forEachAction(Consumer<? super String> actionHandler) {
        actions.forEach(actionHandler);
    }
    
}
