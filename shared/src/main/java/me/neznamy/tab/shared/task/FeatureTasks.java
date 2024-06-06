package me.neznamy.tab.shared.task;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;

public class FeatureTasks {

    @RequiredArgsConstructor
    public static class WorldSwitch implements Runnable {

        private final WorldSwitchListener listener;
        private final TabPlayer player;
        private final String from;
        private final String to;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onWorldChange(player, from, to);
            TAB.getInstance().getCpu().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
        }
    }

    @RequiredArgsConstructor
    public static class ServerSwitch implements Runnable {

        private final ServerSwitchListener listener;
        private final TabPlayer player;
        private final String from;
        private final String to;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onServerChange(player, from, to);
            TAB.getInstance().getCpu().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.SERVER_SWITCH, System.nanoTime()-time);
        }
    }

    @RequiredArgsConstructor
    public static class Refresh implements Runnable {

        private final RefreshableFeature listener;
        private final TabPlayer player;
        private final boolean force;

        @Override
        public void run() {
            long startTime = System.nanoTime();
            listener.refresh(player, force);
            TAB.getInstance().getCpu().addTime(listener.getFeatureName(), listener.getRefreshDisplayName(), System.nanoTime() - startTime);
        }
    }

    @RequiredArgsConstructor
    public static class Join implements Runnable {

        private final JoinListener listener;
        private final TabPlayer player;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onJoin(player);
            TAB.getInstance().getCPUManager().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
            TAB.getInstance().debug("Feature " + listener.getClass().getSimpleName() + " processed player join in " + (System.nanoTime()-time)/1000000 + "ms");
        }
    }

    @RequiredArgsConstructor
    public static class Quit implements Runnable {

        private final QuitListener listener;
        private final TabPlayer player;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onQuit(player);
            TAB.getInstance().getCPUManager().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.PLAYER_QUIT, System.nanoTime()-time);
        }
    }

    @RequiredArgsConstructor
    public static class VanishStatus implements Runnable {

        private final VanishListener listener;
        private final TabPlayer player;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onVanishStatusChange(player);
            TAB.getInstance().getCPUManager().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.VANISH_CHANGE, System.nanoTime()-time);
        }
    }

    @RequiredArgsConstructor
    public static class TabListClear implements Runnable {

        private final TabListClearListener listener;
        private final TabPlayer player;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onTabListClear(player);
            TAB.getInstance().getCPUManager().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.TABLIST_CLEAR, System.nanoTime() - time);
        }
    }

    @RequiredArgsConstructor
    public static class GameModeChange implements Runnable {

        private final GameModeListener listener;
        private final TabPlayer player;

        @Override
        public void run() {
            long time = System.nanoTime();
            listener.onGameModeChange(player);
            TAB.getInstance().getCPUManager().addTime(((TabFeature)listener).getFeatureName(), CpuUsageCategory.GAMEMODE_CHANGE, System.nanoTime() - time);
        }
    }

    @RequiredArgsConstructor
    public static class Load implements Runnable {

        private final Loadable listener;

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            listener.load();
            TAB.getInstance().debug("Feature " + listener.getClass().getSimpleName() + " processed load in " + (System.currentTimeMillis()-time) + "ms");
        }
    }

    @RequiredArgsConstructor
    public static class Unload implements Runnable {

        private final UnLoadable listener;

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            listener.unload();
            TAB.getInstance().debug("Feature " + listener.getClass().getSimpleName() + " processed unload in " + (System.currentTimeMillis()-time) + "ms");
        }
    }
}
