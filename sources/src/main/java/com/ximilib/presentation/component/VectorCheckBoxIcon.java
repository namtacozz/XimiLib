package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.AbstractButton;
import javax.swing.Icon;

public class VectorCheckBoxIcon implements Icon {

    private final int size;

    public VectorCheckBoxIcon() {
        this(18);
    }

    public VectorCheckBoxIcon(int size) {
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size + 4;
    }

    @Override
    public int getIconHeight() {
        return size + 4;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean isSelected = false;
        boolean isRollover = false;
        boolean hasFocus = false;
        boolean isEnabled = true;

        if (c instanceof AbstractButton btn) {
            isSelected = btn.isSelected();
            isRollover = btn.getModel().isRollover();
            hasFocus = btn.hasFocus();
            isEnabled = btn.isEnabled();
        }

        int boxX = x + 2;
        int boxY = y + 2;
        int boxSize = size;
        int arc = 6;

        if (hasFocus) {
            g2.setColor(UITheme.PRIMARY_TINT);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(boxX - 2, boxY - 2, boxSize + 4, boxSize + 4, arc + 4, arc + 4);
        }

        if (isSelected) {

            Color bg = !isEnabled ? UITheme.CARD_BORDER : (isRollover ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY);
            g2.setColor(bg);
            g2.fillRoundRect(boxX, boxY, boxSize, boxSize, arc, arc);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float tick = new Path2D.Float();
            tick.moveTo(boxX + boxSize * 0.25f, boxY + boxSize * 0.50f);
            tick.lineTo(boxX + boxSize * 0.44f, boxY + boxSize * 0.72f);
            tick.lineTo(boxX + boxSize * 0.78f, boxY + boxSize * 0.28f);
            g2.draw(tick);
        } else {

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(boxX, boxY, boxSize, boxSize, arc, arc);

            Color border = !isEnabled ? UITheme.CARD_BORDER : (isRollover ? UITheme.PRIMARY : UITheme.BORDER);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(boxX, boxY, boxSize - 1, boxSize - 1, arc, arc);
        }

        g2.dispose();
    }
}
