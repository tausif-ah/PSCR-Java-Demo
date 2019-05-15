package android.content;

/**
 * Mimic the BroadcastReceiver Intent class.
 */
public abstract class BroadcastReceiver {

    public abstract void onReceive(Context ctx, Intent intent);
}
