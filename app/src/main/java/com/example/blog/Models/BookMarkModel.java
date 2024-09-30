package com.example.blog.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BookMarkModel {

//    String BookMarkId;
    String userId;
    String articleId;
    String articleAuthor;
    String articleTitle;
    String articleImage;
    String articleDescription;
    String articleLikes;
    String articleCurrentDate;
    @ServerTimestamp
    Date timestamp;

    public BookMarkModel(){}

    public BookMarkModel( String userId, String articleId,
                         String articleAuthor, String articleTitle, String articleImage,
                         String articleDescription, String articleLikes, String articleCurrentDate) {
        this.userId = userId;
        this.articleId = articleId;
        this.articleAuthor = articleAuthor;
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

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getArticleAuthor() {
        return articleAuthor;
    }

    public void setArticleAuthor(String articleAuthor) {
        this.articleAuthor = articleAuthor;
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

    public String getArticleLikes() {
        return articleLikes;
    }

    public void setArticleLikes(String articleLikes) {
        this.articleLikes = articleLikes;
    }

    public String getArticleCurrentDate() {
        return articleCurrentDate;
    }

    public void setArticleCurrentDate(String articleCurrentDate) {
        this.articleCurrentDate = articleCurrentDate;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
