package me.neznamy.tab.shared.cpu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Thread executor for accepting tasks to execute them in a single threaded executor.
 * All tasks are try/catch-ed and might track CPU usage if needed.
 */
public class ThreadExecutor {

    /** Timeout for finishing tasks when shutting down thread executor */
    private static final int SHUTDOWN_TIMEOUT = 2000;

    private final String threadName;
    private final ScheduledExecutorService executor;

    /**
     * Constructs new instance and starts new thread executor with give name.
     * 
     * @param   threadName
     *          Name of the created thread
     */
    public ThreadExecutor(@NotNull String threadName) {
        this.threadName = threadName;
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(threadName).build());
    }

    /**
     * Shuts down the executor.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                TAB.getInstance().getErrorManager().printError("Soft shutdown of thread " + threadName + " exceeded time limit of " + SHUTDOWN_TIMEOUT + "ms, forcing shutdown. This may cause issues.", null);
                executor.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            // Shutdown successful (?)
        }
    }

    public void execute(@NotNull Runnable task) {
        if (executor.isShutdown()) return;
        executor.execute(new CaughtTask(task));
    }

    public void execute(@NotNull TimedCaughtTask task) {
        if (executor.isShutdown()) return;
        executor.execute(task);
    }

    public ScheduledFuture<?> executeLater(@NotNull TimedCaughtTask task, long delayMillis) {
        if (executor.isShutdown()) return EmptyFuture.INSTANCE;
        return executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a repeating task.
     *
     * @param   task
     *          Task to run periodically
     * @param   intervalMilliseconds
     *          How often should the task run
     * @return
     */
    public ScheduledFuture<?> repeatTask(@NotNull TimedCaughtTask task, long intervalMilliseconds) {
        return repeatTask(task, intervalMilliseconds, intervalMilliseconds);
    }

    public ScheduledFuture<?> repeatTask(@NotNull TimedCaughtTask task, long initialMilliseconds, long intervalMilliseconds) {
        if (executor.isShutdown()) return EmptyFuture.INSTANCE;
        return executor.scheduleAtFixedRate(task, initialMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }
}
