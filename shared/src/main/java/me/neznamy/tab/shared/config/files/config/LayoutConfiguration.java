package me.neznamy.tab.shared.config.files.config;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.config.files.ConfigurationSection;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.FixedSlotDefinition;
import me.neznamy.tab.shared.config.files.config.LayoutConfiguration.LayoutDefinition.GroupPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class LayoutConfiguration extends ConfigurationSection {

    private final String SECTION = "layout";
    @NotNull public final Direction direction = parseDirection(getString(SECTION + ".direction", "COLUMNS"));
    @NotNull public final String defaultSkin = getString(SECTION + ".default-skin", "mineskin:1753261242");
    public final boolean remainingPlayersTextEnabled = getBoolean(SECTION + ".enable-remaining-players-text", true);
    @NotNull public final String remainingPlayersText = EnumChatFormat.color(getString(SECTION + ".remaining-players-text", "... and %s more"));
    public final int emptySlotPing = getInt(SECTION + ".empty-slot-ping-value", 1000);
    @NotNull public final Map<Integer, String> defaultSkinHashMap = new HashMap<>();
    @NotNull public final LinkedHashMap<String, LayoutDefinition> layouts = new LinkedHashMap<>();

    public LayoutConfiguration(@NotNull ConfigurationFile config) {
        super(config);
        checkForUnknownKey(SECTION, Arrays.asList("enabled", "direction", "default-skin", "enable-remaining-players-text",
                "remaining-players-text", "empty-slot-ping-value", "default-skins", "layouts"));

        Map<?, ?> defaultSkins = getMap(SECTION + ".default-skins");
        if (defaultSkins != null) {
            for (Object groupName : defaultSkins.keySet()) {
                String skin = getString(SECTION + ".default-skins." + groupName + ".skin");
                for (String line : getStringList(SECTION + ".default-skins." + groupName + ".slots", Collections.emptyList())) {
                    String[] arr = line.split("-");
                    int from = Integer.parseInt(arr[0]);
                    int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);
                    for (int i = from; i<= to; i++) {
                        defaultSkinHashMap.put(i, skin);
                    }
                }
            }
        }

        for (Object layoutName : getMap(SECTION + ".layouts", Collections.emptyMap()).keySet()) {
            checkForUnknownKey(new String[]{SECTION, "layouts", layoutName.toString()}, Arrays.asList("condition", "fixed-slots", "groups"));
            String condition = getString(new String[]{SECTION, "layouts", layoutName.toString(), "condition"});
            List<FixedSlotDefinition> fixedSlots = new ArrayList<>();
            for (String line : getStringList(new String[]{SECTION, "layouts", layoutName.toString(), "fixed-slots"}, Collections.emptyList())) {
                FixedSlotDefinition def = fixedSlotFromLine(layoutName.toString(), line);
                if (def != null) fixedSlots.add(def);
            }
            LinkedHashMap<String, GroupPattern> groups = new LinkedHashMap<>();
            String noConditionGroup = null;
            Map<Integer, String> takenSlots = new HashMap<>();
            for (Object groupName : getMap(new String[]{SECTION, "layouts", layoutName.toString(), "groups"}, Collections.emptyMap()).keySet()) {
                GroupPattern pattern = groupFromName(layoutName.toString(), groupName.toString());

                // Checking for unreachable layout
                if (noConditionGroup != null) {
                    startupWarn("Layout \"" + layoutName + "\"'s player group \"" + groupName + "\" is unreachable, " +
                            "because it is defined after group \"" + noConditionGroup + "\", which has no condition requirement.");
                } else if (pattern.condition == null) {
                    noConditionGroup = groupName.toString();
                }

                // Checking for duplicated slots
                for (int slot : pattern.slots) {
                    if (takenSlots.containsKey(slot)) {
                        startupWarn("Layout \"" + layoutName + "\"'s player group \"" + pattern.name + "\" defines slot " +
                                slot + ", but this slot is already taken by group \"" + takenSlots.get(slot) + "\", which will take priority.");
                    } else {
                        takenSlots.put(slot, pattern.name);
                    }
                }

                groups.put(groupName.toString(), pattern);
            }
            layouts.put(layoutName.toString(), new LayoutDefinition(condition, fixedSlots, groups));
        }
    }

    @NotNull
    public String getDefaultSkin(int slot) {
        return defaultSkinHashMap.getOrDefault(slot, defaultSkin);
    }

    @NotNull
    private Direction parseDirection(@NotNull String value) {
        try {
            return Direction.valueOf(value);
        } catch (IllegalArgumentException e) {
            startupWarn("\"" + direction + "\" is not a valid type of layout direction. Valid options are: " +
                    Arrays.deepToString(Direction.values()) + ". Using COLUMNS");
            return Direction.COLUMNS;
        }
    }

    @Nullable
    private FixedSlotDefinition fixedSlotFromLine(@NotNull String layoutName, @NotNull String line) {
        String[] array = line.split("\\|");
        if (array.length < 2) {
            startupWarn("Layout " + layoutName + " has invalid fixed slot defined as \"" + line + "\". Supported values are " +
                    "\"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, TEXT is displayed text and SKIN is skin used for the slot");
            return null;
        }
        int slot;
        try {
            slot = Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            startupWarn("Layout " + layoutName + " has invalid fixed slot defined as \"" + line + "\". Supported values are " +
                    "\"SLOT|TEXT\" and \"SLOT|TEXT|SKIN\", where SLOT is a number from 1 to 80, TEXT is displayed text and SKIN is skin used for the slot");

            return null;
        }
        String skin = array.length > 2 ? array[2] : null;
        Integer ping = null;
        if (array.length > 3) {
            try {
                ping = (int) Math.round(Double.parseDouble(array[3]));
            } catch (NumberFormatException ignored) {
                startupWarn("Layout " + layoutName + " has fixed slot with defined ping \"" + array[3] + "\", which is not a valid number");
            }
        }
        return new FixedSlotDefinition(slot, array[1], skin, ping);
    }

    @NotNull
    private GroupPattern groupFromName(@NotNull String layout, @NotNull String groupName) {
        checkForUnknownKey(new String[]{SECTION, "layouts", layout, "groups", groupName}, Arrays.asList("condition", "slots"));
        List<Integer> positions = new ArrayList<>();
        for (String line : getStringList(new String[]{SECTION, "layouts", layout, "groups", groupName, "slots"}, Collections.emptyList())) {
            String[] arr = line.split("-");
            int from = Integer.parseInt(arr[0]);
            int to = arr.length == 1 ? from : Integer.parseInt(arr[1]);
            for (int i = from; i<= to; i++) {
                positions.add(i);
            }
        }
        String condition = getString(new String[]{SECTION, "layouts", layout, "groups", groupName, "condition"});
        return new GroupPattern(groupName, condition, positions.stream().mapToInt(i->i).toArray());
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
            boolean v1_21_2Plus = viewer.getVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId() &&
                    TAB.getInstance().getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId();
            if (viewer.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() && !v1_21_2Plus) {
                if (teamsEnabled) {
                    return "|slot_" + (10+slotTranslator.apply(slot));
                } else {
                    return " slot_" + (10+slotTranslator.apply(slot));
                }
            } else {
                return "";
            }
        }
    }

    @RequiredArgsConstructor
    public static class LayoutDefinition {

        @Nullable public final String condition;
        @NotNull public final List<FixedSlotDefinition> fixedSlots;
        @NotNull public final LinkedHashMap<String, GroupPattern> groups;

        @RequiredArgsConstructor
        public static class FixedSlotDefinition {

            public final int slot;
            @NotNull public final String text;
            @Nullable public final String skin;
            @Nullable public final Integer ping;
        }

        /**
         * Layout pattern for player groups displaying players if they meet a condition
         */
        @RequiredArgsConstructor
        public static class GroupPattern {

            /** Name of this pattern */
            @NotNull public final String name;

            /** Condition players must meet to be displayed in this group */
            @Nullable public final String condition;

            /** Slots to display players in */
            public final int[] slots;
        }
    }
}