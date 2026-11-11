package com.ximilib.presentation.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.ximilib.presentation.component.SectionCard;
import com.ximilib.presentation.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Lớp cha trừu tượng cho tất cả các màn hình View trong ứng dụng (Presentation Layer Inheritance).
 * Gom các hàm thiết lập tiêu đề trang, thẻ KPI thu nhỏ, định dạng bảng chuẩn LibRack,
 * và điều phối tác vụ bất đồng bộ (SwingWorker).
 */
public abstract class BaseViewPanel extends JPanel {

    protected BaseViewPanel() {
        super(new BorderLayout());
        setBackground(UITheme.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));
    }

    /**
     * Tạo thanh tiêu đề trang chuẩn hóa.
     */
    protected JPanel createHeader(String titleText, String subtitleText, JComponent rightAction) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel(subtitleText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(sub);

        header.add(textPanel, BorderLayout.WEST);
        if (rightAction != null) {
            header.add(rightAction, BorderLayout.EAST);
        }
        return header;
    }

    /**
     * Tạo thẻ KPI nhỏ chuẩn LibRack với viền accent bên trái.
     */
    protected JPanel createMiniKpiCard(String label, JLabel valueLabel, Color accent) {
        SectionCard card = new SectionCard(new BorderLayout(12, 0), new Insets(12, 16, 12, 16), 12, Color.WHITE, UITheme.CARD_BORDER);

        JPanel leftBar = new SectionCard(4, accent);
        leftBar.setPreferredSize(new Dimension(4, 36));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        center.add(lbl);
        center.add(Box.createVerticalStrut(2));
        center.add(valueLabel);

        card.add(leftBar, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    /**
     * Cấu hình bảng JTable theo chuẩn giao diện FlatLaf / LibRack.
     */
    protected JScrollPane styleStandardTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight > 0 ? rowHeight : 44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(253, 242, 248));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    /**
     * Thực thi tác vụ bất đồng bộ trên luồng phụ và cập nhật UI trên Event Dispatch Thread (EDT).
     */
    protected <T> void runAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (Exception ex) {
                    if (onError != null) {
                        onError.accept(ex);
                    } else {
                        ex.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
