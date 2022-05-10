package me.neznamy.tab.api.chat;

import me.neznamy.tab.api.ProtocolVersion;

import java.util.List;

/**
 * A class extending IChatBaseComponent that holds the original value
 * to avoid deserializing it, which is an unnecessary operation that
 * slows down the performance, as well as makes code more complicated
 * and open to problems.
 */
public class WrappedChatComponent extends IChatBaseComponent {

    /** Original NMS component */
    private final Object originalComponent;

    /**
     * Constructs new instance with given parameter
     *
     * @param   originalComponent
     *          Original NMS component
     */
    public WrappedChatComponent(Object originalComponent) {
        this.originalComponent = originalComponent;
    }

    /**
     * Returns the original NMS component
     * @return  the original NMS component
     */
    public Object get() {
        return originalComponent;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public String toString(ProtocolVersion clientVersion) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public List<IChatBaseComponent> getExtra(){
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public ChatModifier getModifier() {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public void addExtra(IChatBaseComponent child) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }

    @Override
    public void setModifier(ChatModifier modifier) {
        throw new UnsupportedOperationException("Not supported for " + getClass().getSimpleName());
    }
}