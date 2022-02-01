package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.RepeatingTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Implementation of RepeatingTask interface which submits provided task
 * to TAB's processing thread in configured interval.
 */
public class TabRepeatingTask implements RepeatingTask {

	/** Executor service to submit the repeating task to */
	private final ExecutorService exe;

	/** Task to run periodically */
	private final Runnable runnable;

	/** Feature creating this task, used in cpu command output */
	private final TabFeature feature;

	/** Sub-feature creating this task, used in cpu command output */
	private final String type;

	/** Interval in milliseconds how often to submit the task to main thread */
	private int interval;

	/** Task pointer returned by executor service to allow this task to be cancelled */
	private Future<?> task;

	/**
	 * Constructs new instance with given parameters and starts the task.
	 *
	 * @param	exe
	 * 			Executor service to submit the repeating task to
	 * @param	runnable
	 *			Task to run periodically
	 * @param	feature
	 * 			Feature creating this task, used in cpu command output
	 * @param	type
	 * 			Sub-feature creating this task, used in cpu command output
	 * @param	interval
	 *			Interval in milliseconds how often to submit the task to main thread
	 */
	public TabRepeatingTask(ExecutorService exe, Runnable runnable, TabFeature feature, String type, int interval) {
		if (interval < 0) throw new IllegalArgumentException("Interval cannot be negative");
		this.exe = exe;
		this.runnable = runnable;
		this.feature = feature;
		this.type = type;
		this.interval = interval;
		createTask();
	}

	/**
	 * Creates the repeating task and submits it to executor service.
	 */
	private void createTask() {
		task = exe.submit(() -> {
			long nextLoop = System.currentTimeMillis();
			while (true) {
				try {
					nextLoop += interval;
					long sleep = nextLoop - System.currentTimeMillis();
					if (sleep > interval) sleep = interval; //servers who travel back in time
					if (sleep > 0) Thread.sleep(sleep);
					if (feature != null) {
						TAB.getInstance().getCPUManager().runMeasuredTask(feature, type, runnable);
					} else {
						TAB.getInstance().getCPUManager().runTask(runnable);
					}
				} catch (InterruptedException pluginDisabled) {
					Thread.currentThread().interrupt();
					break;
				}
			} 
		});
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public void setInterval(int interval) {
		if (interval < 0) throw new IllegalArgumentException("Interval cannot be negative");
		//restarting task to break sleep in case of interval reduction
		cancel();
		this.interval = interval;
		createTask();
	}

	@Override
	public void cancel() {
		task.cancel(true);
	}
}