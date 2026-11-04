package com.ximilib.presentation.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AppLogo {

    private static final String SVG_PATH = "images/logo/logo.svg";
    private static final String PNG_PATH_BASE = "/images/logo/logo";
    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};

    private static final ConcurrentMap<Integer, Icon> ICON_CACHE = new ConcurrentHashMap<>();
    private static List<Image> windowImagesCache = null;

    private AppLogo() {}

    public static Icon getIcon(int size) {
        return ICON_CACHE.computeIfAbsent(size, s -> {
            Image img = getImage(s);
            if (img != null) {
                return new ImageIcon(img);
            }
            try {
                return new FlatSVGIcon(SVG_PATH, s, s);
            } catch (Exception ignored) {}
            return new ImageIcon();
        });
    }

    public static Image getImage(int size) {
        String specificPath = PNG_PATH_BASE + "_" + size + ".png";
        try (InputStream is = AppLogo.class.getResourceAsStream(specificPath)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {}

        try (InputStream is = AppLogo.class.getResourceAsStream(PNG_PATH_BASE + ".png")) {
            if (is != null) {
                BufferedImage master = ImageIO.read(is);
                return createScaledImage(master, size);
            }
        } catch (Exception ignored) {}

        try {
            File f = new File("sources/src/main/resources" + PNG_PATH_BASE + "_" + size + ".png");
            if (f.exists()) {
                return ImageIO.read(f);
            }
            File fMaster = new File("sources/src/main/resources" + PNG_PATH_BASE + ".png");
            if (fMaster.exists()) {
                BufferedImage master = ImageIO.read(fMaster);
                return createScaledImage(master, size);
            }
        } catch (Exception ignored) {}

        return null;
    }

    private static Image createScaledImage(BufferedImage master, int size) {
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(master, 0, 0, size, size, null);
        g2.dispose();
        return scaled;
    }

    public static synchronized List<Image> getAppWindowImages() {
        if (windowImagesCache != null) {
            return windowImagesCache;
        }
        List<Image> list = new ArrayList<>();
        for (int sz : ICON_SIZES) {
            Image img = getImage(sz);
            if (img != null) {
                list.add(img);
            }
        }
        windowImagesCache = list;
        return list;
    }

    public static void applyWindowIcons(Window window) {
        if (window == null) return;
        List<Image> icons = getAppWindowImages();
        if (!icons.isEmpty()) {
            window.setIconImages(icons);
        }
    }
}
