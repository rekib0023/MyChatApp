package com.example.mychatapp;

public class Users {

    public String name;
    public String image;
    public String status;
    public  String thumb;

    public Users(){

    }

    public Users(String name, String image, String status, String thumb) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
