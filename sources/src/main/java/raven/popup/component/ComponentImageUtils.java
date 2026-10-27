package raven.popup.component;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 * Shadow override of raven.popup.component.ComponentImageUtils
 * to eliminate NullPointerException when Component.createVolatileImage returns null.
 */
public class ComponentImageUtils {

    public static VolatileImage createImage(Component component) {
        if (component == null) {
            return null;
        }
        int width = component.getWidth();
        int height = component.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        try {
            VolatileImage image = component.createVolatileImage(width, height);
            if (image == null) {
                GraphicsConfiguration gc = component.getGraphicsConfiguration();
                if (gc == null) {
                    gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration();
                }
                if (gc != null) {
                    image = gc.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
                }
            }

            if (image != null) {
                Graphics2D g2 = image.createGraphics();
                if (g2 != null) {
                    try {
                        component.paint(g2);
                    } finally {
                        g2.dispose();
                    }
                }
                return image;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static Image createImage(Component component, int round) {
        if (component == null) {
            return null;
        }
        int width = component.getWidth();
        int height = component.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        try {
            BufferedImage compImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gComp = compImage.createGraphics();
            component.paint(gComp);
            gComp.dispose();

            if (round <= 0) {
                return compImage;
            }

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = result.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int roundScale = UIScale.scale(round);
            FlatUIUtils.paintComponentBackground(g2, 0, 0, width, height, 0.0f, (float) roundScale);
            g2.setComposite(AlphaComposite.SrcIn);
            g2.drawImage(compImage, 0, 0, null);
            g2.dispose();
            compImage.flush();
            return result;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
