package com.ryca;

public class PostModel {
    private String profilePictureUrl;
    private String username;
    private String address;
    private String postImageUrl;
    private String rating;
    private String category;
    private String description;
    private String userId;
    private String PostId;
    private boolean isSaved;

    public PostModel(String postImageUrl, String username, String address, String rating, String category, String description, String userId, String PostId, boolean isSaved) {
    }

    public PostModel(String profilePictureUrl, String username, String address, String postImageUrl, String rating, String category, String description, String userId, String PostId, boolean isSaved) {
        this.profilePictureUrl = profilePictureUrl;
        this.username = username;
        this.address = address;
        this.postImageUrl = postImageUrl;
        this.rating = rating;
        this.category = category;
        this.description = description;
        this.userId = userId;
        this.PostId = PostId;
        this.isSaved = isSaved;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = String.valueOf(rating);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Add getters and setters
}
