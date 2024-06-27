package me.neznamy.tab.shared.cpu;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread executor for accepting tasks to execute them in a single threaded executor.
 * All tasks are try/catch-ed and might track CPU usage if needed.
 */
public class ThreadExecutor {

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
    @SneakyThrows
    public void shutdown() {
        executor.shutdown();
        if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            TAB.getInstance().getErrorManager().printError("Shutdown of thread " + threadName + " exceeded time limit of 500ms", null);
        }
    }
    
    @NotNull
    private CpuManager getCpu() {
        return TAB.getInstance().getCpu();
    }

    public void execute(@NotNull Runnable task, @NotNull String feature, @NotNull String type) {
        if (executor.isShutdown()) return;
        if (!getCpu().isTrackUsage()) {
            executor.execute(new CaughtTask(task));
            return;
        }
        executor.execute(new TimedCaughtTask(getCpu(), task, feature, type));
    }

    public void execute(@NotNull Runnable task) {
        if (executor.isShutdown()) return;
        executor.execute(new CaughtTask(task));
    }

    public void executeLater(@NotNull Runnable task, @NotNull String feature, @NotNull String type, int delayMillis) {
        if (executor.isShutdown()) return;
        if (!getCpu().isTrackUsage()) {
            executor.schedule(new CaughtTask(task), delayMillis, TimeUnit.MILLISECONDS);
            return;
        }
        executor.schedule(new TimedCaughtTask(getCpu(), task, feature, type), delayMillis, TimeUnit.MILLISECONDS);
    }

    public void executeLater(@NotNull Runnable task, int delayMillis) {
        if (executor.isShutdown()) return;
        executor.schedule(new CaughtTask(task), delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a repeating task that measures how long it takes.
     *
     * @param   task
     *          Task to run periodically
     * @param   feature
     *          Feature executing the task
     * @param   type
     *          Usage the of the feature
     * @param   intervalMilliseconds
     *          How often should the task run
     */
    public void repeatTask(@NotNull Runnable task, @NotNull String feature, @NotNull String type, int intervalMilliseconds) {
        if (executor.isShutdown()) return;
        if (!getCpu().isTrackUsage()) {
            executor.scheduleAtFixedRate(new CaughtTask(task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
            return;
        }
        executor.scheduleAtFixedRate(new TimedCaughtTask(getCpu(), task, feature, type), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a repeating task.
     *
     * @param   task
     *          Task to run periodically
     * @param   intervalMilliseconds
     *          How often should the task run
     */
    public void repeatTask(@NotNull Runnable task, int intervalMilliseconds) {
        if (executor.isShutdown()) return;
        executor.scheduleAtFixedRate(new CaughtTask(task), intervalMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }
}
