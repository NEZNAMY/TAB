package me.neznamy.tab.api.task;

import java.util.concurrent.Future;

import me.neznamy.tab.api.TabFeature;

/**
 * Manager for scheduling and executing tasks in TAB's
 * main thread to avoid concurrency issues.
 */
public interface ThreadManager {

    /**
     * Starts a task in new thread and measures how long it took to process
     *
     * @param   feature
     *          feature to add cpu usage to
     * @param   type
     *          usage type to add cpu usage to
     * @param   task
     *          the task
     */
    Future<Void> runMeasuredTask(TabFeature feature, String type, Runnable task);

    /**
     * Starts a task in new thread and measures how long it took to process
     *
     * @param   feature
     *          feature to add cpu usage to
     * @param   type
     *          usage type to add cpu usage to
     * @param   task
     *          the task
     */
    Future<Void> runMeasuredTask(String feature, String type, Runnable task);

    /**
     * Runs task in a new thread
     *
     * @param   task
     *          the task
     */
    Future<Void> runTask(Runnable task);

    /**
     * Starts a new task with defined repeat interval that measures cpu usage
     *
     * @param   intervalMilliseconds
     *          task interval
     * @param   feature
     *          feature to add cpu usage to
     * @param   type
     *          usage type to add cpu usage to
     * @param   task
     *          the task
     * @return  Future representing the submitted task
     */
    Future<?> startRepeatingMeasuredTask(int intervalMilliseconds, TabFeature feature, String type, Runnable task);

    /**
     * Starts a new task with defined repeat interval
     *
     * @param   intervalMilliseconds
     *          task interval
     * @param   task
     *          the task
     * @return  Future representing the submitted task
     */
    Future<?> startRepeatingTask(int intervalMilliseconds, Runnable task);

    /**
     * Runs task with a delay and measures how long it took to process
     *
     * @param   delayMilliseconds
     *          how long to run the task after
     * @param   feature
     *          feature to add cpu usage to
     * @param   type
     *          usage type to add cpu usage to
     * @param   task
     *          the task
     * @return  Future representing the submitted task
     */
    Future<?> runTaskLater(int delayMilliseconds, TabFeature feature, String type, Runnable task);

    /**
     * Runs task with a delay
     *
     * @param   delayMilliseconds
     *          how long to run the task after
     * @param   task
     *          the task
     * @return  Future representing the submitted task
     */
    Future<?> runTaskLater(int delayMilliseconds, Runnable task);
}