package me.neznamy.tab.api.task;

public interface RepeatingTask {
	
	int getInterval();

	void setInterval(int paramInt);

	void cancel();

	void setRunnable(Runnable paramRunnable);
}
