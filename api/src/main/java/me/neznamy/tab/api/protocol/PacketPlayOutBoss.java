package me.neznamy.tab.api.protocol;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;

import java.util.UUID;

/**
 * A class representing platform specific packet class
 */
@Data
public class PacketPlayOutBoss implements TabPacket {

    /** UUID of the BossBar */
    private final UUID id;

    /** Action of this packet */
    private Action action;

    /** BossBar title */
    private String name;

    /** BossBar progress (0-1)*/
    private float pct;

    /** BossBar color */
    private BarColor color;

    /** BossBar style */
    private BarStyle overlay;

    /** Darken screen flag */
    private boolean darkenScreen;

    /** Play music flag */
    private boolean playMusic;

    /** Create fog flag */
    private boolean createWorldFog;

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action}.ADD action
     *
     * @param   id
     *          BossBar uuid
     * @param   name
     *          BossBar title
     * @param   pct
     *          BossBar progress
     * @param   color
     *          BossBar color
     * @param   overlay
     *          BossBar style
     */
    public PacketPlayOutBoss(@NonNull UUID id, @NonNull String name, float pct, @NonNull BarColor color, @NonNull BarStyle overlay) {
        this.action = Action.ADD;
        this.id = id;
        this.name = name;
        this.pct = pct;
        this.color = color;
        this.overlay = overlay;
    }

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action#REMOVE} action
     *
     * @param   id
     *          BossBar uuid
     */
    public PacketPlayOutBoss(@NonNull UUID id) {
        this.action = Action.REMOVE;
        this.id = id;
    }

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action#UPDATE_PCT} action
     *
     * @param   id
     *          BossBar uuid
     * @param   pct
     *          BossBar progress
     */
    public PacketPlayOutBoss(@NonNull UUID id, float pct) {
        this.action = Action.UPDATE_PCT;
        this.id = id;
        this.pct = pct;
    }

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action#UPDATE_NAME} action
     *
     * @param   id
     *          BossBar uuid
     * @param   name
     *          BossBar title
     */
    public PacketPlayOutBoss(@NonNull UUID id, @NonNull String name) {
        this.action = Action.UPDATE_NAME;
        this.id = id;
        this.name = name;
    }

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action#UPDATE_STYLE} action
     *
     * @param   id
     *          BossBar uuid
     * @param   color
     *          BossBar color
     * @param   overlay
     *          BossBar style
     */
    public PacketPlayOutBoss(@NonNull UUID id, @NonNull BarColor color, @NonNull BarStyle overlay) {
        this.action = Action.UPDATE_STYLE;
        this.id = id;
        this.color = color;
        this.overlay = overlay;
    }

    /**
     * Constructs new instance with given parameters and
     * {@link me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action#UPDATE_PROPERTIES} action
     *
     * @param   darkenScreen
     *          Darken screen flag
     * @param   playMusic
     *          Play music flag
     * @param   createWorldFog
     *          Create fog flag
     */
    public PacketPlayOutBoss(@NonNull UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
        this.action = Action.UPDATE_PROPERTIES;
        this.id = id;
        this.darkenScreen = darkenScreen;
        this.playMusic = playMusic;
        this.createWorldFog = createWorldFog;
    }

    /**
     * Returns bitmask based on {@link #darkenScreen}, {@link #playMusic} and {@link #darkenScreen} values.
     * <p>
     * {@link #darkenScreen} adds {@code 1}, {@link #playMusic} {@code 2} and {@link #darkenScreen} {@code 4}
     * to the final value.
     *
     * @return  the bitmask
     */
    public byte getFlags() {
        byte value = 0;
        if (darkenScreen) value += 1;
        if (playMusic) value += 2;
        if (createWorldFog) value += 4;
        return value;
    }

    /**
     * An enum representing all valid boss packet actions.
     * Calling ordinal() will return action's network ID.
     */
    public enum Action {

        ADD,
        REMOVE,
        UPDATE_PCT,
        UPDATE_NAME,
        UPDATE_STYLE,
        UPDATE_PROPERTIES
    }
}