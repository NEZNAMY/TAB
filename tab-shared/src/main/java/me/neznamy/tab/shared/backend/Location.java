package me.neznamy.tab.shared.backend;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class representing a 3D location.
 */
@Data @AllArgsConstructor
public class Location {

    /** X position */
    private double x;

    /** Y position */
    private double y;

    /** Z position */
    private double z;
}
