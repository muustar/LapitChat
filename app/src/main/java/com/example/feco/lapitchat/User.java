package com.example.feco.lapitchat;

public class User {
    private String name, status, image, image_thumb;

    public User(){}

    public User(String name, String status, String image, String image_thumb) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.image_thumb = image_thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }
}
