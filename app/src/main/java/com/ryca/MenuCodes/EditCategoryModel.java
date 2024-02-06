package com.ryca.MenuCodes;

public class EditCategoryModel {
    private String categoryName;

    public EditCategoryModel() {
    }

    public EditCategoryModel(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
