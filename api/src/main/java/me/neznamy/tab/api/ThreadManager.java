package me.neznamy.tab.api;

import java.util.concurrent.Future;

public interface ThreadManager {

	/**
	 * Starts a task in new thread and measures how long it took to process
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> runMeasuredTask(String errorDescription, TabFeature feature, String type, Runnable task);
	
	/**
	 * Starts a task in new thread and measures how long it took to process
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> runMeasuredTask(String errorDescription, String feature, String type, Runnable task);
	
	/**
	 * Runs task in a new thread
	 * @param errorDescription - description to use if this task throws an error
	 * @param task - the task
	 */
	public Future<Void> runTask(String errorDescription, Runnable task);
	
	/**
	 * Starts a new task with defined repeat interval that measures cpu usage
	 * @param intervalMilliseconds - task interval
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, TabFeature feature, String type, Runnable task);
	
	/**
	 * Starts a new task with defined repeat interval that measures cpu usage
	 * @param intervalMilliseconds - task interval
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> startRepeatingMeasuredTask(int intervalMilliseconds, String errorDescription, String feature, String type, Runnable task);
	
	/**
	 * Runs task with a delay and measures how long it took to process
	 * @param delayMilliseconds - how long to run the task after
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> runTaskLater(int delayMilliseconds, String errorDescription, TabFeature feature, String type, Runnable task);
	
	/**
	 * Runs task with a delay and measures how long it took to process
	 * @param delayMilliseconds - how long to run the task after
	 * @param errorDescription - description to use if this task throws an error
	 * @param feature - feature to add cpu usage to
	 * @param type - usage type to add cpu usage to
	 * @param task - the task
	 */
	public Future<Void> runTaskLater(int delayMilliseconds, String errorDescription, String feature, String type, Runnable task);
}