package com.ximilib.presentation.view;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.model.LoanTicketStatus;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UserSession;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.presentation.component.VietQrModalPanel;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * ReaderMyLoansView: Màn hình "Phiếu mượn của tôi" dành cho Bạn Đọc (UC-5).
 * Cho phép xem sách đang mượn, hạn trả và gia hạn trực tuyến thêm 7 ngày (BR-03).
 * Tuân thủ R-01, R-06, R-08, R-11.
 */
public class ReaderMyLoansView extends JPanel {

    private final CirculationService circulationService;
    private final String currentUserId;
    private final LoanTableModel tableModel = new LoanTableModel();
    private JTable loanTable;

    public ReaderMyLoansView() {
        this(AppContainer.getInstance().getCirculationService(),
             UserSession.getCurrentUser() != null ? UserSession.getCurrentUser().getId() : "ND004");
    }

    public ReaderMyLoansView(CirculationService service, String userId) {
        this.circulationService = service != null ? service : AppContainer.getInstance().getCirculationService();
        this.currentUserId = userId != null ? userId : "ND004";

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
        loadDataAsync();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Phiếu mượn của tôi");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Theo dõi thời hạn các cuốn sách đang mượn và thực hiện gia hạn trực tuyến (tối đa 1 lần)");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        JButton btnRefresh = UITheme.createPillButton("Làm mới", UITheme.PRIMARY_TINT, UITheme.ON_TINT);
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> loadDataAsync());

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
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

        loanTable = new JTable(tableModel);
        loanTable.setRowHeight(48);
        loanTable.setShowHorizontalLines(true);
        loanTable.setShowVerticalLines(false);
        loanTable.setGridColor(UITheme.BORDER);
        loanTable.setBackground(UITheme.CARD_BG);
        loanTable.setFont(UITheme.FONT_REGULAR);
        loanTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        loanTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        loanTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        loanTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        loanTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        loanTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        loanTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        loanTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        loanTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        loanTable.getColumnModel().getColumn(5).setPreferredWidth(210);

        ActionCell cell = new ActionCell();
        loanTable.getColumnModel().getColumn(5).setCellRenderer(cell);
        loanTable.getColumnModel().getColumn(5).setCellEditor(cell);

        JScrollPane scroll = new JScrollPane(loanTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        card.add(scroll, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        main.add(card, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private void loadDataAsync() {
        SwingWorker<List<BorrowTicketDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BorrowTicketDto> doInBackground() {
                return circulationService.getReaderLoans(currentUserId);
            }

            @Override
            protected void done() {
                try {
                    List<BorrowTicketDto> list = get();
                    SwingUtilities.invokeLater(() -> tableModel.setTickets(list));
                } catch (Exception ex) {
                    UiModalHelper.showToastError(ReaderMyLoansView.this, "Lỗi tải phiếu mượn: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleRenew(BorrowTicketDto ticket) {
        JTextField txtReason = new JTextField("Cần thêm thời gian nghiên cứu tài liệu");
        txtReason.setPreferredSize(new Dimension(280, 36));

        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setPreferredSize(new Dimension(300, 60));
        p.add(new JLabel("Lý do gia hạn:"), BorderLayout.NORTH);
        p.add(txtReason, BorderLayout.CENTER);

        UiModalHelper.showCustomModal(
            this,
            "RENEW",
            "Gia hạn phiếu mượn #" + ticket.getTicketId(),
            p,
            raven.modal.component.SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                    try {
                        circulationService.renewLoan(ticket.getTicketId(), txtReason.getText().trim());
                        controller.close();
                        UiModalHelper.showToastSuccess(this, "Đã gia hạn thành công thêm 7 ngày!");
                        loadDataAsync();
                    } catch (Exception ex) {
                        UiModalHelper.showToastWarning(this, ex.getMessage());
                    }
                } else if (action == raven.modal.component.SimpleModalBorder.CANCEL_OPTION || action == raven.modal.component.SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
    }

    private void handleReportLoss(BorrowTicketDto ticket) {
        String copyBarcode = ticket.getCopyBarcodes();
        if (copyBarcode == null || copyBarcode.trim().isEmpty()) {
            UiModalHelper.showToastWarning(this, "Không xác định được mã cuốn sách của phiếu mượn này.");
            return;
        }
        // Lấy mã cuốn sách đầu tiên nếu có nhiều cuốn
        String barcode = copyBarcode.split(";")[0].trim();

        String confirmMsg = """
            Bạn có chắc chắn muốn BÁO MẤT cuốn sách thuộc phiếu mượn #%s không?

            Theo quy định của Thư viện (SRS YC-09 & BR-08):
            • Phiếu mượn sẽ chuyển sang trạng thái 'Chờ xử lý bồi thường'.
            • Tiền bồi thường = (200%% * Giá bìa gốc) + 20.000 VNĐ phí kỹ thuật + phạt quá hạn (nếu có).
            • Bạn có thể thanh toán trực tuyến ngay qua mã VietQR hoặc nộp tiền tại quầy.
            """.formatted(ticket.getTicketId());

        UiModalHelper.showConfirm(
            this,
            "Xác Nhận Báo Mất Sách (SRS YC-09)",
            confirmMsg,
            () -> {
                try {
                    CompensationResultDto res = circulationService.reportLoss(barcode);
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                    JPanel resultPanel = new JPanel();
                    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
                    resultPanel.setPreferredSize(new Dimension(380, 200));

                    resultPanel.add(new JLabel("Đầu sách: " + res.getBookTitle()));
                    resultPanel.add(Box.createVerticalStrut(4));
                    resultPanel.add(new JLabel("Mã cuốn sách: " + barcode));
                    resultPanel.add(Box.createVerticalStrut(4));
                    resultPanel.add(new JLabel("Giá bìa gốc: " + nf.format(res.getCoverPrice())));
                    resultPanel.add(new JLabel("Tiền đền bù sách (200%): " + nf.format(res.getBookLossAmount())));
                    resultPanel.add(new JLabel("Phí xử lý kỹ thuật: " + nf.format(res.getTechFee())));
                    resultPanel.add(new JLabel("Phạt trễ hạn: " + nf.format(res.getOverdueFine())));
                    resultPanel.add(Box.createVerticalStrut(8));

                    JLabel lblTotal = new JLabel("TỔNG TIỀN PHẢI NỘP: " + nf.format(res.getTotalAmount()));
                    lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    lblTotal.setForeground(UITheme.PRIMARY);
                    resultPanel.add(lblTotal);

                    UiModalHelper.showCustomModal(
                        this,
                        "LOSS_RESULT_MODAL",
                        "Kết Quả Báo Mất & Bồi Thường",
                        resultPanel,
                        raven.modal.component.SimpleModalBorder.OK_OPTION,
                        (controller, action) -> {
                            if (action == raven.modal.component.SimpleModalBorder.OK_OPTION) {
                                controller.close();
                                // Mở tiếp mã VietQR để độc giả thanh toán trực tuyến
                                VietQrModalPanel.show(
                                    this,
                                    res.getFineTicketId(),
                                    res.getTotalAmount(),
                                    () -> {
                                        circulationService.payFine(res.getFineTicketId(), "VIETQR");
                                        UiModalHelper.showToastSuccess(this, "Thanh toán bồi hoàn thành công qua VietQR! Phiếu mượn đã được hoàn tất.");
                                        loadDataAsync();
                                    }
                                );
                            }
                        }
                    );
                    loadDataAsync();
                } catch (Exception ex) {
                    UiModalHelper.showToastError(this, ex.getMessage());
                }
            }
        );
    }

    private class LoanTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã phiếu", "Đầu sách mượn", "Ngày mượn", "Hạn trả", "Tình trạng", "Thao tác"};
        private final List<BorrowTicketDto> tickets = new ArrayList<>();

        public void setTickets(List<BorrowTicketDto> list) {
            tickets.clear();
            if (list != null) tickets.addAll(list);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return tickets.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public boolean isCellEditable(int r, int c) { return c == 5; }

        @Override
        public Object getValueAt(int r, int c) {
            BorrowTicketDto t = tickets.get(r);
            return switch (c) {
                case 0 -> t.getTicketId();
                case 1 -> t.getBookTitle();
                case 2 -> t.getBorrowDate() != null ? t.getBorrowDate().toLocalDate() : "-";
                case 3 -> t.getDueDate() != null ? t.getDueDate().toLocalDate() : "-";
                case 4 -> t.getStatusLabel();
                case 5 -> t;
                default -> null;
            };
        }
    }

    private class ActionCell extends AbstractCellEditor implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        private final JButton btnRenew = UITheme.createPillButton("Gia hạn (+7d)", UITheme.PRIMARY_TINT, UITheme.ON_TINT);
        private final JButton btnReportLoss = UITheme.createPillButton("Báo mất", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        private BorrowTicketDto currentTicket;

        ActionCell() {
            panel.setOpaque(false);
            btnRenew.setFont(UITheme.FONT_BADGE);
            btnReportLoss.setFont(UITheme.FONT_BADGE);

            btnRenew.addActionListener(e -> {
                fireEditingStopped();
                if (currentTicket != null) handleRenew(currentTicket);
            });

            btnReportLoss.addActionListener(e -> {
                fireEditingStopped();
                if (currentTicket != null) handleReportLoss(currentTicket);
            });

            panel.add(btnRenew);
            panel.add(btnReportLoss);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            if (v instanceof BorrowTicketDto b) {
                currentTicket = b;
                boolean isBorrowing = (b.getStatus() == LoanTicketStatus.DANG_MUON.getCode());
                btnRenew.setEnabled(isBorrowing && !b.isOverdue());
                btnReportLoss.setEnabled(isBorrowing);
            }
            return panel;
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            if (v instanceof BorrowTicketDto b) currentTicket = b;
            return panel;
        }

        @Override public Object getCellEditorValue() { return currentTicket; }
    }
}
