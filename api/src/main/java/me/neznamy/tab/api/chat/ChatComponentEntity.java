package me.neznamy.tab.api.chat;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;

import java.util.UUID;

public class ChatComponentEntity extends IChatBaseComponent {

    private final String CONTENTS;
    private final String VALUE_1_13;
    private final String VALUE_1_12;
    private final String VALUE_1_8;

    public ChatComponentEntity(@NonNull String type, @NonNull UUID id, String name) {
        this.CONTENTS = String.format("{\"type\":\"%s\",\"id\":\"%s\",\"name\":{\"text\":\"%s\"}}", type, id, name);
        this.VALUE_1_13 = String.format("{type:\"%s\",id:\"%s\",name:\"{\\\"text\\\":\\\"%s\\\"}\"}", type, id, name);
        this.VALUE_1_12 = String.format("{type:\"%s\",id:\"%s\",name:\"%s\"}", type, id, name);
        this.VALUE_1_8 = String.format("{type:%s,id:%s,name:%s}", type, id, name);
    }

    @Override
    public String toRawText() {
        return toString();
    }
    
    @Override
    public String toString() {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 16) return CONTENTS;
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 13) return VALUE_1_13;
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() == 12) return VALUE_1_12;
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 8) return VALUE_1_8;
        throw new IllegalStateException("show_entity hover action is not supported on <1.8");
    }
}
