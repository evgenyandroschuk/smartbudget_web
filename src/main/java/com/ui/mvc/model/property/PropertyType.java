package com.ui.mvc.model.property;

import com.google.common.base.Objects;

public class PropertyType {

    private int id;
    private String description;

    public PropertyType(int id, String description) {
        this.id = id;
        this.description = description;
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
        PropertyType that = (PropertyType) o;
        return id == that.id &&
                Objects.equal(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, description);
    }
}
