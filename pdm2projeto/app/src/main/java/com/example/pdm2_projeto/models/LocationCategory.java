package com.example.pdm2_projeto.models;

/**
 * Model that represents location categories.
 */
public class LocationCategory {
    private String id;
    private String description;
    private String descriptionEn;

    public LocationCategory() {}

    public LocationCategory(String id, String description, String descriptionEn) {
        this.id = id;
        this.description = description;
        this.descriptionEn = descriptionEn;
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

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }
}
