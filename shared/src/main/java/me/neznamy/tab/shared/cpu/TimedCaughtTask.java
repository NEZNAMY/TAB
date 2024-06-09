package me.neznamy.tab.shared.cpu;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;

/**
 * Runnable that measures how long the task took and try/catches it.
 */
@RequiredArgsConstructor
public class TimedCaughtTask implements Runnable {

    /** CPU manager to add time to */
    private final CpuManager cpu;

    /** Task to run */
    private final Runnable task;

    /** Feature name to add CPU usage to */
    private final String feature;

    /** Type of CPU usage of a feature */
    private final String usageType;

    @Override
    public void run() {
        try {
            long time = System.nanoTime();
            task.run();
            cpu.addTime(feature, usageType, System.nanoTime() - time);
        } catch (Exception | LinkageError | StackOverflowError e) {
            TAB.getInstance().getErrorManager().taskThrewError(e);
        }
    }
}
