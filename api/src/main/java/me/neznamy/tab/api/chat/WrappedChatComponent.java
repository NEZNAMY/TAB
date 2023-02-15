package me.neznamy.tab.api.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A class extending IChatBaseComponent that holds the original value
 * to avoid deserializing it, which is an unnecessary operation that
 * slows down the performance, as well as makes code more complicated
 * and open to problems.
 */
@AllArgsConstructor
public class WrappedChatComponent extends IChatBaseComponent {

    /** Original NMS component */
    @Getter private final Object originalComponent;

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public String toString(ProtocolVersion clientVersion) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public List<IChatBaseComponent> getExtra() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public @NotNull ChatModifier getModifier() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public IChatBaseComponent setExtra(List<IChatBaseComponent> components) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public void addExtra(@NonNull IChatBaseComponent child) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public void setModifier(@NotNull ChatModifier modifier) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }
}