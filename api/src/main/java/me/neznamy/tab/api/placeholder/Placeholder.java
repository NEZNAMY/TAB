package me.neznamy.tab.api.placeholder;

public interface Placeholder {

    int getRefresh();

    String getIdentifier();

    void enableTriggerMode(Runnable onActivation, Runnable onDisable);

    void unload();

    boolean isUsed();
}