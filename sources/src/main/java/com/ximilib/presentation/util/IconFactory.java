package com.ximilib.presentation.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public class IconFactory {

    public interface DynamicIcon extends Icon {
        void setColor(Color c);
    }

    private static class SvgDynamicIcon implements DynamicIcon {
        private final FlatSVGIcon svgIcon;
        private Color currentColor;

        public SvgDynamicIcon(String name, Color color, int size) {
            this.svgIcon = new FlatSVGIcon("icons/svg/" + name + ".svg", size, size);
            this.currentColor = color;
            if (color != null) {
                this.svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> this.currentColor));
            }
        }

        @Override
        public void setColor(Color c) {
            this.currentColor = c;
            if (c != null) {
                this.svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter(orig -> this.currentColor));
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            svgIcon.paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return svgIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return svgIcon.getIconHeight();
        }
    }

    private static Icon CreateSvgIcon(String name, Color color, int size) {
        return new SvgDynamicIcon(name, color, size);
    }

    public static Icon createSearchIcon(Color color, int size) {
        return CreateSvgIcon("search", color, size);
    }

    public static Icon createHomeIcon(Color color, int size) {
        return CreateSvgIcon("home", color, size);
    }

    public static Icon createBookIcon(Color color, int size) {
        return CreateSvgIcon("book", color, size);
    }

    public static Icon createTagIcon(Color color, int size) {
        return CreateSvgIcon("tag", color, size);
    }

    public static Icon createUsersIcon(Color color, int size) {
        return CreateSvgIcon("users", color, size);
    }

    public static Icon createUserIcon(Color color, int size) {
        return CreateSvgIcon("user", color, size);
    }

    public static Icon createGridIcon(Color color, int size) {
        return CreateSvgIcon("grid", color, size);
    }

    public static Icon createListIcon(Color color, int size) {
        return CreateSvgIcon("list", color, size);
    }

    public static Icon createCirculationIcon(Color color, int size) {
        return CreateSvgIcon("repeat", color, size);
    }

    public static Icon createCheckIcon(Color color, int size) {
        return CreateSvgIcon("check", color, size);
    }

    public static Icon createHistoryIcon(Color color, int size) {
        return CreateSvgIcon("history", color, size);
    }

    public static Icon createChartIcon(Color color, int size) {
        return CreateSvgIcon("bar-chart", color, size);
    }

    public static Icon createTrashIcon(Color color, int size) {
        return CreateSvgIcon("trash", color, size);
    }

    public static Icon createEditIcon(Color color, int size) {
        return CreateSvgIcon("edit", color, size);
    }

    public static Icon createWarningIcon(Color color, int size) {
        return CreateSvgIcon("alert-triangle", color, size);
    }

    public static Icon createStarIcon(Color color, int size) {
        return CreateSvgIcon("star", color, size);
    }

    public static Icon createChevronLeftIcon(Color color, int size) {
        return CreateSvgIcon("chevron-left", color, size);
    }

    public static Icon createChevronRightIcon(Color color, int size) {
        return CreateSvgIcon("chevron-right", color, size);
    }

    public static Icon createSettingsIcon(Color color, int size) {
        return CreateSvgIcon("settings", color, size);
    }

    public static Icon createHelpIcon(Color color, int size) {
        return CreateSvgIcon("help-circle", color, size);
    }

    public static Icon createCloseIcon(Color color, int size) {
        return CreateSvgIcon("x", color, size);
    }

    public static Icon createLockIcon(Color color, int size) {
        return CreateSvgIcon("lock", color, size);
    }

    public static Icon createUnlockIcon(Color color, int size) {
        return CreateSvgIcon("unlock", color, size);
    }

    public static Icon createClockIcon(Color color, int size) {
        return CreateSvgIcon("clock", color, size);
    }

    public static Icon createRefreshIcon(Color color, int size) {
        return CreateSvgIcon("refresh-cw", color, size);
    }

    public static Icon createExcelIcon(Color color, int size) {
        return CreateSvgIcon("file-spreadsheet", color, size);
    }

    public static Icon createPrintIcon(Color color, int size) {
        return CreateSvgIcon("printer", color, size);
    }

    public static Icon createAddUserIcon(Color color, int size) {
        return CreateSvgIcon("user-plus", color, size);
    }

    public static Icon createKeyIcon(Color color, int size) {
        return CreateSvgIcon("key", color, size);
    }

    public static Icon createShieldIcon(Color color, int size) {
        return CreateSvgIcon("shield", color, size);
    }

    public static Icon createRestoreIcon(Color color, int size) {
        return CreateSvgIcon("rotate-ccw", color, size);
    }

    public static Icon createCreditCardIcon(Color color, int size) {
        return CreateSvgIcon("credit-card", color, size);
    }

    public static Icon createEyeIcon(Color color, int size) {
        return CreateSvgIcon("eye", color, size);
    }

    public static Icon createBellIcon(Color color, int size) {
        return CreateSvgIcon("bell", color, size);
    }

    public static void drawBell(java.awt.Graphics2D g2, int x, int y, int size, Color color) {
        Icon bell = createBellIcon(color, size);
        bell.paintIcon(null, g2, x, y);
    }
}
