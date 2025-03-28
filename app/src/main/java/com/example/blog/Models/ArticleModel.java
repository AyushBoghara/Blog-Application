package com.example.blog.Models;

public class ArticleModel {

    String userId;
    String userName;
    String userProfileImage;

    String articleId;
    String articleTitle;
    String articleImage;
    String articleDescription;
    private  int articleLikes;
    String articleCurrentDate;

    //Default Constructor
    public ArticleModel(){}

    //permiterized Constructor
    public ArticleModel(String userId, String userName, String userProfileImage,
                        String articleId, String articleTitle, String articleImage,
                        String articleDescription, int articleLikes, String articleCurrentDate) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.articleImage = articleImage;
        this.articleDescription = articleDescription;
        this.articleLikes = articleLikes;
        this.articleCurrentDate = articleCurrentDate;
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

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleImage() {
        return articleImage;
    }

    public void setArticleImage(String articleImage) {
        this.articleImage = articleImage;
    }

    public String getArticleDescription() {
        return articleDescription;
    }

    public void setArticleDescription(String articleDescription) {
        this.articleDescription = articleDescription;
    }

    public int getArticleLikes() {
        return articleLikes;
    }

    public void setArticleLikes(int articleLikes) {
        this.articleLikes = articleLikes;
    }

    public String getArticleCurrentDate() {
        return articleCurrentDate;
    }

    public void setArticleCurrentDate(String articleCurrentDate) {
        this.articleCurrentDate = articleCurrentDate;
    }
}
