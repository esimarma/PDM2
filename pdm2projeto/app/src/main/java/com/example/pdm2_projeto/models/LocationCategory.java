package com.example.pdm2_projeto.models;

/**
 * Model that represents location categories.
 * Each category is identified by an ID and contains descriptions in multiple languages.
 */
public class LocationCategory {

    /**
     * Unique identifier for the category.
     */
    private String id;

    /**
     * Description of the category in the default language.
     */
    private String description;

    /**
     * Description of the category in English.
     */
    private String descriptionEn;

    /**
     * Default constructor required for serialization and database operations.
     */
    public LocationCategory() {}

    /**
     * Constructs a new LocationCategory object with the given parameters.
     *
     * @param id Unique identifier of the category.
     * @param description Description of the category in the default language.
     * @param descriptionEn Description of the category in English.
     */
    public LocationCategory(String id, String description, String descriptionEn) {
        this.id = id;
        this.description = description;
        this.descriptionEn = descriptionEn;
    }

    /**
     * Retrieves the unique ID of the category.
     *
     * @return The category ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the category.
     *
     * @param id The category ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the description of the category in the default language.
     *
     * @return The category description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the category in the default language.
     *
     * @param description The category description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the description of the category in English.
     *
     * @return The English category description.
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }

    /**
     * Sets the description of the category in English.
     *
     * @param descriptionEn The English category description to set.
     */
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }
}
