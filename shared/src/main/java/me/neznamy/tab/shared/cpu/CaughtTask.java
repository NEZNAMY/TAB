package me.neznamy.tab.shared.cpu;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;

/**
 * Runnable wrapper that try/catches the task.
 */
@RequiredArgsConstructor
public class CaughtTask implements Runnable {

    /** Task to run */
    private final Runnable task;

    @Override
    public void run() {
        try {
            task.run();
        } catch (Exception | LinkageError | StackOverflowError e) {
            TAB.getInstance().getErrorManager().taskThrewError(e);
        }
    }
}
