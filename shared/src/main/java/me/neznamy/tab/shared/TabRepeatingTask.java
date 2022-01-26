package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.RepeatingTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TabRepeatingTask implements RepeatingTask {
	
	private final ExecutorService exe;
	private Runnable runnable;
	private final String errorDescription;
	private final TabFeature feature;
	private final String type;
	private int interval;
	private Future<?> task;

	public TabRepeatingTask(ExecutorService exe, Runnable runnable, String errorDescription, TabFeature feature, String type, int interval) {
		if (interval < 0) throw new IllegalArgumentException("Interval cannot be negative");
		this.exe = exe;
		this.runnable = runnable;
		this.errorDescription = errorDescription;
		this.feature = feature;
		this.type = type;
		this.interval = interval;
		createTask();
	}

	private void createTask() {
		task = exe.submit(() -> {
			long nextLoop = System.currentTimeMillis();
			while (true) {
				try {
					nextLoop += interval;
					long sleep = nextLoop - System.currentTimeMillis();
					if (sleep > interval) sleep = interval; //servers who travel back in time
					if (sleep > 0) Thread.sleep(sleep);
					TAB.getInstance().getCPUManager().runMeasuredTask(errorDescription, feature, type, runnable);
				} catch (InterruptedException pluginDisabled) {
					Thread.currentThread().interrupt();
					break;
				} catch (Exception | LinkageError e) {
					TAB.getInstance().getErrorManager().printError("An error occurred when " + errorDescription, e);
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
		cancel();
		this.interval = interval;
		createTask();
	}

	@Override
	public void cancel() {
		task.cancel(true);
	}

	@Override
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}