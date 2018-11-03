package com.ui.mvc.model;

import com.google.common.base.Objects;

public class VehicleServiceType {

    private int id;
    private String description;

    private VehicleServiceType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static VehicleServiceType of (int id, String description) {
        return new VehicleServiceType(id, description);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleServiceType that = (VehicleServiceType) o;
        return id == that.id &&
                Objects.equal(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, description);
    }

    @Override
    public String toString() {
        return "VehicleServiceType{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
