package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class PageHeader extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblSubtitle;
    private final JPanel pnlActions;

    public PageHeader(String title, String subtitle) {
        super(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel pnlText = new JPanel();
        pnlText.setOpaque(false);
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));

        lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        pnlText.add(lblTitle);
        pnlText.add(lblSubtitle);

        pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        add(pnlText, BorderLayout.WEST);
        add(pnlActions, BorderLayout.EAST);
    }

    public void AddAction(JComponent component) {
        pnlActions.add(component);
        revalidate();
        repaint();
    }
}
