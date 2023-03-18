package me.neznamy.tab.shared.backend;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class Location {

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
}
