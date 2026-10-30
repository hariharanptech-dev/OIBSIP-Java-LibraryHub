package com.reservation.model;

import java.time.LocalDate;

/** A confirmed ticket booking, identified by its PNR. */
public class Reservation {
    private final String pnr;
    private final String passengerName;
    private final int trainNumber;
    private final String trainName;
    private final String classType;
    private final LocalDate journeyDate;
    private final String source;
    private final String destination;
    private final String bookedBy;
    private final String bookedAt;

    /**
     * Creates a reservation.
     *
     * @param pnr           unique PNR
     * @param passengerName passenger's name
     * @param trainNumber   train number
     * @param trainName     train name
     * @param classType     travel class
     * @param journeyDate   date of journey
     * @param source        boarding station
     * @param destination   destination station
     * @param bookedBy      username that made the booking
     * @param bookedAt      booking timestamp as stored by the database (null before insertion)
     */
    public Reservation(String pnr, String passengerName, int trainNumber, String trainName,
                       String classType, LocalDate journeyDate, String source, String destination,
                       String bookedBy, String bookedAt) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.source = source;
        this.destination = destination;
        this.bookedBy = bookedBy;
        this.bookedAt = bookedAt;
    }

    /** @return the PNR */
    public String getPnr() { return pnr; }

    /** @return the passenger name */
    public String getPassengerName() { return passengerName; }

    /** @return the train number */
    public int getTrainNumber() { return trainNumber; }

    /** @return the train name */
    public String getTrainName() { return trainName; }

    /** @return the travel class */
    public String getClassType() { return classType; }

    /** @return the date of journey */
    public LocalDate getJourneyDate() { return journeyDate; }

    /** @return the source station */
    public String getSource() { return source; }

    /** @return the destination station */
    public String getDestination() { return destination; }

    /** @return the username that booked the ticket */
    public String getBookedBy() { return bookedBy; }

    /** @return the booking timestamp, or null if not yet stored */
    public String getBookedAt() { return bookedAt; }
}
