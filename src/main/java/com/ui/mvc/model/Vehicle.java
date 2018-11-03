package com.ui.mvc.model;

import com.google.common.base.Objects;

public class Vehicle {

    private int id;
    private String description;
    private String licensePlate;
    private String vin;
    private String sts;

    private Vehicle(int id, String description, String licensePlate, String vin, String sts) {
        this.id = id;
        this.description = description;
        this.licensePlate = licensePlate;
        this.vin = vin;
        this.sts = sts;
    }

    public static Vehicle of (
            int id,
            String description,
            String licensePlate,
            String vin,
            String sts
    ) {
        return new Vehicle(id, description, licensePlate, vin, sts);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getVin() {
        return vin;
    }

    public String getSts() {
        return sts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return id == vehicle.id &&
                Objects.equal(description, vehicle.description) &&
                Objects.equal(licensePlate, vehicle.licensePlate) &&
                Objects.equal(vin, vehicle.vin) &&
                Objects.equal(sts, vehicle.sts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, description, licensePlate, vin, sts);
    }

}
