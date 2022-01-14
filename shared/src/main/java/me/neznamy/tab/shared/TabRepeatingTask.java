package me.neznamy.tab.shared;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.task.RepeatingTask;

public class TabRepeatingTask implements RepeatingTask {
	
	private Future<Void> task;
	private final ThreadPoolExecutor executor;
	private Runnable runnable;
	private final String errorDescription;
	private final TabFeature feature;
	private final String type;
	private int interval;

	public TabRepeatingTask(ThreadPoolExecutor executor, Runnable runnable, String errorDescription, TabFeature feature, String type, int interval) {
		if (interval < 0) throw new IllegalArgumentException("Interval cannot be negative");
		this.executor = executor;
		this.runnable = runnable;
		this.errorDescription = errorDescription;
		this.feature = feature;
		this.type = type;
		this.interval = interval;
		this.task = createTask();
	}
	
	@SuppressWarnings("unchecked")
	private Future<Void> createTask() {
		return (Future<Void>)executor.submit(() -> {
			long nextLoop = System.currentTimeMillis() - interval;
			while (true) {
				try {
					nextLoop += interval;
					long sleep = nextLoop - System.currentTimeMillis();
					if (sleep > interval) sleep = interval; //time travelers who travel back in time
					if (sleep > 0) Thread.sleep(sleep);
					long time = System.nanoTime();
					runnable.run();
					TAB.getInstance().getCPUManager().addTime(feature, type, System.nanoTime() - time);
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
		task = createTask();
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