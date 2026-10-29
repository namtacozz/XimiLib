package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import raven.datetime.DatePicker;

/**
 * DateRangeFilterPanel: Bo loc thoi gian tu ngay den ngay
 * su dung DatePicker voi che do BETWEEN_DATE_SELECTED (UC-7).
 */
public class DateRangeFilterPanel extends JPanel {

    private final JFormattedTextField txtRangeEditor;
    private final DatePicker rangePicker;

    public DateRangeFilterPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        JLabel lbl = new JLabel("Khoảng ngày:");
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);

        txtRangeEditor = new JFormattedTextField();
        txtRangeEditor.setFont(UITheme.FONT_REGULAR);
        txtRangeEditor.setPreferredSize(new Dimension(190, 32));
        txtRangeEditor.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));

        rangePicker = UiModalHelper.createDatePicker(txtRangeEditor, true);
        rangePicker.setSelectedDateRange(LocalDate.now().minusDays(30), LocalDate.now());

        add(lbl);
        add(txtRangeEditor);
    }

    public LocalDate[] getSelectedDateRange() {
        return rangePicker.getSelectedDateRange();
    }
}
