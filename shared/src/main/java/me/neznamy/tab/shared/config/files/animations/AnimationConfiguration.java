package me.neznamy.tab.shared.config.files.animations;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimationConfiguration extends ConfigurationSection {

    @NotNull public final Map<String, AnimationDefinition> animations = new HashMap<>();

    public AnimationConfiguration(ConfigurationFile animationFile) {
        super(animationFile);
        for (Object animationName : getMap("", Collections.emptyMap()).keySet()) {
            checkForUnknownKey(animationName.toString(), Arrays.asList("change-interval", "texts"));
            animations.put(animationName.toString(), new AnimationDefinition(
                    fixAnimationInterval(animationName.toString(), getInt(animationName + ".change-interval")),
                    getStringList(animationName + ".texts", Collections.singletonList("<Animation does not have any texts>"))
            ));
        }
    }

    private int fixAnimationInterval(@NotNull String name, @Nullable Integer interval) {
        if (interval == null) {
            startupWarn(String.format("Animation \"%s\" does not define change-interval! Did you forget to configure it? Using 1000.", name));
            return 1000;
        }
        if (interval == 0) {
            startupWarn(String.format("Animation \"%s\" has refresh interval of 0 milliseconds! Using 1000.", name));
            return 1000;
        }
        if (interval < 0) {
            startupWarn(String.format("Animation \"%s\" has refresh interval of %s. Refresh cannot be negative! Using 1000.", name, interval));
            return 1000;
        }
        if (interval % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
            int min = TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL;
            int newInterval = Math.round((float) interval / min) * min; // rounding
            if (newInterval == 0) newInterval = min;
            startupWarn(String.format("Animation \"%s\" has refresh interval of %s, which is not divisible by %s! Using %s.",
                    name, interval, min, newInterval));
            return newInterval;
        }
        return interval;
    }

    @RequiredArgsConstructor
    public static class AnimationDefinition {

        public final int changeInterval;
        public final List<String> texts;
    }
}
