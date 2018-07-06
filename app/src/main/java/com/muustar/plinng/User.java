package com.muustar.plinng;

public class User {
    private String chat_window_open = null;
    private String email;
    private Boolean email_visible;
    private String image;
    private String image_thumb;
    private String name;
    private String online = null;
    private String status;
    private String uid;
    private int version = 0;

    public User() {
    }

    public User(String name, String status, String image, String image_thumb, String email,
                String uid, Boolean email_visible) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.image_thumb = image_thumb;
        this.email = email;
        this.uid = uid;
        this.email_visible = email_visible;
    }

    public User(String name, String status, String image, String image_thumb, String email, String uid, Boolean email_visible, int version) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.image_thumb = image_thumb;
        this.email = email;
        this.uid = uid;
        this.email_visible = email_visible;
        this.version = version;
    }

    public int getVersion() {
        return version;
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
