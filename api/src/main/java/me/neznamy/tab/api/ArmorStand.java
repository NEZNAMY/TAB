package me.neznamy.tab.api;

/**
 * Interface representing an armor stand attached to player
 * for displaying text in game.
 */
public interface ArmorStand {

    /**
     * Returns true if offset is static, false if not
     *
     * @return  true if static, false if dynamic
     */
    boolean isStaticOffset();
    
    /**
     * Changes offset of the armor stand
     *
     * @param   offset
     *          new offset
     */
    void setOffset(double offset);

    /**
     * Returns offset of the armor stand
     *
     * @return  offset from base
     */
    double getOffset();
    
    /**
     * Returns property for armor stand's name
     *
     * @return  property for armor stand's name
     */
    Property getProperty();

    /**
     * Updates armor stand's name if needed
     */
    void refresh();
    
    /**
     * Updates visibility if needed
     *
     * @param   force
     *          if refresh should be forced
     */
    void updateVisibility(boolean force);
    
    /**
     * Returns entity ID of this armor stand
     *
     * @return  entity ID of this armor stand
     */
    int getEntityId();
}
