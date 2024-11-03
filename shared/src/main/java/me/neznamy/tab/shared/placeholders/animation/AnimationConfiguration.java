package me.neznamy.tab.shared.placeholders.animation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Class storing configured animations section.
 */
@Getter
@RequiredArgsConstructor
public class AnimationConfiguration {

    @NotNull private final Map<String, AnimationDefinition> animations;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static AnimationConfiguration fromSection(@NotNull ConfigurationSection section) {
        Map<String, AnimationDefinition> animations = new HashMap<>();
        for (Object animationName : section.getKeys()) {
            animations.put(animationName.toString(), AnimationDefinition.fromSection(
                    section.getConfigurationSection(animationName.toString()), 
                    animationName.toString()
            ));
        }
        return new AnimationConfiguration(animations);
    }

    /**
     * Structure holding animation settings.
     */
    @RequiredArgsConstructor
    @Getter
    public static class AnimationDefinition {

        private final int changeInterval;
        private final List<String> texts;

        /**
         * Returns instance of this class created from given configuration section. If there are
         * issues in the configuration, console warns are printed.
         *
         * @param   section
         *          Configuration section to load from
         * @param   name
         *          Name of this animation
         * @return  Loaded instance from given configuration section
         */
        @NotNull
        public static AnimationDefinition fromSection(@NotNull ConfigurationSection section, @NotNull String name) {
            // Check keys
            section.checkForUnknownKey(Arrays.asList("change-interval", "texts"));
            
            return new AnimationDefinition(
                    fixAnimationInterval(name, section.getInt("change-interval"), section),
                    section.getStringList("texts", Collections.singletonList("<Animation does not have any texts>"))
            );
        }

        private static int fixAnimationInterval(@NotNull String name, @Nullable Integer interval, @NotNull ConfigurationSection section) {
            if (interval == null) {
                section.startupWarn(String.format("Animation \"%s\" does not define change-interval! Did you forget to configure it? Using 1000.", name));
                return 1000;
            }
            if (interval == 0) {
                section.startupWarn(String.format("Animation \"%s\" has refresh interval of 0 milliseconds! Using 1000.", name));
                return 1000;
            }
            if (interval < 0) {
                section.startupWarn(String.format("Animation \"%s\" has refresh interval of %s. Refresh cannot be negative! Using 1000.", name, interval));
                return 1000;
            }
            if (interval % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0) {
                int min = TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL;
                int newInterval = Math.round((float) interval / min) * min; // rounding
                if (newInterval == 0) newInterval = min;
                section.startupWarn(String.format("Animation \"%s\" has refresh interval of %s, which is not divisible by %s! Using %s.",
                        name, interval, min, newInterval));
                return newInterval;
            }
            return interval;
        }
    }
}
