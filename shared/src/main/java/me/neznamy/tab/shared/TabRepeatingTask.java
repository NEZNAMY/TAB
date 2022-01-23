package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.RepeatingTask;

public class TabRepeatingTask implements RepeatingTask {
	
	private Thread thread;
	private Runnable runnable;
	private final String errorDescription;
	private final TabFeature feature;
	private final String type;
	private int interval;

	public TabRepeatingTask(Runnable runnable, String errorDescription, TabFeature feature, String type, int interval) {
		if (interval < 0) throw new IllegalArgumentException("Interval cannot be negative");
		this.runnable = runnable;
		this.errorDescription = errorDescription;
		this.feature = feature;
		this.type = type;
		this.interval = interval;
		createTask();
	}

	private void createTask() {
		thread = new Thread(() -> {
			long nextLoop = System.currentTimeMillis() - interval;
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
		thread.start();
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
		TAB.getInstance().getCPUManager().cancelTask(this);
	}

	public void interrupt() {
		thread.interrupt();
	}

	@Override
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}