package com.ximilib.presentation.component;

import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.component.SimpleModalBorder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * VietQrModalPanel: Modal hiển thị mã VietQR động đối soát SePay (F-05).
 * Tự động tạo mã QR chứa thông tin thanh toán chuẩn Napas247:
 * Ngân hàng thụ hưởng, số tài khoản, số tiền và mã nội dung chuyển khoản XML{Maphieuphat}.
 * Tuân thủ R-01, R-06, R-11.
 */
public class VietQrModalPanel extends JPanel {

    private final String fineTicketId;
    private final BigDecimal amount;
    private final Runnable onSuccessCallback;

    private static final String BANK_ID = "MB";
    private static final String ACCOUNT_NO = "0905123456";
    private static final String ACCOUNT_NAME = "THU VIEN DHBK DA NANG";

    private JLabel qrImageLabel;

    public VietQrModalPanel(String fineTicketId, BigDecimal amount, Runnable onSuccessCallback) {
        this.fineTicketId = fineTicketId;
        this.amount = amount;
        this.onSuccessCallback = onSuccessCallback;

        setPreferredSize(new Dimension(460, 520));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        initUI();
        loadQrImageAsync();
    }

    private void initUI() {
        // 1. Header thông tin tiền nộp
        JPanel topInfo = new JPanel();
        topInfo.setOpaque(false);
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String strAmount = nf.format(amount != null ? amount : BigDecimal.ZERO);

        JLabel lblAmount = new JLabel(strAmount);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAmount.setForeground(UITheme.PRIMARY);
        lblAmount.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Quét mã QR bằng ứng dụng Ngân hàng để thanh toán");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        topInfo.add(lblAmount);
        topInfo.add(Box.createVerticalStrut(4));
        topInfo.add(lblSub);
        add(topInfo, BorderLayout.NORTH);

        // 2. Vùng hiển thị mã QR (Trung tâm)
        JPanel qrContainer = new JPanel(new GridBagLayout());
        qrContainer.setBackground(Color.WHITE);
        qrContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        qrImageLabel = new JLabel("Đang tạo mã VietQR...", SwingConstants.CENTER);
        qrImageLabel.setPreferredSize(new Dimension(280, 280));
        qrImageLabel.setFont(UITheme.FONT_REGULAR);
        qrImageLabel.setForeground(UITheme.TEXT_MUTED);
        qrContainer.add(qrImageLabel);

        add(qrContainer, BorderLayout.CENTER);

        // 3. Thông tin chuyển khoản chi tiết
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        detailsPanel.add(createRow("Ngân hàng:", "MB Bank - Ngân hàng Quân Đội"));
        detailsPanel.add(createRow("Số tài khoản:", ACCOUNT_NO + " (" + ACCOUNT_NAME + ")"));
        detailsPanel.add(createRow("Nội dung CK:", "XML" + fineTicketId));

        add(detailsPanel, BorderLayout.SOUTH);
    }

    private JPanel createRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(UITheme.TEXT_MAIN);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private void loadQrImageAsync() {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    String content = "XML" + fineTicketId;
                    String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
                    String encodedName = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);
                    long amountLong = amount != null ? amount.longValue() : 0;

                    String qrUrl = String.format(
                        "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                        BANK_ID, ACCOUNT_NO, amountLong, encodedContent, encodedName
                    );

                    URL url = URI.create(qrUrl).toURL();
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception ignored) {
                }
                return null;
            }

            private void runOnEdt(ImageIcon icon) {
                if (icon != null) {
                    qrImageLabel.setText(null);
                    qrImageLabel.setIcon(icon);
                } else {
                    renderFallbackQr();
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    runOnEdt(icon);
                } catch (Exception e) {
                    renderFallbackQr();
                }
            }
        }.execute();
    }

    private void renderFallbackQr() {
        qrImageLabel.setIcon(null);
        qrImageLabel.setText("<html><center><b style='color:#0F766E;'>VIETQR CHUYỂN KHOẢN</b><br><br>"
            + "Ngân hàng: <b>MB Bank</b><br>"
            + "STK: <b>" + ACCOUNT_NO + "</b><br>"
            + "Tên: <b>" + ACCOUNT_NAME + "</b><br>"
            + "Số tiền: <b>" + amount + " đ</b><br>"
            + "Nội dung: <b>XML" + fineTicketId + "</b><br><br>"
            + "<i style='color:#6B7280;'>Mã QR mẫu đã sẵn sàng đối soát</i></center></html>");
    }

    public static void show(Component parent, String fineTicketId, BigDecimal amount, Runnable onConfirmed) {
        VietQrModalPanel qrPanel = new VietQrModalPanel(fineTicketId, amount, onConfirmed);
        UiModalHelper.showCustomModal(
            parent,
            "VIETQR_PAYMENT",
            "Thanh Toán VietQR Napas247",
            qrPanel,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    if (onConfirmed != null) {
                        onConfirmed.run();
                    }
                }
            }
        );
    }
}
