package com.ximilib.presentation.component;

import com.ximilib.infrastructure.config.DatabaseConfig;
import com.ximilib.presentation.util.QrCodeGenerator;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.listener.ModalController;
import raven.modal.component.SimpleModalBorder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VietQrModalPanel extends JPanel {

    private final String fineTicketId;
    private final BigDecimal amount;
    private final Runnable onSuccessCallback;

    private final String bankId;
    private final String accountNo;
    private final String accountName;
    private final String sepayApiToken;

    private JLabel qrImageLabel;
    private JLabel lblPollStatus;
    private Timer pollTimer;
    private ModalController modalController;
    private volatile boolean paymentVerified = false;
    private volatile boolean isChecking = false;
    private int pollCount = 0;

    public VietQrModalPanel(String fineTicketId, BigDecimal amount, Runnable onSuccessCallback) {
        this.fineTicketId = fineTicketId;
        this.amount = amount;
        this.onSuccessCallback = onSuccessCallback;

        String cfgBank = "MB";
        String cfgAcc = "99924052005";
        String cfgName = "LE VO THANH NAM";
        String cfgToken = "";
        try {
            DatabaseConfig cfg = DatabaseConfig.getInstance();
            cfgBank = cfg.getSepayBankCode();
            cfgAcc = cfg.getSepayAccountNo();
            cfgName = cfg.getSepayAccountName();
            cfgToken = cfg.getSepayApiToken();
        } catch (Exception ignored) {}

        this.bankId = cfgBank;
        this.accountNo = cfgAcc;
        this.accountName = cfgName;
        this.sepayApiToken = cfgToken;

        setPreferredSize(new Dimension(460, 535));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 20, 16, 20));

        initUI();
        loadQrImageAsync();
        startPaymentPolling();
    }

    private void initUI() {

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

        JPanel qrContainer = new JPanel(new GridBagLayout());
        qrContainer.setBackground(Color.WHITE);
        qrContainer.setPreferredSize(new Dimension(260, 260));
        qrContainer.setMaximumSize(new Dimension(260, 260));
        qrContainer.setMinimumSize(new Dimension(260, 260));
        qrContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        String payload = "VIETQR:" + bankId + "|" + accountNo + "|" + (amount != null ? amount.longValue() : 0) + "|XML" + fineTicketId;
        BufferedImage initialQr = generateQrImage(payload, 240);

        qrImageLabel = new JLabel(new ImageIcon(initialQr), SwingConstants.CENTER);
        qrImageLabel.setPreferredSize(new Dimension(240, 240));
        qrImageLabel.setMaximumSize(new Dimension(240, 240));
        qrImageLabel.setMinimumSize(new Dimension(240, 240));
        qrContainer.add(qrImageLabel);

        add(qrContainer, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel(new GridLayout(5, 1, 4, 4));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        detailsPanel.add(createRow("Ngân hàng:", bankId + " Bank"));
        detailsPanel.add(createRow("Số tài khoản:", accountNo + " (" + accountName + ")"));
        detailsPanel.add(createRow("Nội dung CK:", "XML" + fineTicketId));
        detailsPanel.add(createRow("Cổng SePay:", "Napas247 Tự Động"));

        String initPollText = sepayApiToken.isBlank()
            ? "Chế độ quầy: Thủ thư đối soát và xác nhận thu tiền."
            : "Đang chờ xác nhận giao dịch tự động từ SePay...";
        lblPollStatus = new JLabel(initPollText, SwingConstants.CENTER);
        lblPollStatus.setFont(UITheme.FONT_SMALL);
        lblPollStatus.setForeground(UITheme.PRIMARY);
        detailsPanel.add(lblPollStatus);

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

    private static BufferedImage generateQrImage(String text, int size) {
        boolean[][] matrix = QrCodeGenerator.encode(text);
        int mSize = matrix.length;
        int padding = 14;
        int available = size - padding * 2;
        int moduleSize = Math.max(2, available / mSize);
        int qrPixelWidth = mSize * moduleSize;
        int startX = (size - qrPixelWidth) / 2;
        int startY = (size - qrPixelWidth) / 2;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, size, size);

        g2.setColor(Color.BLACK);
        for (int r = 0; r < mSize; r++) {
            for (int c = 0; c < mSize; c++) {
                if (matrix[r][c]) {
                    g2.fillRect(startX + c * moduleSize, startY + r * moduleSize, moduleSize, moduleSize);
                }
            }
        }

        int logoSize = moduleSize * 5;
        if (logoSize >= 18) {
            int lx = (size - logoSize) / 2;
            int ly = (size - logoSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(lx - 2, ly - 2, logoSize + 4, logoSize + 4, 6, 6);
            g2.setColor(new Color(225, 29, 72));
            g2.fillRoundRect(lx, ly, logoSize, logoSize, 4, 4);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(9, logoSize / 2)));
            FontMetrics fm = g2.getFontMetrics();
            String badgeText = "MB";
            int tx = lx + (logoSize - fm.stringWidth(badgeText)) / 2;
            int ty = ly + ((logoSize - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(badgeText, tx, ty);
        }

        g2.dispose();
        return image;
    }

    private void loadQrImageAsync() {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    String content = "XML" + fineTicketId;
                    String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
                    String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
                    long amountLong = amount != null ? amount.longValue() : 0;

                    String qrUrl = String.format(
                        "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                        bankId, accountNo, amountLong, encodedContent, encodedName
                    );

                    URL url = URI.create(qrUrl).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                        String newUrl = conn.getHeaderField("Location");
                        if (newUrl != null && !newUrl.isBlank()) {
                            conn = (HttpURLConnection) URI.create(newUrl).toURL().openConnection();
                            conn.setConnectTimeout(4000);
                            conn.setReadTimeout(4000);
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        }
                    }

                    BufferedImage img = ImageIO.read(conn.getInputStream());
                    if (img != null) {
                        Image scaled = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                } catch (Exception ignored) {

                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon onlineIcon = get();
                    if (onlineIcon != null) {
                        qrImageLabel.setIcon(onlineIcon);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    @Override
    public void removeNotify() {
        stopPaymentPolling();
        super.removeNotify();
    }

    private void startPaymentPolling() {
        if (sepayApiToken == null || sepayApiToken.isBlank()) {
            return;
        }
        pollTimer = new Timer(3000, e -> checkSepayTransactionAsync());
        pollTimer.setInitialDelay(3000);
        pollTimer.start();
    }

    private void stopPaymentPolling() {
        if (pollTimer != null && pollTimer.isRunning()) {
            pollTimer.stop();
        }
    }

    private void checkSepayTransactionAsync() {
        if (paymentVerified || isChecking) {
            return;
        }
        pollCount++;
        if (pollCount > 60) {
            stopPaymentPolling();
            if (lblPollStatus != null) {
                lblPollStatus.setText("Hết thời gian chờ tự động (3 phút). Vui lòng thử lại.");
                lblPollStatus.setForeground(UITheme.DANGER_TEXT);
            }
            return;
        }
        isChecking = true;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return verifyTransactionWithSepay();
            }

            @Override
            protected void done() {
                isChecking = false;
                try {
                    if (Boolean.TRUE.equals(get()) && !paymentVerified) {
                        paymentVerified = true;
                        stopPaymentPolling();
                        if (modalController != null) {
                            modalController.close();
                        }
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private boolean verifyTransactionWithSepay() {
        try {
            URL url = URI.create("https://my.sepay.vn/userapi/transactions/list?limit=20").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestProperty("Authorization", "Bearer " + sepayApiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            if (conn.getResponseCode() != 200) {
                return false;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return matchesPaymentCodeAndAmount(sb.toString(), "XML" + fineTicketId, amount);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean matchesPaymentCodeAndAmount(String json, String paymentCode, BigDecimal expectedAmount) {
        if (json == null || json.isBlank()) {
            return false;
        }
        Pattern codePattern = Pattern.compile("(?<![A-Za-z0-9])" + Pattern.quote(paymentCode) + "(?![A-Za-z0-9])");
        String[] blocks = json.split("\\{");
        for (String block : blocks) {
            if (!codePattern.matcher(block).find()) {
                continue;
            }
            Matcher amtMatcher = Pattern.compile("\"amount_in\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?").matcher(block);
            if (amtMatcher.find()) {
                BigDecimal txAmount = new BigDecimal(amtMatcher.group(1));
                BigDecimal target = expectedAmount != null ? expectedAmount : BigDecimal.ZERO;
                if (txAmount.compareTo(target) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void show(Component parent, String fineTicketId, BigDecimal amount, Runnable onConfirmed) {
        show(parent, fineTicketId, amount, false, onConfirmed);
    }

    public static void show(Component parent, String fineTicketId, BigDecimal amount, boolean allowManualConfirm, Runnable onConfirmed) {
        Component owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        if (owner == null) owner = parent;

        VietQrModalPanel qrPanel = new VietQrModalPanel(fineTicketId, amount, onConfirmed);
        UiModalHelper.showCustomModal(
            owner,
            "VIETQR_PAYMENT",
            "Thanh Toán VietQR Napas247",
            qrPanel,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                qrPanel.modalController = controller;
                if (action == SimpleModalBorder.OK_OPTION) {
                    if (qrPanel.paymentVerified) {
                        qrPanel.stopPaymentPolling();
                        controller.close();
                        return;
                    }
                    if (allowManualConfirm) {
                        qrPanel.paymentVerified = true;
                        qrPanel.stopPaymentPolling();
                        controller.close();
                        if (onConfirmed != null) {
                            onConfirmed.run();
                        }
                    } else if (qrPanel.sepayApiToken.isBlank()) {
                        UiModalHelper.showToastWarning(qrPanel, "Chưa cấu hình SePay API Token. Vui lòng thanh toán trực tiếp tại quầy thủ thư.");
                    } else {
                        UiModalHelper.showToastWarning(qrPanel, "Chưa nhận được giao dịch chuyển khoản khớp mã XML" + fineTicketId + ".");
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    qrPanel.stopPaymentPolling();
                    controller.close();
                }
            }
        );
    }
}
