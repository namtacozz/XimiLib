package com.ximilib.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * ImageSlideshow: Quản lý trạng thái logic của Slideshow trình chiếu ảnh.
 * Độc lập hoàn toàn với Swing, tuân thủ Clean Architecture (Domain Model).
 */
public class ImageSlideshow {

    public record Slide(String imageResourcePath, String title, String description) {
        public Slide {
            Objects.requireNonNull(imageResourcePath, "imageResourcePath không được null");
            Objects.requireNonNull(title, "title không được null");
            Objects.requireNonNull(description, "description không được null");
        }
    }

    private final List<Slide> slides;
    private int currentIndex = 0;
    private boolean paused = false;

    public ImageSlideshow(List<Slide> slides) {
        if (slides == null || slides.isEmpty()) {
            throw new IllegalArgumentException("slides không được null hoặc rỗng");
        }
        this.slides = List.copyOf(slides);
    }

    public List<Slide> getSlides() {
        return slides;
    }

    public int getSlideCount() {
        return slides.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Slide getCurrentSlide() {
        return slides.get(currentIndex);
    }

    public int next() {
        currentIndex = (currentIndex + 1) % slides.size();
        return currentIndex;
    }

    public int previous() {
        currentIndex = (currentIndex - 1 + slides.size()) % slides.size();
        return currentIndex;
    }

    public void goTo(int index) {
        if (index >= 0 && index < slides.size()) {
            this.currentIndex = index;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
