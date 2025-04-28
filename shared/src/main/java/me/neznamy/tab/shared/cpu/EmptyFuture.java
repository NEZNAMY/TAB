package me.neznamy.tab.shared.cpu;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EmptyFuture implements ScheduledFuture<Object> {

    public static final EmptyFuture INSTANCE = new EmptyFuture();

    EmptyFuture() {
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
