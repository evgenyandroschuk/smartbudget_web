package com.ui.mvc.model.property;

import com.google.common.base.Objects;

import java.time.LocalDate;

public class PropertyData {

    private long id;
    private PropertyType property;
    private PropertyType service;
    private String description;
    private String name;
    private double price;
    private String updateDate;

    public PropertyData(
            long id,
            PropertyType property,
            PropertyType service,
            String description,
            String name,
            double price,
            String updateDate
    ) {
        this.id = id;
        this.property = property;
        this.service = service;
        this.description = description;
        this.name = name;
        this.price = price;
        this.updateDate = updateDate;
    }

    public PropertyData(
            PropertyType property,
            PropertyType service,
            String description,
            String name,
            double price,
            String updateDate
    ) {
        this.property = property;
        this.service = service;
        this.description = description;
        this.name = name;
        this.price = price;
        this.updateDate = updateDate;
    }

    public long getId() {
        return id;
    }

    public PropertyType getProperty() {
        return property;
    }

    public PropertyType getService() {
        return service;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyData that = (PropertyData) o;
        return id == that.id &&
                Double.compare(that.price, price) == 0 &&
                Objects.equal(property, that.property) &&
                Objects.equal(service, that.service) &&
                Objects.equal(description, that.description) &&
                Objects.equal(name, that.name) &&
                Objects.equal(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, property, service, description, name, price, updateDate);
    }
}
