package android.os;

import android.app.Application;
import android.support.annotation.NonNull;
import java.util.PriorityQueue;

/**
 * Mimic the Android Looper class
 */
public class Looper implements Runnable {

    private class TimedAction implements Comparable<TimedAction> {

        final long time;
        final Runnable action;

        public TimedAction(long time, Runnable action) {
            this.time = time;
            this.action = action;
        }

        @Override
        public int compareTo(TimedAction o) {
            if (o == null) {
                return 1;
            }
            return Long.compare(time, o.time);
        }

    }
    private final PriorityQueue<TimedAction> messageQueue = new PriorityQueue<>();
    private final Thread looperThread;

    public Looper() {
        looperThread = new Thread(this);
        looperThread.setDaemon(true);
        looperThread.start();
    }

    void addAction(@NonNull Runnable action) {
        addActionAt(action, System.currentTimeMillis());
    }

    void addActionAt(@NonNull Runnable action, long time) {
        synchronized (messageQueue) {
            messageQueue.add(new TimedAction(time, action));
        }
        if (sleeping) {
            looperThread.interrupt();
        }
    }

    private boolean sleeping = false;

    @Override
    public void run() {
        while (true) {
            TimedAction next;
            long sleepTime;
            synchronized (messageQueue) {
                next = messageQueue.peek();
                if (next == null) { // empty, sleep till someone asks us to wake up.
                    sleepTime = Long.MAX_VALUE;
                } else {
                    sleepTime = next.time - System.currentTimeMillis();
                    if (sleepTime <= 0) {
                        next = messageQueue.poll();
                    }
                }
            }
            if (sleepTime <= 0) {
                next.action.run();
            } else {
                sleeping = true;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                }
                sleeping = false;
            }
        }
    }
}
