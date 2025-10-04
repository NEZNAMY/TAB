package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.ConfigurationSection;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Class representing layout configuration.
 */
@Getter
@RequiredArgsConstructor
public class LayoutConfiguration {

    @NotNull private final Direction direction;
    @NotNull private final String defaultSkin;
    private final boolean remainingPlayersTextEnabled;
    @NotNull private final String remainingPlayersText;
    private final int emptySlotPing;
    @NotNull private final Map<Integer, String> defaultSkinHashMap;
    @NotNull private final LinkedHashMap<String, LayoutDefinition> layouts;

    /**
     * Returns instance of this class created from given configuration section. If there are
     * issues in the configuration, console warns are printed.
     *
     * @param   section
     *          Configuration section to load from
     * @return  Loaded instance from given configuration section
     */
    @NotNull
    public static LayoutConfiguration fromSection(@NotNull ConfigurationSection section) {
        // Check keys
        section.checkForUnknownKey(Arrays.asList("enabled", "direction", "default-skin", "enable-remaining-players-text",
                        "remaining-players-text", "empty-slot-ping-value", "default-skins", "layouts"));

        // Check direction
        String directionString = section.getString("direction", "COLUMNS");
        Direction direction;
        try {
            direction = Direction.valueOf(directionString);
        } catch (IllegalArgumentException e) {
            section.startupWarn("\"" + directionString + "\" is not a valid type of layout direction. Valid options are: " +
                    Arrays.deepToString(Direction.values()) + ". Using COLUMNS");
            direction = Direction.COLUMNS;
        }

        // Load default skins
        ConfigurationSection defaultSkins = section.getConfigurationSection("default-skins");
        Map<Integer, String> defaultSkinHashMap = new HashMap<>();
        for (Object groupName : defaultSkins.getKeys()) {
            String asString = groupName.toString();
            ConfigurationSection groupSection = defaultSkins.getConfigurationSection(asString);

            String skin = groupSection.getString("skin");
            for (String line : section.getStringList("slots", Collections.emptyList())) {
                String[] arr = line.split("-");
                int from = Integer.parseInt(arr[0]);
                int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);
                for (int i = from; i<= to; i++) {
                    defaultSkinHashMap.put(i, skin);
                }
            }
        }

        // Load layouts
        ConfigurationSection layoutsSection = section.getConfigurationSection("layouts");
        LinkedHashMap<String, LayoutDefinition> layouts = new LinkedHashMap<>();
        for (Object bar : layoutsSection.getKeys()) {
            String asString = bar.toString();
            layouts.put(asString, LayoutDefinition.fromSection(asString, layoutsSection.getConfigurationSection(asString)));
        }

        return new LayoutConfiguration(
                direction,
                section.getString("default-skin", "mineskin:1753261242"),
                section.getBoolean("enable-remaining-players-text", true),
                EnumChatFormat.color(section.getString("remaining-players-text", "... and %s more")),
                section.getInt("empty-slot-ping-value", 1000),
                defaultSkinHashMap,
                layouts
        );
    }

    @NotNull
    public String getDefaultSkin(int slot) {
        return defaultSkinHashMap.getOrDefault(slot, defaultSkin);
    }

    @RequiredArgsConstructor
    public enum Direction {

        COLUMNS(slot -> slot),
        ROWS(slot -> (slot-1)%4*20+(slot-((slot-1)%4))/4+1);

        @NotNull private final Function<Integer, Integer> slotTranslator;

        public int translateSlot(int slot) {
            return slotTranslator.apply(slot);
        }

        public String getEntryName(@NotNull TabPlayer viewer, int slot, boolean teamsEnabled) {
            boolean legacySorting = viewer.getVersionId() < ProtocolVersion.V1_19_3.getNetworkId();
            boolean modernSorting = viewer.getVersionId() >= ProtocolVersion.V1_21_2.getNetworkId() && TAB.getInstance().getPlatform().supportsListOrder();
            if (legacySorting || modernSorting) {
                return "";
            } else {
                if (teamsEnabled) {
                    return "|slot_" + (10+slotTranslator.apply(slot));
                } else {
                    return " slot_" + (10+slotTranslator.apply(slot));
                }
            }
        }
    }

    /**
     * Class representing configuration of a specific layout.
     */
    @Getter
    @RequiredArgsConstructor
    public static class LayoutDefinition {

        @Nullable private final String condition;
        @Nullable private final String defaultSkin;
        @NotNull private final List<FixedSlotDefinition> fixedSlots;
        @NotNull private final LinkedHashMap<String, GroupPattern> groups;

        /**
         * Returns instance of this class created from given configuration section. If there are
         * issues in the configuration, console warns are printed.
         *
         * @param   name
         *          Layout name
         * @param   section
         *          Configuration section to load from
         * @return  Loaded instance from given configuration section
         */
        public static LayoutDefinition fromSection(@NotNull String name, @NotNull ConfigurationSection section) {
            // Check keys
            section.checkForUnknownKey(Arrays.asList("condition", "default-skin", "fixed-slots", "groups"));

            List<FixedSlotDefinition> fixedSlots = new ArrayList<>();
            for (String line : section.getStringList("fixed-slots", Collections.emptyList())) {
                FixedSlotDefinition def = FixedSlotDefinition.fromLine(line, name, section);
                if (def != null) fixedSlots.add(def);
            }

            ConfigurationSection groupsSection = section.getConfigurationSection("groups");
            LinkedHashMap<String, GroupPattern> groups = new LinkedHashMap<>();
            String noConditionGroup = null;
            Map<Integer, String> takenSlots = new HashMap<>();
            for (Object groupName : groupsSection.getKeys()) {
                String asString = groupName.toString();
                GroupPattern pattern = GroupPattern.fromSection(groupsSection.getConfigurationSection(asString), name, asString);

                // Checking for unreachable layout
                if (noConditionGroup != null) {
                    section.startupWarn("Layout \"" + name + "\"'s player group \"" + groupName + "\" is unreachable, " +
                            "because it is defined after group \"" + noConditionGroup + "\", which has no condition requirement.");
                } else if (pattern.condition == null) {
                    noConditionGroup = asString;
                }

                // Checking for duplicated slots
                for (int slot : pattern.slots) {
                    if (takenSlots.containsKey(slot)) {
                        section.startupWarn("Layout \"" + name + "\"'s player group \"" + pattern.name + "\" defines slot " +
                                slot + ", but this slot is already taken by group \"" + takenSlots.get(slot) + "\", which will take priority.");
                    } else {
                        takenSlots.put(slot, pattern.name);
                    }
                }

                groups.put(asString, pattern);
            }
            return new LayoutDefinition(
                    section.getString("condition"),
                    section.getString("default-skin"),
                    fixedSlots,
                    groups
            );
        }

        /**
         * Configuration of a fixed slot.
         */
        @Getter
        @RequiredArgsConstructor
        public static class FixedSlotDefinition {

            private final int slot;
            @NotNull private final String text;
            @Nullable private final String skin;
            @Nullable private final Integer ping;

            @Nullable
            private static FixedSlotDefinition fromLine(@NotNull String line, @NotNull String layoutName, @NotNull ConfigurationSection section) {
                String[] array = line.split("\\|");

                if (array.length < 2) {
                    section.startupWarn("Layout " + layoutName + " has invalid fixed slot defined as \"" + line + "\". " +
                            "Supported values are \"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, " +
                            "TEXT is displayed text and SKIN is skin used for the slot");
                    return null;
                }
                int slot;
                try {
                    slot = Integer.parseInt(array[0]);
                    if (slot < 1 || slot > 80) {
                        section.startupWarn("Layout " + layoutName + " has invalid fixed slot value \"" + slot + "\" defined. Slots must range between 1 - 80.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    section.startupWarn("Layout " + layoutName + " has invalid fixed slot defined as \"" + line + "\". " +
                            "Supported values are \"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, " +
                            "TEXT is displayed text and SKIN is skin used for the slot");
                    return null;
                }
                String skin = array.length > 2 ? array[2] : null;
                Integer ping = null;
                if (array.length > 3) {
                    try {
                        ping = (int) Math.round(Double.parseDouble(array[3]));
                    } catch (NumberFormatException ignored) {
                        section.startupWarn("Layout " + layoutName + " has fixed slot with defined ping \"" + array[3] + "\", which is not a valid number");
                    }
                }
                return new FixedSlotDefinition(slot, array[1], skin, ping);
            }

        }

        /**
         * Layout pattern for player groups displaying players if they meet a condition.
         */
        @Getter
        @RequiredArgsConstructor
        public static class GroupPattern {

            /** Name of this pattern */
            @NotNull private final String name;

            /** Condition players must meet to be displayed in this group */
            @Nullable private final String condition;

            /** Slots to display players in */
            private final int[] slots;

            @NotNull
            private static GroupPattern fromSection(@NotNull ConfigurationSection section, @NotNull String layout, @NotNull String groupName) {
                // Check keys
                section.checkForUnknownKey(Arrays.asList("condition", "slots"));

                List<Integer> positions = new ArrayList<>();
                for (String line : section.getStringList("slots", Collections.emptyList())) {
                    String[] arr = line.split("-");
                    int from = Integer.parseInt(arr[0]);
                    int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);
                    for (int i = from; i<= to; i++) {
                        if (i < 1 || i > 80) {
                            section.startupWarn("Layout " + layout + "'s player group \"" + groupName + "\" has invalid slot value \"" + i + "\" defined. Slots must range between 1 - 80.");
                            continue;
                        }
                        if (positions.contains(i)) {
                            section.startupWarn("Layout " + layout + "'s player group \"" + groupName + "\" has duplicated slot \"" + i + "\".");
                            continue;
                        }
                        positions.add(i);
                    }
                }
                String condition = section.getString("condition");
                return new GroupPattern(groupName, condition, positions.stream().mapToInt(i->i).toArray());
            }
        }
    }
}