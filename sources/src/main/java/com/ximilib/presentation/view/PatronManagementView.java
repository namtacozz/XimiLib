package com.ximilib.presentation.view;

import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.presentation.component.SearchField;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatronManagementView: Màn hình Hồ Sơ Độc Giả & Quản Lý Thẻ Thư Viện (UC-2, F-04).
 * Tìm kiếm bạn đọc, kiểm tra thời hạn thẻ, thực hiện khóa/mở thẻ và gia hạn thẻ.
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class PatronManagementView extends JPanel {

    private final AuthenticationService authService;
    private final PatronTableModel tableModel = new PatronTableModel();
    private JTable patronTable;
    private SearchField txtSearch;

    public PatronManagementView() {
        this(AppContainer.getInstance().getAuthenticationService());
    }

    public PatronManagementView(AuthenticationService service) {
        this.authService = service != null ? service : AppContainer.getInstance().getAuthenticationService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
        loadPatronsAsync("");
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Hồ sơ độc giả & Quản lý thẻ");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Danh sách bạn đọc đã đăng ký tài khoản và thông tin thẻ thư viện điện tử");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBox.setOpaque(false);
        txtSearch = new SearchField("Tìm theo tên, email, số thẻ...");
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { loadPatronsAsync(txtSearch.getText().trim()); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { loadPatronsAsync(txtSearch.getText().trim()); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { loadPatronsAsync(txtSearch.getText().trim()); }
        });
        searchBox.add(txtSearch);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(searchBox, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        patronTable = new JTable(tableModel);
        patronTable.setRowHeight(48);
        patronTable.setShowHorizontalLines(true);
        patronTable.setShowVerticalLines(false);
        patronTable.setGridColor(UITheme.BORDER);
        patronTable.setBackground(UITheme.CARD_BG);
        patronTable.setFont(UITheme.FONT_REGULAR);
        patronTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        patronTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        patronTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        patronTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        patronTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        patronTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        patronTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        patronTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        patronTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        patronTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        patronTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        patronTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        ActionCell cell = new ActionCell();
        patronTable.getColumnModel().getColumn(6).setCellRenderer(cell);
        patronTable.getColumnModel().getColumn(6).setCellEditor(cell);

        JScrollPane scroll = new JScrollPane(patronTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        card.add(scroll, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        main.add(card, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private void loadPatronsAsync(String keyword) {
        SwingWorker<List<PatronDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PatronDto> doInBackground() {
                return authService.searchPatrons(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<PatronDto> list = get();
                    SwingUtilities.invokeLater(() -> tableModel.setPatrons(list));
                } catch (Exception ex) {
                    UiModalHelper.showToastError(PatronManagementView.this, "Lỗi tải bạn đọc: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void toggleLockCard(PatronDto patron) {
        boolean isLocked = "KHOA".equalsIgnoreCase(patron.getCardStatus()) || "0".equals(patron.getCardStatus());
        String newStatus = isLocked ? "HOAT_DONG" : "KHOA";
        String prompt = isLocked ? ("Mở khóa thẻ " + patron.getCardNumber() + "?") : ("Khóa thẻ " + patron.getCardNumber() + "?");

        UiModalHelper.showConfirm(this, "Xác nhận", prompt, () -> {
            authService.updateCardStatus(patron.getCardNumber(), newStatus);
            UiModalHelper.showToastSuccess(this, "Đã cập nhật trạng thái thẻ thành công!");
            loadPatronsAsync(txtSearch.getText().trim());
        });
    }

    private void extendCard(PatronDto patron) {
        UiModalHelper.showConfirm(this, "Gia hạn thẻ", "Gia hạn thẻ " + patron.getCardNumber() + " thêm 365 ngày?", () -> {
            authService.extendCardValidity(patron.getCardNumber(), 365);
            UiModalHelper.showToastSuccess(this, "Đã gia hạn thẻ thêm 365 ngày thành công!");
            loadPatronsAsync(txtSearch.getText().trim());
        });
    }

    private class PatronTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã ĐG", "Họ và tên", "Email", "Số thẻ TV", "Hạn thẻ", "Trạng thái", "Thao tác"};
        private final List<PatronDto> patrons = new ArrayList<>();

        public void setPatrons(List<PatronDto> list) {
            patrons.clear();
            if (list != null) patrons.addAll(list);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return patrons.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 6; }

        @Override
        public Object getValueAt(int r, int c) {
            PatronDto p = patrons.get(r);
            return switch (c) {
                case 0 -> p.getUserId();
                case 1 -> p.getFullName();
                case 2 -> p.getEmail();
                case 3 -> p.getCardNumber() != null ? p.getCardNumber() : "Chưa cấp";
                case 4 -> p.getExpiryDate() != null ? p.getExpiryDate() : "-";
                case 5 -> {
                    if ("CHUA_CAP".equalsIgnoreCase(p.getCardStatus())) yield "Chưa cấp";
                    if ("HOAT_DONG".equalsIgnoreCase(p.getCardStatus()) || "1".equals(p.getCardStatus())) {
                        yield p.isExpired() ? "Hết hạn" : "Hoạt động";
                    }
                    yield "Bị khóa";
                }
                case 6 -> p;
                default -> null;
            };
        }
    }

    private class ActionCell extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        private final JButton btnLock = UITheme.createPillButton("Khóa", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        private final JButton btnExtend = UITheme.createPillButton("+1 Năm", UITheme.SUCCESS_BG, UITheme.SUCCESS_TEXT);
        private PatronDto currentPatron;

        ActionCell() {
            panel.setOpaque(false);
            btnLock.setFont(UITheme.FONT_BADGE);
            btnExtend.setFont(UITheme.FONT_BADGE);

            btnLock.addActionListener(e -> {
                fireEditingStopped();
                if (currentPatron != null) toggleLockCard(currentPatron);
            });
            btnExtend.addActionListener(e -> {
                fireEditingStopped();
                if (currentPatron != null) extendCard(currentPatron);
            });

            panel.add(btnLock);
            panel.add(btnExtend);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            if (v instanceof PatronDto p) {
                currentPatron = p;
                boolean isLocked = "KHOA".equalsIgnoreCase(p.getCardStatus()) || "0".equals(p.getCardStatus());
                btnLock.setText(isLocked ? "Mở khóa" : "Khóa");
                btnLock.setBackground(isLocked ? UITheme.SUCCESS_BG : UITheme.DANGER_BG);
                btnLock.setForeground(isLocked ? UITheme.SUCCESS_TEXT : UITheme.DANGER_TEXT);
            }
            return panel;
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            if (v instanceof PatronDto p) {
                currentPatron = p;
                boolean isLocked = "KHOA".equalsIgnoreCase(p.getCardStatus()) || "0".equals(p.getCardStatus());
                btnLock.setText(isLocked ? "Mở khóa" : "Khóa");
                btnLock.setBackground(isLocked ? UITheme.SUCCESS_BG : UITheme.DANGER_BG);
                btnLock.setForeground(isLocked ? UITheme.SUCCESS_TEXT : UITheme.DANGER_TEXT);
            }
            return panel;
        }

        @Override public Object getCellEditorValue() { return currentPatron; }
    }

    private static class StatusBadgeRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String val = (value != null) ? value.toString() : "";
            if ("Hoạt động".equalsIgnoreCase(val)) {
                lbl.setForeground(UITheme.SUCCESS_TEXT);
            } else if ("Hết hạn".equalsIgnoreCase(val)) {
                lbl.setForeground(UITheme.WARNING_TEXT);
            } else {
                lbl.setForeground(UITheme.DANGER_TEXT);
            }
            lbl.setFont(UITheme.FONT_BOLD);
            return lbl;
        }
    }
}
