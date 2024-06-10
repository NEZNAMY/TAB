package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.cpu.ThreadExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features that manage everything in their own thread.
 */
public interface CustomThreaded {

    /**
     * Returns the feature's custom thread to execute all tasks in.
     *
     * @return  feature's custom thread to execute all tasks in
     */
    @NotNull
    ThreadExecutor getCustomThread();
}
