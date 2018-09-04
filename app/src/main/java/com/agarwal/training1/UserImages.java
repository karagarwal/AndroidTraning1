package com.agarwal.training1;

public class UserImages {
    private  String ImageURL;


    public UserImages(String imageUrl) {
        ImageURL = imageUrl;
    }

    public  UserImages(){

    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
}
