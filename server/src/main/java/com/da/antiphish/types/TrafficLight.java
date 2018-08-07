package com.da.antiphish.types;

/**
 * Enumeration class which represents a traffic light.
 * 
 * @author Marco Madritsch
 */
public enum TrafficLight {
	GRAY(0),
	GREEN(1),
	YELLOW(2),
	RED(3);
	
	private final int value;

	TrafficLight(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
