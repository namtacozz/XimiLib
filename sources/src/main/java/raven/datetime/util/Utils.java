package raven.datetime.util;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

/**
 * Shadow override of raven.datetime.util.Utils to maintain compatibility
 * with FlatLaf 3.5.4+ (FlatSVGIcon.ColorFilter Function constructor).
 */
public class Utils {

    public static Point adjustPopupLocation(JPopupMenu popupMenu, Component component, Point point) {
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window == null) {
            return new Point(0, component.getHeight());
        }
        Insets insets = window.getInsets();
        insets = FlatUIUtils.addInsets(insets, UIScale.scale(new Insets(5, 5, 5, 5)));
        Dimension preferredSize = popupMenu.getComponent().getPreferredSize();
        int availableWidth = window.getWidth() - (insets.left + insets.right);
        int availableHeight = window.getHeight() - (insets.top + insets.bottom);
        Point convertedPoint = SwingUtilities.convertPoint(component, new Point(0, component.getHeight()), window);
        int spaceBottom = availableHeight - convertedPoint.y - preferredSize.height - point.y;
        int spaceRight = availableWidth - convertedPoint.x - preferredSize.width - point.x;
        int targetX;
        if (insets.left + spaceRight > 0) {
            targetX = convertedPoint.x + point.x;
        } else {
            targetX = Math.max(convertedPoint.x + component.getWidth() - preferredSize.width - point.x, insets.left);
        }
        int targetY;
        if (insets.top + spaceBottom > 0) {
            targetY = convertedPoint.y + point.y;
        } else {
            targetY = Math.max(convertedPoint.y - component.getHeight() - preferredSize.height - point.y, insets.top);
        }
        return SwingUtilities.convertPoint(window, new Point(targetX, targetY), component);
    }

    public static Color getColor(Color color, boolean isSelected, boolean isHover) {
        if (isSelected) {
            return FlatLaf.isLafDark() ? ColorFunctions.lighten(color, 0.1f) : ColorFunctions.darken(color, 0.1f);
        }
        if (isHover) {
            return FlatLaf.isLafDark() ? ColorFunctions.lighten(color, 0.03f) : ColorFunctions.darken(color, 0.03f);
        }
        return color;
    }

    public static Icon createIcon(String name, float scale) {
        FlatSVGIcon icon = new FlatSVGIcon(name, scale);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UIManager.getColor("Actions.Grey")));
        return icon;
    }
}
