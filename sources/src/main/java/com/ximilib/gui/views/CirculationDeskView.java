package com.ximilib.gui.views;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository.BookCopyInfo;
import com.ximilib.domain.repository.CirculationRepository.UserCardInfo;
import com.ximilib.gui.util.UITheme;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CirculationDeskView: Bàn Lưu Hành Mượn Sách Tại Quầy Thủ Thư (UC-6).
 * Quét thẻ độc giả, quét mã vạch cuốn sách, chọn hình thức mượn (Tại chỗ / Về nhà).
 * Tuân thủ R-01, R-06, BR-02, BR-04, BR-07.
 */
public class CirculationDeskView extends JPanel {

    private final CirculationService circulationService;

    // Components
    private JTextField txtReaderCard;
    private JLabel lblReaderInfo;
    private UserCardInfo currentCard;

    private JTextField txtBarcode;
    private JComboBox<String> cmbLoanType;
    private DefaultTableModel cartTableModel;
    private final List<String> cartBarcodes = new ArrayList<>();

    public CirculationDeskView() {
        this(AppContainer.getInstance().getCirculationService());
    }

    public CirculationDeskView(CirculationService service) {
        this.circulationService = service != null ? service : AppContainer.getInstance().getCirculationService();

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
    }

    private void initUI() {
        // Tiêu đề
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("Bàn lưu hành sách (Circulation Desk)");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Xử lý lập phiếu mượn sách tại quầy cho độc giả • Tự động tính hạn trả theo quy định");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        // Khối Card chính
        JPanel card = new JPanel(new BorderLayout(20, 0)) {
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
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Nửa trái: Form nhập thẻ + quét sách
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(380, 0));

        // 1. Quét thẻ độc giả
        leftPanel.add(createSectionLabel("1. THẺ THƯ VIỆN BẠN ĐỌC"));
        JPanel readerInputBox = new JPanel(new BorderLayout(8, 0));
        readerInputBox.setOpaque(false);
        readerInputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtReaderCard = new JTextField("LIB-2026-00001");
        txtReaderCard.setFont(UITheme.FONT_REGULAR);
        txtReaderCard.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));

        JButton btnCheckReader = UITheme.createPillButton("Kiểm tra", UITheme.PRIMARY, Color.WHITE);
        btnCheckReader.setPreferredSize(new Dimension(90, 36));
        btnCheckReader.addActionListener(e -> checkReaderCard());

        readerInputBox.add(txtReaderCard, BorderLayout.CENTER);
        readerInputBox.add(btnCheckReader, BorderLayout.EAST);
        leftPanel.add(readerInputBox);

        lblReaderInfo = new JLabel("Chưa kiểm tra thẻ bạn đọc");
        lblReaderInfo.setFont(UITheme.FONT_SMALL);
        lblReaderInfo.setForeground(UITheme.TEXT_MUTED);
        lblReaderInfo.setBorder(BorderFactory.createEmptyBorder(6, 4, 14, 4));
        leftPanel.add(lblReaderInfo);

        // 2. Hình thức mượn
        leftPanel.add(createSectionLabel("2. HÌNH THỨC MƯỢN"));
        cmbLoanType = new JComboBox<>(new String[]{"Mang về nhà (Hạn 14 ngày, tối đa 3 cuốn)", "Đọc tại chỗ (Trả trước 17:00 hôm nay)"});
        cmbLoanType.setFont(UITheme.FONT_REGULAR);
        cmbLoanType.setBackground(Color.WHITE);
        cmbLoanType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        leftPanel.add(cmbLoanType);
        leftPanel.add(Box.createVerticalStrut(14));

        // 3. Quét mã vạch cuốn sách
        leftPanel.add(createSectionLabel("3. QUÉT MÃ VẠCH CUỐN SÁCH"));
        JPanel barcodeInputBox = new JPanel(new BorderLayout(8, 0));
        barcodeInputBox.setOpaque(false);
        barcodeInputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        txtBarcode = new JTextField();
        txtBarcode.setFont(UITheme.FONT_REGULAR);
        txtBarcode.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtBarcode.addActionListener(e -> addBarcodeToCart());

        JButton btnAddBook = UITheme.createPillButton("+ Thêm", UITheme.PRIMARY_TINT, UITheme.ON_TINT);
        btnAddBook.setPreferredSize(new Dimension(80, 36));
        btnAddBook.addActionListener(e -> addBarcodeToCart());

        barcodeInputBox.add(txtBarcode, BorderLayout.CENTER);
        barcodeInputBox.add(btnAddBook, BorderLayout.EAST);
        leftPanel.add(barcodeInputBox);

        leftPanel.add(Box.createVerticalGlue());

        JButton btnConfirm = UITheme.createPillButton("XÁC NHẬN CHO MƯỢN", UITheme.PRIMARY, Color.WHITE);
        btnConfirm.setFont(UITheme.FONT_BOLD);
        btnConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConfirm.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirm.addActionListener(e -> handleConfirmLoan());
        leftPanel.add(btnConfirm);

        // Nửa phải: Bảng danh sách sách trong phiếu
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);

        JLabel lblCartTitle = new JLabel("Danh sách cuốn sách chuẩn bị mượn");
        lblCartTitle.setFont(UITheme.FONT_TITLE);
        lblCartTitle.setForeground(UITheme.TEXT_MAIN);
        rightPanel.add(lblCartTitle, BorderLayout.NORTH);

        String[] cols = {"Mã vạch cuốn", "Tên đầu sách", "Vị trí kệ sách", "Tình trạng"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(42);
        cartTable.setShowHorizontalLines(true);
        cartTable.setShowVerticalLines(false);
        cartTable.setGridColor(UITheme.BORDER);
        cartTable.setFont(UITheme.FONT_REGULAR);
        cartTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        cartTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        cartTable.getTableHeader().setForeground(UITheme.TEXT_MUTED);

        JScrollPane scrollTable = new JScrollPane(cartTable);
        scrollTable.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UITheme.BORDER));
        scrollTable.getViewport().setBackground(Color.WHITE);
        rightPanel.add(scrollTable, BorderLayout.CENTER);

        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);

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
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 2, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void checkReaderCard() {
        String idOrCard = txtReaderCard.getText().trim();
        if (idOrCard.isEmpty()) return;

        UserCardInfo info = circulationService.getCardInfo(idOrCard);
        if (info == null) {
            lblReaderInfo.setText("<html><font color='#dc2626'>Không tìm thấy bạn đọc này trong CSDL!</font></html>");
            currentCard = null;
            return;
        }

        currentCard = info;
        if (!info.cardActive()) {
            lblReaderInfo.setText("<html><font color='#dc2626'>Thẻ đang bị khóa! Bạn đọc: " + info.fullName() + "</font></html>");
        } else if (info.cardExpired()) {
            lblReaderInfo.setText("<html><font color='#dc2626'>Thẻ đã hết hạn! Bạn đọc: " + info.fullName() + "</font></html>");
        } else {
            lblReaderInfo.setText("<html><font color='#16a34a'>Hợp lệ: <b>" + info.fullName() + "</b> (Thẻ: " + info.cardNumber() + ")</font></html>");
        }
    }

    private void addBarcodeToCart() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;

        if (cartBarcodes.contains(barcode)) {
            UiModalHelper.showToastWarning(this, "Cuốn sách này đã có trong danh sách mượn!");
            txtBarcode.setText("");
            return;
        }

        BookCopyInfo copy = circulationService.getCopyInfo(barcode);
        if (copy == null) {
            UiModalHelper.showToastError(this, "Không tìm thấy cuốn sách với mã vạch: " + barcode);
            return;
        }

        if (!"KHA_DUNG".equalsIgnoreCase(copy.status())) {
            UiModalHelper.showToastWarning(this, "Cuốn sách đang ở trạng thái: " + copy.status() + ", không thể cho mượn!");
            return;
        }

        cartBarcodes.add(barcode);
        cartTableModel.addRow(new Object[]{copy.copyId(), copy.bookTitle(), copy.shelfLocation(), copy.status()});
        txtBarcode.setText("");
    }

    private void handleConfirmLoan() {
        if (currentCard == null) {
            UiModalHelper.showToastWarning(this, "Vui lòng kiểm tra thẻ bạn đọc trước khi cho mượn!");
            return;
        }
        if (cartBarcodes.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng quét ít nhất 1 cuốn sách vào danh sách!");
            return;
        }

        LoanType lt = (cmbLoanType.getSelectedIndex() == 0) ? LoanType.VE_NHA : LoanType.TAI_CHO;

        SwingWorker<BorrowTicketDto, Void> worker = new SwingWorker<>() {
            @Override
            protected BorrowTicketDto doInBackground() {
                return circulationService.createLoan(currentCard.cardNumber(), cartBarcodes, lt, "Mượn tại bàn lưu hành");
            }

            @Override
            protected void done() {
                try {
                    BorrowTicketDto ticket = get();
                    SwingUtilities.invokeLater(() -> {
                        UiModalHelper.showToastSuccess(CirculationDeskView.this, "Lập phiếu mượn thành công! Mã: " + ticket.getTicketId());
                        cartBarcodes.clear();
                        cartTableModel.setRowCount(0);
                    });
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    SwingUtilities.invokeLater(() -> UiModalHelper.showToastError(CirculationDeskView.this, cause.getMessage()));
                }
            }
        };
        worker.execute();
    }
}
