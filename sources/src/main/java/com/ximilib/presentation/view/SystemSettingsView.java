package com.ximilib.presentation.view;

import com.ximilib.application.port.SystemSettingsPort;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * SystemSettingsView: Màn hình Cài đặt Quy định Thư viện (SRS & Bảng cauhinh).
 * Dành riêng cho Quản trị viên (Admin) điều chỉnh các tham số nghiệp vụ động:
 * Hạn mức mượn, số ngày mượn, mức phạt trễ hạn, phí kỹ thuật, tỷ lệ đền bù.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class SystemSettingsView extends JPanel {

    private final SystemSettingsPort settingsPort;
    private final DefaultTableModel tableModel;
    private JTable settingsTable;

    public SystemSettingsView() {
        this(AppContainer.getInstance().getSystemSettingsPort());
    }

    public SystemSettingsView(SystemSettingsPort settingsPort) {
        this.settingsPort = (settingsPort != null) ? settingsPort : AppContainer.getInstance().getSystemSettingsPort();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 28, 28, 28));

        tableModel = new DefaultTableModel(new String[]{"Mã Tham Số", "Giá Trị Quy Định", "Mô Tả Nghiệp Vụ (SRS)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Chỉ cho phép sửa cột Giá Trị
            }
        };

        initUI();
        loadSettingsAsync();
    }

    @Deprecated
    public SystemSettingsView(Object legacyConnectionManager) {
        this(AppContainer.getInstance().getSystemSettingsPort());
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblBreadcrumb = new JLabel("Trang chủ / Cài đặt quy định (SRS)");
        lblBreadcrumb.setFont(UITheme.FONT_SMALL);
        lblBreadcrumb.setForeground(UITheme.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Cấu Hình Tham Số Nghiệp Vụ Thư Viện");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblTitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnSave = UITheme.createPillButton("Lưu Thay Đổi", UITheme.PRIMARY, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(130, 36));
        btnSave.addActionListener(e -> saveSelectedSetting());

        JButton btnReload = UITheme.createPillButton("Tải Lại", UITheme.BG_LIGHT, UITheme.TEXT_MAIN);
        btnReload.setPreferredSize(new Dimension(90, 36));
        btnReload.addActionListener(e -> loadSettingsAsync());

        actions.add(btnReload);
        actions.add(btnSave);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Bảng tham số
        settingsTable = new JTable(tableModel);
        settingsTable.setRowHeight(38);
        settingsTable.setFont(UITheme.FONT_REGULAR);
        settingsTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        settingsTable.getTableHeader().setBackground(UITheme.BG_LIGHT);

        settingsTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        settingsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        settingsTable.getColumnModel().getColumn(2).setPreferredWidth(450);

        JScrollPane scrollPane = new JScrollPane(settingsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(16, 0, 0, 0),
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSettingsAsync() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                return settingsPort.getAllSettings();
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void saveSelectedSetting() {
        if (settingsTable.isEditing()) {
            settingsTable.getCellEditor().stopCellEditing();
        }

        int row = settingsTable.getSelectedRow();
        if (row < 0) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn một dòng tham số để lưu cập nhật!");
            return;
        }

        String key = (String) tableModel.getValueAt(row, 0);
        String val = (String) tableModel.getValueAt(row, 1);
        String user = (UserSession.getCurrentUser() != null) ? UserSession.getCurrentUser().getId() : "ADMIN";

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                settingsPort.updateSetting(key, val, user);
                return null;
            }

            @Override
            protected void done() {
                UiModalHelper.showToastSuccess(SystemSettingsView.this, "Đã cập nhật tham số [" + key + "] thành công!");
            }
        }.execute();
    }
}
