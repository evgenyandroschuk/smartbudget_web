package com.ui.mvc.model;

import com.google.common.base.Objects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VehicleData {

    private Long id;
    private VehicleServiceType vehicleServiceType;
    private int vehicleId;
    private String description;
    private int mileAge;
    private double price;
    private LocalDate date;

    private VehicleData(Long id,  VehicleServiceType vehicleServiceType, int vehicleId, String description, int mileAge, double price, LocalDate date) {
        this.id = id;
        this.vehicleServiceType = vehicleServiceType;
        this.vehicleId = vehicleId;
        this.description = description;
        this.mileAge = mileAge;
        this.price = price;
        this.date = date;
    }

    public static VehicleData of (
            long id,
            VehicleServiceType vehicleServiceType,
            int vehicleId,
            String description,
            int mileAge,
            double price,
            LocalDate date
    ) {
        return new VehicleData(id, vehicleServiceType, vehicleId, description, mileAge, price, date);
    }

    public static VehicleData of (
            VehicleServiceType vehicleServiceType,
            int vehicleId,
            String description,
            int mileAge,
            double price,
            LocalDate date
    ) {
        return new VehicleData(null ,vehicleServiceType, vehicleId, description, mileAge, price, date);
    }

    public long getId() {
        return id;
    }

    public VehicleServiceType getVehicleServiceType() {
        return vehicleServiceType;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public int getMileAge() {
        return mileAge;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDateString() {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-YYYY"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleData that = (VehicleData) o;
        return id == that.id &&
                vehicleServiceType == that.vehicleServiceType &&
                vehicleId == that.vehicleId &&
                mileAge == that.mileAge &&
                Double.compare(that.price, price) == 0 &&
                Objects.equal(description, that.description) &&
                Objects.equal(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, vehicleServiceType, vehicleId, description, mileAge, price, date);
    }

    @Override
    public String toString() {
        return "VehicleData{" +
                "id=" + id +
                ", vehicleServiceType=" + vehicleServiceType +
                ", vehicleId=" + vehicleId +
                ", description='" + description + '\'' +
                ", mileAge=" + mileAge +
                ", price=" + price +
                ", date=" + date +
                '}';
    }
}
