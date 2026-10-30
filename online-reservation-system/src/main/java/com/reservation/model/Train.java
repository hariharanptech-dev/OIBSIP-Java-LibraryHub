package com.reservation.model;

/** A train that can be booked, identified by its number. */
public class Train {
    private final int number;
    private final String name;

    /**
     * Creates a train.
     *
     * @param number train number
     * @param name   train name
     */
    public Train(int number, String name) {
        this.number = number;
        this.name = name;
    }

    /** @return the train number */
    public int getNumber() {
        return number;
    }

    /** @return the train name */
    public String getName() {
        return name;
    }
}
