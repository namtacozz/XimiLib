package com.ximilib.presentation.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.LayoutManager;

public class SectionCard extends JPanel {

    public SectionCard() {
        InitComponent(16);
    }

    public SectionCard(int padding) {
        InitComponent(padding);
    }

    public SectionCard(LayoutManager layout, int padding) {
        super(layout);
        InitComponent(padding);
    }

    private void InitComponent(int padding) {
        setBackground(Color.WHITE);
        putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: " + UITheme.HEX_ON_PRIMARY + "; borderColor: " + UITheme.HEX_CARD_BORDER);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)
        ));
    }
}
