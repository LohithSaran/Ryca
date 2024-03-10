package com.ryca.Fragments;

import java.util.List;

public class SearchExhibitorList {
    private String shopName;
    private String shopDescription;
    private String profilePicture;
    private String city;
    private String location;
    private String userId;
    private List<String> imageUrls;

    public SearchExhibitorList(String shopName, String shopDescription, String profilePicture, String city, String location, String userId, List<String> imageUrls) {
        this.shopName = shopName;
        this.shopDescription = shopDescription;
        this.profilePicture = profilePicture;
        this.city = city;
        this.location = location;
        this.userId = userId;
        this.imageUrls = imageUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters
    public String getShopName() {
        return shopName;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getCity() {
        return city;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    // Setters
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
