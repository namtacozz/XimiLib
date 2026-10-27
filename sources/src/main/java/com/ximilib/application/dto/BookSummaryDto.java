package com.ximilib.application.dto;

import java.awt.Color;

/**
 * BookSummaryDto: DTO hiển thị từng dòng sách trên bảng "Kho sách" (BookCatalogTable).
 * Chứa đầy đủ thông tin theo ánh xạ cột DATABASE_SCHEMA.md.
 */
public class BookSummaryDto {

    private String bookId;           // sach.Masach (#0011 hoặc MS001)
    private String title;            // sach.Tensach
    private String author;           // tacgia.Tentacgia qua sach_tacgia
    private Integer categoryId;      // theloai.Matheloai
    private String categoryName;     // theloai.Tentheloai
    private String publisherName;    // nhaxuatban.tennxb
    private int availableCopies;     // cuonsach có Trangthaisach = 'KHA_DUNG'
    private int totalCopies;         // tổng số cuonsach của đầu sách này
    private Color coverColor;        // Màu đại diện cho ảnh bìa (đồng bộ phong cách Trang chủ)
    private String coverImage;       // Đường dẫn ảnh bìa (tương đối trong resources: /assets/covers/...)
    private boolean isDeleted;       // sach.IsDeleted (Soft-delete)
    private int activeLoansCount;    // Số cuốn đang mượn (phục vụ kiểm tra BR-09)

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
