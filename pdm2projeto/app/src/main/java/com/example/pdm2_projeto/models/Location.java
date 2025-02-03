package com.example.pdm2_projeto.models;

/**
 * Model that represents Locations
 */
public class Location {
    private String id;
    private String name;
    private String nameEn;
    private String description;
    private String descriptionEn;
    private String address;
    private double latitude;
    private double longitude;
    private String categoryId;
    private String imageUrl;
    private String country;
    private String countryEn;

    public Location() {}

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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryEn() {
        return countryEn;
    }

    public void setCountryEn(String countryEn) {
        this.countryEn = countryEn;
    }
}
