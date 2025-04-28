package me.neznamy.tab.shared.features.proxy;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.Broker;
import com.saicone.delivery4j.util.DelayedExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.chat.TextColor;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.EmptyFuture;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ProxyMessengerSupport extends ProxySupport {

    @NotNull
    private final String messengerName;

    @NotNull
    private final Supplier<Broker> brokerSupplier;

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Messenger Thread");

    @Nullable
    private AbstractMessenger messenger;

    @Override
    public void sendMessage(@NotNull String message) {
        if (messenger == null) return;
        messenger.send(TabConstants.PROXY_CHANNEL_NAME, message);
    }

    @Override
    public void register() {
        try {
            messenger = new Messenger(brokerSupplier.get());
            messenger.setExecutor(customThread::execute);
            messenger.subscribe(TabConstants.PROXY_CHANNEL_NAME).consume((channel, lines) -> processMessage(lines[0])).cache(true);
            messenger.start();
            TAB.getInstance().getPlatform().logInfo(new TextComponent("Successfully connected to " + messengerName, TextColor.GREEN));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to connect to " + messengerName + ": " + e.getClass().getName() + ": " + e.getMessage(), null);
        }
    }

    @Override
    public void unregister() {
        if (messenger == null) return;
        messenger.close();
        messenger.clear();
    }

    private class Messenger extends AbstractMessenger implements Broker.Logger, DelayedExecutor<ScheduledFuture<?>> {

        private final Broker broker;

        public Messenger(@NotNull Broker broker) {
            this.broker = broker;
        }

        @Override
        protected @NotNull Broker loadBroker() {
            return broker;
        }

        @Override
        public void log(int level, @NotNull String msg) {
            switch (level) {
                case 1:
                case 2:
                    TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text(msg));
                    break;
                case 3:
                    TAB.getInstance().getPlatform().logInfo(SimpleTextComponent.text(msg));
                    break;
                case 4:
                    TAB.getInstance().debug(msg);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void log(int level, @NotNull String msg, @NotNull Throwable throwable) {
            switch (level) {
                case 1:
                case 2:
                    TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text(msg));
                    for (String line : TAB.getInstance().getErrorManager().throwableToList(throwable, false)) {
                        TAB.getInstance().getPlatform().logWarn(SimpleTextComponent.text(line));
                    }
                    break;
                case 3:
                    TAB.getInstance().getPlatform().logInfo(SimpleTextComponent.text(msg));
                    for (String line : TAB.getInstance().getErrorManager().throwableToList(throwable, false)) {
                        TAB.getInstance().getPlatform().logInfo(SimpleTextComponent.text(line));
                    }
                    break;
                case 4:
                    TAB.getInstance().debug(msg);
                    if (TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                        for (String line : TAB.getInstance().getErrorManager().throwableToList(throwable, false)) {
                            TAB.getInstance().debug(line);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @NotNull
        private TimedCaughtTask task(@NotNull Runnable command) {
            return new TimedCaughtTask(TAB.getInstance().getCpu(), command, getFeatureName(), TabConstants.CpuUsageCategory.PROXY_MESSENGER);
        }

        @Override
        public @NotNull ScheduledFuture<?> execute(@NotNull Runnable command) {
            final TimedCaughtTask task = task(command);
            customThread.execute(task);
            return EmptyFuture.INSTANCE;
        }

        @Override
        public @NotNull ScheduledFuture<?> execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
            return customThread.executeLater(task(command), unit.toMillis(delay));
        }

        @Override
        public @NotNull ScheduledFuture<?> execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
            return customThread.repeatTask(task(command), unit.toMillis(delay), unit.toMillis(period));
        }

        @Override
        public void cancel(@NotNull ScheduledFuture<?> future) {
            future.cancel(true);
        }
    }
}
