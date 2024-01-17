package com.ryca.Profile;

public class Upload {

    private String imgdesc;
    private String prodprice;
    private String category;
    private String imageURL;
    private String date;

    public Upload() {
        // Default constructor, no initialization needed
    }

    public Upload(String imgdesc, String prodprice, String category, String imageURL, String date) {
        this.imgdesc = imgdesc;
        this.prodprice = prodprice;
        this.category = category;
        this.imageURL = imageURL;
        this.date = date;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
