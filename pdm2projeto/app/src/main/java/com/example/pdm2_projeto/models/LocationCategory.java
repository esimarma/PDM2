package com.example.pdm2_projeto.models;

/**
 * Model that represents location categories.
 */
public class LocationCategory {
    private String id;
    private String description;

    public LocationCategory() {}

    public LocationCategory(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
