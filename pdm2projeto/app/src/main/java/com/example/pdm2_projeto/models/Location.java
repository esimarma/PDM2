package com.example.pdm2_projeto.models;

/**
 * Model that represents a Location.
 * This class stores information about a location including its name, description, coordinates, and category.
 */
public class Location {

    /**
     * Unique identifier for the location.
     */
    private String id;

    /**
     * Name of the location in the default language.
     */
    private String name;

    /**
     * Name of the location in English.
     */
    private String nameEn;

    /**
     * Description of the location in the default language.
     */
    private String description;

    /**
     * Description of the location in English.
     */
    private String descriptionEn;

    /**
     * Address of the location.
     */
    private String address;

    /**
     * Geographical latitude of the location.
     */
    private double latitude;

    /**
     * Geographical longitude of the location.
     */
    private double longitude;

    /**
     * Category ID that classifies the location.
     */
    private String categoryId;

    /**
     * URL of the image representing the location.
     */
    private String imageUrl;

    /**
     * Country where the location is situated.
     */
    private String country;

    /**
     * Country name in English.
     */
    private String countryEn;

    /**
     * Default constructor required for serialization.
     */
    public Location() {}

    /**
     * Constructs a new Location object with the given parameters.
     *
     * @param id Unique identifier of the location.
     * @param name Name of the location in the default language.
     * @param nameEn Name of the location in English.
     * @param description Description of the location in the default language.
     * @param descriptionEn Description of the location in English.
     * @param address Address of the location.
     * @param latitude Geographical latitude.
     * @param longitude Geographical longitude.
     * @param categoryId ID of the category the location belongs to.
     * @param imageUrl URL of the location's image.
     * @param country Country where the location is situated.
     * @param countryEn Country name in English.
     */
    public Location(String id, String name, String nameEn, String description, String descriptionEn, String address, double latitude,
                    double longitude, String categoryId, String imageUrl, String country, String countryEn) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.description = description;
        this.descriptionEn = descriptionEn;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.country = country;
        this.countryEn = countryEn;
    }

    /**
     * Retrieves the unique identifier of the location.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the location.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the name of the location in the default language.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the location in the default language.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the location in English.
     */
    public String getNameEn() {
        return nameEn;
    }

    /**
     * Sets the name of the location in English.
     */
    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    /**
     * Retrieves the description of the location in the default language.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the location in the default language.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the description of the location in English.
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }

    /**
     * Sets the description of the location in English.
     */
    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    /**
     * Retrieves the address of the location.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the location.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Retrieves the latitude coordinate of the location.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinate of the location.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Retrieves the longitude coordinate of the location.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinate of the location.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Retrieves the category ID associated with the location.
     *
     * @return The category ID.
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category ID for the location.
     *
     * @param categoryId The category ID to set.
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Retrieves the URL of the location's image.
     *
     * @return The image URL.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of the location's image.
     *
     * @param imageUrl The image URL to set.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Retrieves the country where the location is situated.
     *
     * @return The country name.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country where the location is situated.
     *
     * @param country The country name to set.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Retrieves the country name in English.
     *
     * @return The English country name.
     */
    public String getCountryEn() {
        return countryEn;
    }

    /**
     * Sets the country name in English.
     *
     * @param countryEn The English country name to set.
     */
    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
    }
}
