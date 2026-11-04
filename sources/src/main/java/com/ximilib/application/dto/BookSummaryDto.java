package com.ximilib.application.dto;

import java.awt.Color;

public class BookSummaryDto {

    private String bookId;
    private String title;
    private String author;
    private Integer categoryId;
    private String categoryName;
    private String publisherName;
    private int availableCopies;
    private int totalCopies;
    private Color coverColor;
    private String coverImage;
    private boolean isDeleted;
    private int activeLoansCount;
    private String description;
    private String isbn;

    public BookSummaryDto(String bookId, String title, String author, Integer categoryId,
                          String categoryName, String publisherName, int availableCopies,
                          int totalCopies, Color coverColor, boolean isDeleted, int activeLoansCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.publisherName = publisherName;
        this.availableCopies = availableCopies;
        this.totalCopies = totalCopies;
        this.coverColor = coverColor;
        this.isDeleted = isDeleted;
        this.activeLoansCount = activeLoansCount;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public Color getCoverColor() {
        return coverColor;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public int getActiveLoansCount() {
        return activeLoansCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setActiveLoansCount(int activeLoansCount) {
        this.activeLoansCount = activeLoansCount;
    }
}
