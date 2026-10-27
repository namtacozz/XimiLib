package com.ximilib.domain.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ImageSlideshowTest {

    @Test
    public void testSlideshowNavigation() {
        ImageSlideshow.Slide s1 = new ImageSlideshow.Slide("/path1", "Title 1", "Desc 1");
        ImageSlideshow.Slide s2 = new ImageSlideshow.Slide("/path2", "Title 2", "Desc 2");
        ImageSlideshow slideshow = new ImageSlideshow(List.of(s1, s2));

        assertEquals(2, slideshow.getSlideCount());
        assertEquals(0, slideshow.getCurrentIndex());
        assertEquals(s1, slideshow.getCurrentSlide());

        // Next wraps around
        assertEquals(1, slideshow.next());
        assertEquals(s2, slideshow.getCurrentSlide());
        assertEquals(0, slideshow.next());
        assertEquals(s1, slideshow.getCurrentSlide());

        // Previous wraps around
        assertEquals(1, slideshow.previous());
        assertEquals(s2, slideshow.getCurrentSlide());
        assertEquals(0, slideshow.previous());
        assertEquals(s1, slideshow.getCurrentSlide());

        // Go to specific index
        slideshow.goTo(1);
        assertEquals(1, slideshow.getCurrentIndex());
        slideshow.goTo(99); // invalid
        assertEquals(1, slideshow.getCurrentIndex());

        // Pause state
        assertFalse(slideshow.isPaused());
        slideshow.setPaused(true);
        assertTrue(slideshow.isPaused());
    }

    @Test
    public void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new ImageSlideshow(null));
        assertThrows(IllegalArgumentException.class, () -> new ImageSlideshow(List.of()));
    }
}
