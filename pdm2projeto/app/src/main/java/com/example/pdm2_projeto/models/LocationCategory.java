package com.example.pdm2_projeto.models;

/**
 * Model that represents location categories.
 */
public class LocationCategory {
    private String id;
    private String name;

    public LocationCategory() {}

    public LocationCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
