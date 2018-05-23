package com.muustar.feco.mychat;

public class User {
    private String name, status, image, image_thumb, email, uid;
    private Boolean email_visible;

    public User() {
    }

    public User(String name, String status, String image, String image_thumb, String email, String uid, Boolean email_visible) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.image_thumb = image_thumb;
        this.email = email;
        this.uid = uid;
        this.email_visible = email_visible;
    }

    public Boolean getEmail_visible() {
        return email_visible;
    }

    public void setEmail_visible(Boolean email_visible) {
        this.email_visible = email_visible;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
