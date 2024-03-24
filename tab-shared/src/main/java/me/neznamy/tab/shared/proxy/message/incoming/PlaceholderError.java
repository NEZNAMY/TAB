package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderError implements IncomingMessage {

    private String message;
    private List<String> stack;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        message = in.readUTF();
        int count = in.readInt();
        stack = new ArrayList<>();
        for (int i=0; i<count; i++) {
            stack.add(in.readUTF());
        }
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        TAB.getInstance().getErrorManager().placeholderError(message, stack);
    }
}
