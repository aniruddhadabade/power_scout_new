package com.example.powerscout;

import java.util.Objects;

public class Tip {
    private String title;
    private String author;
    private String imageUrl;
    private String description;
    private int thanks;
    private String articleLink;

    public Tip(String title, String author, String imageUrl, String description, int thanks, String articleLink) {
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.description = description;
        this.thanks = thanks;
        this.articleLink = articleLink;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public int getThanks() { return thanks; }
    public String getArticleLink() { return articleLink; }

    public void setThanks(int newThanks) {
        this.thanks = newThanks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return thanks == tip.thanks &&
                Objects.equals(title, tip.title) &&
                Objects.equals(author, tip.author) &&
                Objects.equals(imageUrl, tip.imageUrl) &&
                Objects.equals(description, tip.description) &&
                Objects.equals(articleLink, tip.articleLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, imageUrl, description, thanks, articleLink);
    }
}
