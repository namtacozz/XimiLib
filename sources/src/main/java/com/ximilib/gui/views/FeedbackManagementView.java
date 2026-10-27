package com.ximilib.gui.views;

import com.ximilib.gui.util.UITheme;
import com.ximilib.gui.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FeedbackManagementView: Màn hình Góp Ý & Phản Hồi Dịch Vụ Thư Viện (F-10).
 * Cho phép Độc giả gửi phản hồi, và Thủ thư / Quản trị viên tiếp nhận xử lý.
 * Dữ liệu lưu trữ trong bảng `gopy`.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class FeedbackManagementView extends JPanel {

    private final DatabaseConnectionManager connectionManager;
    private final boolean isReader;

    // Components cho Reader
    private JTextField txtTitle;
    private JTextArea txtContent;

    // Components cho Staff
    private DefaultTableModel tableModel;
    private JTable feedbackTable;

    public FeedbackManagementView() {
        this(AppContainer.getInstance().getConnectionManager(),
            UserSession.getCurrentUser() != null && UserSession.getCurrentUser().getRole().contains("Độc Giả"));
    }

    public FeedbackManagementView(DatabaseConnectionManager connectionManager, boolean isReader) {
        this.connectionManager = (connectionManager != null) ? connectionManager : AppContainer.getInstance().getConnectionManager();
        this.isReader = isReader;

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 28, 28, 28));

        initUI();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblBreadcrumb = new JLabel("Trang chủ / Góp ý & Phản hồi (F-10)");
        lblBreadcrumb.setFont(UITheme.FONT_SMALL);
        lblBreadcrumb.setForeground(UITheme.TEXT_MUTED);

        JLabel lblTitle = new JLabel("Hộp Thư Góp Ý & Đánh Giá Dịch Vụ");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        titleBox.add(lblBreadcrumb);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblTitle);
        headerPanel.add(titleBox, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        if (isReader) {
            add(createReaderFeedbackPanel(), BorderLayout.CENTER);
        } else {
            add(createStaffFeedbackPanel(), BorderLayout.CENTER);
            loadFeedbacksAsync();
        }
    }

    private JPanel createReaderFeedbackPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(16, 0, 0, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
            )
        ));

        JLabel lblDesc = new JLabel("Mọi ý kiến đóng góp của bạn đọc giúp Thư viện ĐHBK nâng cao chất lượng phục vụ:");
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        panel.add(lblDesc);
        panel.add(Box.createVerticalStrut(16));

        JLabel lblT = new JLabel("Tiêu đề góp ý:");
        lblT.setFont(UITheme.FONT_BOLD);
        panel.add(lblT);
        panel.add(Box.createVerticalStrut(6));

        txtTitle = new JTextField();
        txtTitle.setFont(UITheme.FONT_REGULAR);
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(txtTitle);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblC = new JLabel("Nội dung chi tiết:");
        lblC.setFont(UITheme.FONT_BOLD);
        panel.add(lblC);
        panel.add(Box.createVerticalStrut(6));

        txtContent = new JTextArea(6, 20);
        txtContent.setFont(UITheme.FONT_REGULAR);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtContent);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        panel.add(scroll);
        panel.add(Box.createVerticalStrut(20));

        JButton btnSubmit = UITheme.createPillButton("Gửi Góp Ý Đến Ban Quản Lý", UITheme.PRIMARY, Color.WHITE);
        btnSubmit.setPreferredSize(new Dimension(220, 38));
        btnSubmit.addActionListener(e -> submitReaderFeedback());
        panel.add(btnSubmit);

        return panel;
    }

    private JPanel createStaffFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 0, 0, 0));

        tableModel = new DefaultTableModel(new String[]{"Mã", "Bạn Đọc", "Tiêu Đề", "Nội Dung", "Thời Gian", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feedbackTable = new JTable(tableModel);
        feedbackTable.setRowHeight(36);
        feedbackTable.setFont(UITheme.FONT_REGULAR);
        feedbackTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        feedbackTable.getTableHeader().setBackground(UITheme.BG_LIGHT);

        feedbackTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        feedbackTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        feedbackTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        feedbackTable.getColumnModel().getColumn(3).setPreferredWidth(320);
        feedbackTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        feedbackTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(feedbackTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomActions.setOpaque(false);

        JButton btnProcess = UITheme.createPillButton("Đánh Dấu Đã Xử Lý", UITheme.PRIMARY, Color.WHITE);
        btnProcess.setPreferredSize(new Dimension(160, 36));
        btnProcess.addActionListener(e -> markAsProcessed());

        JButton btnReload = UITheme.createPillButton("Làm Mới", UITheme.BG_LIGHT, UITheme.TEXT_MAIN);
        btnReload.setPreferredSize(new Dimension(90, 36));
        btnReload.addActionListener(e -> loadFeedbacksAsync());

        bottomActions.add(btnReload);
        bottomActions.add(btnProcess);
        panel.add(bottomActions, BorderLayout.SOUTH);

        return panel;
    }

    private void submitReaderFeedback() {
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng điền đầy đủ tiêu đề và nội dung góp ý!");
            return;
        }

        String userId = (UserSession.getCurrentUser() != null) ? UserSession.getCurrentUser().getId() : "ND004";

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                String sql = "INSERT INTO gopy (id_nguoidung, tieude, noidung, ngaytao, trangthai) VALUES (?, ?, ?, NOW(), 'CHO_TIEP_NHAN')";
                try (Connection conn = connectionManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, userId);
                    ps.setString(2, title);
                    ps.setString(3, content);
                    ps.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                txtTitle.setText("");
                txtContent.setText("");
                UiModalHelper.showToastSuccess(FeedbackManagementView.this, "Cảm ơn bạn đã gửi phản hồi! Thư viện sẽ sớm tiếp nhận.");
            }
        }.execute();
    }

    private void loadFeedbacksAsync() {
        new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() throws SQLException {
                String sql = """
                    SELECT g.id, COALESCE(u.Hoten, 'Ẩn danh'), g.tieude, g.noidung, g.ngaytao,
                           CASE g.trangthai WHEN 'CHO_TIEP_NHAN' THEN 'Chờ tiếp nhận' ELSE 'Đã xử lý' END
                    FROM gopy g
                    LEFT JOIN nguoidung u ON g.id_nguoidung = u.ID
                    ORDER BY g.ngaytao DESC
                """;
                try (Connection conn = connectionManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        publish(new Object[]{
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6)
                        });
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                tableModel.setRowCount(0);
                for (Object[] r : chunks) {
                    tableModel.addRow(r);
                }
            }
        }.execute();
    }

    private void markAsProcessed() {
        int row = feedbackTable.getSelectedRow();
        if (row < 0) {
            UiModalHelper.showToastWarning(this, "Vui lòng chọn dòng góp ý cần xử lý!");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                String sql = "UPDATE gopy SET trangthai = 'DA_XU_LY' WHERE id = ?";
                try (Connection conn = connectionManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                UiModalHelper.showToastSuccess(FeedbackManagementView.this, "Đã cập nhật trạng thái xử lý góp ý #" + id);
                loadFeedbacksAsync();
            }
        }.execute();
    }
}
