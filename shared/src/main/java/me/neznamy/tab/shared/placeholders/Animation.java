package me.neznamy.tab.shared.placeholders;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing an animation from animations.yml
 */
public class Animation {

    /** Placeholder manager for fast access */
    private final PlaceholderManagerImpl placeholderManager;

    /** Animation's name defined in configuration */
    @Getter private final String name;
    
    /** All defined frames of the animation */
    private final String[] messages;
    
    /** Configured change interval for animation to jump to the next frame */
    private final int interval;

    /**
     * Refresh interval of placeholder created from this animation.
     * It may be lower than change interval due to nested placeholders,
     * which may need to refresh faster for any reason,
     * such as this being a slow animation with a fast nested animation.
     */
    @Getter private final int refresh;
    
    /** All nested placeholders used in all frames, preloading for
     * better performance since they can be detected immediately and
     * don't change at runtime.
     */
    @Getter private final String[] nestedPlaceholders;
    
    /**
     * Constructs new instance with given arguments which are fixed if necessary, such as when
     * refresh is not divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *
     * @param   placeholderManager
     *          Placeholder manager
     * @param   name
     *          animation's name
     * @param   list
     *          list of animation frames
     * @param   interval
     *          change interval to next frame
     */
    public Animation(@NotNull PlaceholderManagerImpl placeholderManager, @NonNull String name, @Nullable List<String> list, int interval) {
        this.placeholderManager = placeholderManager;
        this.name = name;
        messages = TAB.getInstance().getConfigHelper().startup().fixAnimationFrames(name, list).toArray(new String[0]);
        this.interval = TAB.getInstance().getConfigHelper().startup().fixAnimationInterval(name, interval);
        int refresh = this.interval;
        List<String> nestedPlaceholders0 = new ArrayList<>();
        for (int i=0; i<messages.length; i++) {
            messages[i] = RGBUtils.getInstance().applyCleanGradients(messages[i]);
            messages[i] = EnumChatFormat.color(messages[i]);
            nestedPlaceholders0.addAll(placeholderManager.detectPlaceholders(messages[i]));
        }
        for (String placeholder : nestedPlaceholders0) {
            int localRefresh;
            if (placeholder.startsWith("%animation:")) {
                //nested animations may not be loaded into the system yet due to load order, manually getting the refresh interval
                String nestedAnimation = placeholder.substring("%animation:".length(), placeholder.length()-1);
                localRefresh = TAB.getInstance().getConfiguration().getAnimationFile().hasConfigOption(nestedAnimation + ".change-interval") ?
                        TAB.getInstance().getConfiguration().getAnimationFile().getInt(nestedAnimation + ".change-interval") : this.interval;
            } else {
                localRefresh = placeholderManager.getPlaceholder(placeholder).getRefresh();
            }
            if (localRefresh != -1 && localRefresh < refresh) {
                refresh = localRefresh;
            }
        }
        this.refresh = refresh;
        placeholderManager.addUsedPlaceholders(nestedPlaceholders0);
        nestedPlaceholders = nestedPlaceholders0.toArray(new String[0]);
    }

    /**
     * Returns current up-to-date message depending on current system time
     *
     * @return  current message
     */
    public @NotNull String getMessage() {
        return messages[(((placeholderManager.getLoopTime())%(messages.length*interval))/interval)];
    }
}