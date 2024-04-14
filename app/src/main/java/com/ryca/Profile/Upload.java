package com.ryca.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Upload {

    private String imgdesc;
    private String prodprice;
    private String category;
    private Map<String, String> itemUrls;
    private String date;

    public Upload() {
        // Default constructor, no initialization needed
    }


    public Upload(String imgdesc, String prodprice, String category, String date) {
        this.imgdesc = imgdesc;
        this.prodprice = prodprice;
        this.category = category;
        this.itemUrls = new HashMap<>();
        this.date = date;
    }

    public Upload(String imgdesc, String prodprice, String category, String imageUrl, String date) {
        this.imgdesc = imgdesc;
        this.prodprice = prodprice;
        this.category = category;
        this.itemUrls = new HashMap<>();
        itemUrls.put("default_child_key", imageUrl);
        this.date = date;
    }

    public Map<String, String> getItemUrls() {
        return itemUrls;
    }

    public void setItemUrls(List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            addImageUrl("link_of_image" + (i + 1), imageUrls.get(i));
        }
    }

    public String getImgdesc() {
        return imgdesc;
    }

    public void setImgdesc(String imgdesc) {
        this.imgdesc = imgdesc;
    }

    public String getProdprice() {
        return prodprice;
    }

    public void setProdprice(String prodprice) {
        this.prodprice = prodprice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void addImageUrl(String childKey, String imageUrl) {
        itemUrls.put(childKey, imageUrl);
    }
}
