package com.example.blog.Models;

public class UserModel {
    

    String userId;
    String userName;
    String email;
    String user_image;
    String password;

    public UserModel(){}

    public UserModel(String userId, String userName, String email, String user_image, String password) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.user_image = user_image;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
