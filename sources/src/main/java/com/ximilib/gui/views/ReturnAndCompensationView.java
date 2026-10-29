package com.ximilib.gui.views;

import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.repository.CirculationRepository.BorrowDetailInfo;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * ReturnAndCompensationView: Màn hình Xử Lý Trả Sách & Bồi Hoàn Hỏng/Mất (UC-6 & UC-9).
 * Tự động tính tiền phạt trễ hạn (2.000đ/ngày), phí xử lý kỹ thuật (20.000đ)
 * và bồi thường mất sách (200% giá bìa theo BR-08).
 * Tuân thủ R-01, R-04, R-06, R-10, R-11.
 */
public class ReturnAndCompensationView extends JPanel {

    private final CirculationService circulationService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private JTextField txtBarcode;
    private JLabel lblBookTitle;
    private JLabel lblBorrower;
    private JLabel lblDueDate;
    private JLabel lblOverdueStatus;

    private JRadioButton radNormal;
    private JRadioButton radDamaged;
    private JRadioButton radLost;

    private JLabel lblFineDetail;
    private JLabel lblTotalAmount;
    private JButton btnExecute;

    private BorrowDetailInfo currentDetail;

    public ReturnAndCompensationView() {
        this(AppContainer.getInstance().getCirculationService());
    }

    public ReturnAndCompensationView(CirculationService service) {
        this.circulationService = service != null ? service : AppContainer.getInstance().getCirculationService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Xử lý trả sách & bồi hoàn mất/hỏng");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Quét mã cuốn sách để kiểm tra phiếu mượn, tính phạt trễ hạn hoặc lập biên bản bồi thường");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout()) {
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
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setPreferredSize(new Dimension(540, 500));

        // 1. Quét mã vạch
        formBox.add(createSectionLabel("1. QUÉT MÃ VẠCH CUỐN SÁCH"));
        JPanel scanBox = new JPanel(new BorderLayout(10, 0));
        scanBox.setOpaque(false);
        scanBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtBarcode = new JTextField();
        txtBarcode.setFont(UITheme.FONT_REGULAR);
        txtBarcode.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtBarcode.addActionListener(e -> checkLoanInfo());

        JButton btnScan = UITheme.createPillButton("Kiểm tra phiếu", UITheme.PRIMARY, Color.WHITE);
        btnScan.setPreferredSize(new Dimension(130, 40));
        btnScan.addActionListener(e -> checkLoanInfo());

        scanBox.add(txtBarcode, BorderLayout.CENTER);
        scanBox.add(btnScan, BorderLayout.EAST);
        formBox.add(scanBox);
        formBox.add(Box.createVerticalStrut(14));

        // 2. Thông tin mượn
        formBox.add(createSectionLabel("2. THÔNG TIN PHIẾU MƯỢN HIỆN TẠI"));
        JPanel infoBox = new JPanel(new GridLayout(4, 1, 0, 4));
        infoBox.setBackground(UITheme.BG_LIGHT);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        lblBookTitle = new JLabel("Đầu sách: (Chưa quét)");
        lblBorrower = new JLabel("Người mượn: (Chưa rõ)");
        lblDueDate = new JLabel("Hạn trả: (Chưa rõ)");
        lblOverdueStatus = new JLabel("Tình trạng: -");

        infoBox.add(lblBookTitle);
        infoBox.add(lblBorrower);
        infoBox.add(lblDueDate);
        infoBox.add(lblOverdueStatus);
        formBox.add(infoBox);
        formBox.add(Box.createVerticalStrut(14));

        // 3. Tình trạng hoàn trả
        formBox.add(createSectionLabel("3. TÌNH TRẠNG SÁCH KHI TIẾP NHẬN"));
        radNormal = new JRadioButton("Bình thường (Đạt tiêu chuẩn)", true);
        radDamaged = new JRadioButton("Hỏng nhẹ / Rách bìa (+ 20.000đ phí kỹ thuật BR-08)");
        radLost = new JRadioButton("Báo mất sách (Bồi thường 200% giá bìa + 20.000đ BR-08)");

        ButtonGroup bg = new ButtonGroup();
        bg.add(radNormal);
        bg.add(radDamaged);
        bg.add(radLost);

        radNormal.addActionListener(e -> recalculateFine());
        radDamaged.addActionListener(e -> recalculateFine());
        radLost.addActionListener(e -> recalculateFine());

        formBox.add(radNormal);
        formBox.add(radDamaged);
        formBox.add(radLost);
        formBox.add(Box.createVerticalStrut(14));

        // 4. Bảng tính tiền
        formBox.add(createSectionLabel("4. TỔNG HỢP NGHĨA VỤ TÀI CHÍNH"));
        lblFineDetail = new JLabel("Chi tiết: Trễ hạn 0đ + Phí 0đ");
        lblFineDetail.setFont(UITheme.FONT_SMALL);
        lblFineDetail.setForeground(UITheme.TEXT_MUTED);

        lblTotalAmount = new JLabel("TỔNG TIỀN PHẢI NỘP: 0 VNĐ");
        lblTotalAmount.setFont(UITheme.FONT_TITLE);
        lblTotalAmount.setForeground(UITheme.DANGER_TEXT);

        formBox.add(lblFineDetail);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(lblTotalAmount);
        formBox.add(Box.createVerticalStrut(20));

        btnExecute = UITheme.createPillButton("XÁC NHẬN HOÀN TẤT", UITheme.PRIMARY, Color.WHITE);
        btnExecute.setFont(UITheme.FONT_BOLD);
        btnExecute.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnExecute.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        btnExecute.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExecute.addActionListener(e -> executeReturnOrLoss());
        formBox.add(btnExecute);

        card.add(formBox);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        main.add(card, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void checkLoanInfo() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        BorrowDetailInfo detail = circulationService.findActiveLoanByCopy(barcode);
        if (detail == null) {
            UiModalHelper.showToastError(this, "Không tìm thấy cuốn sách hoặc sách không nằm trong phiếu mượn hoạt động!");
            currentDetail = null;
            return;
        }

        currentDetail = detail;
        lblBookTitle.setText("Đầu sách: " + detail.bookTitle() + " (Mã: " + detail.copyId() + ")");
        lblBorrower.setText("Mã bạn đọc: " + detail.userId() + " | Mã phiếu: " + detail.ticketId());
        lblDueDate.setText("Hạn trả: " + (detail.dueDate() != null ? detail.dueDate().toLocalDate() : "Không rõ"));

        int overdueDays = 0;
        if (detail.dueDate() != null && LocalDateTime.now().isAfter(detail.dueDate())) {
            overdueDays = (int) Math.max(1, ChronoUnit.DAYS.between(detail.dueDate().toLocalDate(), LocalDateTime.now().toLocalDate()));
            lblOverdueStatus.setText("<html><font color='#dc2626'><b>Quá hạn " + overdueDays + " ngày (Phạt 2.000đ/ngày)</b></font></html>");
        } else {
            lblOverdueStatus.setText("<html><font color='#16a34a'>Đang trong thời hạn mượn hợp lệ</font></html>");
        }

        recalculateFine();
    }

    private void recalculateFine() {
        if (currentDetail == null) return;

        LocalDateTime now = LocalDateTime.now();
        int overdueDays = 0;
        BigDecimal overdueFine = BigDecimal.ZERO;
        if (currentDetail.dueDate() != null && now.isAfter(currentDetail.dueDate())) {
            overdueDays = (int) Math.max(1, ChronoUnit.DAYS.between(currentDetail.dueDate().toLocalDate(), now.toLocalDate()));
            overdueFine = new BigDecimal("2000.00").multiply(BigDecimal.valueOf(overdueDays));
        }

        BigDecimal techFee = BigDecimal.ZERO;
        BigDecimal lossAmount = BigDecimal.ZERO;

        if (radDamaged.isSelected()) {
            techFee = new BigDecimal("20000.00");
        } else if (radLost.isSelected()) {
            BigDecimal cover = currentDetail.coverPrice() != null ? currentDetail.coverPrice() : new BigDecimal("50000.00");
            lossAmount = cover.multiply(new BigDecimal("2.0"));
            techFee = new BigDecimal("20000.00");
        }

        BigDecimal total = overdueFine.add(techFee).add(lossAmount);
        lblFineDetail.setText("Phạt trễ: " + currencyFormat.format(overdueFine) + " + Phí: " + currencyFormat.format(techFee)
            + (lossAmount.compareTo(BigDecimal.ZERO) > 0 ? " + Đền bù mất sách (200%): " + currencyFormat.format(lossAmount) : ""));
        lblTotalAmount.setText("TỔNG TIỀN PHẢI NỘP: " + currencyFormat.format(total));
    }

    private void executeReturnOrLoss() {
        if (currentDetail == null) {
            UiModalHelper.showToastWarning(this, "Vui lòng quét mã cuốn sách cần xử lý!");
            return;
        }

        String barcode = currentDetail.copyId();

        if (radLost.isSelected()) {
            CompensationResultDto loss = circulationService.reportLoss(barcode);
            com.ximilib.presentation.component.VietQrModalPanel.show(this, loss.getFineTicketId(), loss.getTotalAmount(), () -> {
                circulationService.payFine(loss.getFineTicketId(), "Chuyển khoản VietQR");
                UiModalHelper.showToastSuccess(this, "Đã lập biên bản bồi thường và hoàn tất thanh toán VietQR: " + currencyFormat.format(loss.getTotalAmount()));
                resetForm();
            });
        } else {
            String cond = radDamaged.isSelected() ? "Hỏng nhẹ" : "Bình thường";
            ReturnResultDto ret = circulationService.returnBook(barcode, cond);
            if (ret.getTotalFine() != null && ret.getTotalFine().compareTo(BigDecimal.ZERO) > 0) {
                UiModalHelper.showConfirm(this, "Thu tiền phạt", "Tổng tiền phạt là " + currencyFormat.format(ret.getTotalFine()) + ". Mở mã VietQR để thanh toán?", () -> {
                    com.ximilib.presentation.component.VietQrModalPanel.show(this, "P-" + barcode, ret.getTotalFine(), () -> {
                        UiModalHelper.showToastSuccess(this, "Đã thu hồi sách và thu phạt qua VietQR thành công!");
                        resetForm();
                    });
                });
            } else {
                UiModalHelper.showToastSuccess(this, "Đã thu hồi sách thành công! Không có tiền phạt.");
                resetForm();
            }
        }
    }

    private void resetForm() {
        currentDetail = null;
        txtBarcode.setText("");
        lblBookTitle.setText("Đầu sách: (Chưa quét)");
        lblBorrower.setText("Người mượn: (Chưa rõ)");
        lblDueDate.setText("Hạn trả: (Chưa rõ)");
        lblOverdueStatus.setText("Tình trạng: -");
        radNormal.setSelected(true);
        lblFineDetail.setText("Chi tiết: Trễ hạn 0đ + Phí 0đ");
        lblTotalAmount.setText("TỔNG TIỀN PHẢI NỘP: 0 VNĐ");
    }
}
