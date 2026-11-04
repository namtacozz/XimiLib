package com.ximilib.presentation.view;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.PagedResult;
import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.service.BookCatalogService;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.model.LoanType;
import com.ximilib.domain.repository.CirculationRepository.BookCopyInfo;
import com.ximilib.domain.repository.CirculationRepository.UserCardInfo;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiText;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CirculationDeskView extends JPanel {

    private final CirculationService circulationService;
    private final BookCatalogService bookCatalogService;

    private JTextField txtReaderCard;
    private JPanel pnlReaderCard;
    private JLabel lblReaderName;
    private JLabel lblReaderSub;
    private JLabel lblReaderBadge;
    private UserCardInfo currentCard;

    private JToggleButton btnMangVe;
    private JToggleButton btnTaiCho;
    private JLabel lblLoanHint;

    private JTextField txtBarcode;
    private JLabel lblBarcodeNote;
    private JLabel lblCartCounter;
    private Timer readerSuggestTimer;

    private JLabel lblDueDateSummary;
    private JLabel lblReaderSummary;

    private JLabel lblCartCountBadge;
    private JButton btnRemoveSelected;
    private JButton btnClearAll;
    private CardLayout rightCardLayout;
    private JPanel rightCenterPanel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private final List<String> cartBarcodes = new ArrayList<>();

    public CirculationDeskView() {
        this(AppContainer.getInstance().getCirculationService(), AppContainer.getInstance().getBookCatalogService());
    }

    public CirculationDeskView(CirculationService service) {
        this(service, AppContainer.getInstance().getBookCatalogService());
    }

    public CirculationDeskView(CirculationService service, BookCatalogService catalogService) {
        this.circulationService = service != null ? service : AppContainer.getInstance().getCirculationService();
        this.bookCatalogService = catalogService != null ? catalogService : AppContainer.getInstance().getBookCatalogService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
    }

    private void initUI() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Bàn lưu hành sách");
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel("Xử lý lập phiếu mượn sách tại quầy cho độc giả • Tự động tính hạn trả theo quy định");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout(18, 0));
        mainSplit.setOpaque(false);

        mainSplit.add(createLeftFormCard(), BorderLayout.WEST);
        mainSplit.add(createRightTableCard(), BorderLayout.CENTER);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createLeftFormCard() {
        JPanel card = createBaseCard();
        card.setPreferredSize(new Dimension(420, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblCardTitle = new JLabel("Thông Tin Phiếu Mượn");
        lblCardTitle.setFont(UITheme.FONT_TITLE);
        lblCardTitle.setForeground(UITheme.TEXT_MAIN);
        lblCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCardTitle);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(1, "THẺ THƯ VIỆN BẠN ĐỌC"));
        card.add(Box.createVerticalStrut(6));

        JPanel readerInputBox = new JPanel(new BorderLayout(8, 0));
        readerInputBox.setOpaque(false);
        readerInputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        readerInputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtReaderCard = new JTextField("LIB-2026-00001");
        txtReaderCard.setFont(UITheme.FONT_REGULAR);
        txtReaderCard.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtReaderCard.putClientProperty("JTextField.placeholderText", UiText.PH_SEARCH_READER);
        txtReaderCard.addActionListener(e -> checkReaderCard());

        JPopupMenu suggestPopup = new JPopupMenu();
        suggestPopup.setFocusable(false);
        txtReaderCard.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerReaderSuggest(suggestPopup); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerReaderSuggest(suggestPopup); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerReaderSuggest(suggestPopup); }
        });
        txtReaderCard.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                Timer t = new Timer(200, ev -> suggestPopup.setVisible(false));
                t.setRepeats(false);
                t.start();
            }
        });

        JButton btnCheckReader = UITheme.createPillButton(" " + UiText.BTN_CHECK, UITheme.PRIMARY, Color.WHITE);
        btnCheckReader.setIcon(createSearchIcon(13, Color.WHITE));
        btnCheckReader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCheckReader.setPreferredSize(new Dimension(112, 36));
        btnCheckReader.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckReader.addActionListener(e -> checkReaderCard());

        readerInputBox.add(txtReaderCard, BorderLayout.CENTER);
        readerInputBox.add(btnCheckReader, BorderLayout.EAST);
        card.add(readerInputBox);
        card.add(Box.createVerticalStrut(8));

        pnlReaderCard = createReaderStatusCard();
        pnlReaderCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pnlReaderCard);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(2, "HÌNH THỨC MƯỢN"));
        card.add(Box.createVerticalStrut(6));

        btnMangVe = new JToggleButton(UiText.TOGGLE_HOME_LOAN);
        btnTaiCho = new JToggleButton(UiText.TOGGLE_ONSITE_LOAN);
        btnMangVe.setFont(UITheme.FONT_SMALL);
        btnTaiCho.setFont(UITheme.FONT_SMALL);
        btnMangVe.setFocusPainted(false);
        btnTaiCho.setFocusPainted(false);
        btnMangVe.setMargin(new Insets(4, 4, 4, 4));
        btnTaiCho.setMargin(new Insets(4, 4, 4, 4));
        btnMangVe.putClientProperty("JComponent.roundRect", true);
        btnTaiCho.putClientProperty("JComponent.roundRect", true);
        btnMangVe.setSelected(true);

        ButtonGroup grp = new ButtonGroup();
        grp.add(btnMangVe);
        grp.add(btnTaiCho);

        btnMangVe.addActionListener(e -> updateLoanTypeToggleUI());
        btnTaiCho.addActionListener(e -> updateLoanTypeToggleUI());

        JPanel loanTypeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        loanTypeRow.setOpaque(false);
        loanTypeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        loanTypeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loanTypeRow.setPreferredSize(new Dimension(320, 42));
        loanTypeRow.add(btnMangVe);
        loanTypeRow.add(btnTaiCho);
        card.add(loanTypeRow);
        card.add(Box.createVerticalStrut(6));

        lblLoanHint = new JLabel();
        lblLoanHint.setFont(UITheme.FONT_SMALL);
        lblLoanHint.setForeground(UITheme.TEXT_MUTED);
        lblLoanHint.setIconTextGap(6);
        lblLoanHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateLoanTypeToggleUI();
        card.add(lblLoanHint);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(3, "QUÉT MÃ VẠCH CUỐN SÁCH"));
        card.add(Box.createVerticalStrut(6));

        JPanel barcodeInputBox = new JPanel(new BorderLayout(8, 0));
        barcodeInputBox.setOpaque(false);
        barcodeInputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        barcodeInputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtBarcode = new JTextField();
        txtBarcode.setFont(UITheme.FONT_REGULAR);
        txtBarcode.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtBarcode.putClientProperty("JTextField.placeholderText", UiText.PH_SEARCH_BOOK_COPY);
        txtBarcode.addActionListener(e -> addBarcodeToCart());

        JButton btnAddBook = UITheme.createPillButton(" Thêm", UITheme.TINT, UITheme.ON_TINT);
        btnAddBook.setIcon(createPlusIcon(14, UITheme.ON_TINT));
        btnAddBook.setFont(UITheme.FONT_SMALL);
        btnAddBook.setPreferredSize(new Dimension(90, 36));
        btnAddBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddBook.addActionListener(e -> addBarcodeToCart());

        barcodeInputBox.add(txtBarcode, BorderLayout.CENTER);
        barcodeInputBox.add(btnAddBook, BorderLayout.EAST);
        card.add(barcodeInputBox);
        card.add(Box.createVerticalStrut(6));

        lblBarcodeNote = new JLabel("Đặt con trỏ vào ô và quét mã, hệ thống sẽ tự động thêm vào giỏ.");
        lblBarcodeNote.setIcon(createInfoIcon(14, UITheme.TEXT_MUTED));
        lblBarcodeNote.setIconTextGap(6);
        lblBarcodeNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBarcodeNote.setForeground(UITheme.TEXT_MUTED);
        lblBarcodeNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblBarcodeNote);

        card.add(Box.createVerticalStrut(14));

        JPanel summaryInfoBox = new JPanel(new GridLayout(3, 1, 0, 4));
        summaryInfoBox.setOpaque(false);
        summaryInfoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        summaryInfoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        summaryInfoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRow1 = new JLabel("• Ngày lập phiếu: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblRow1.setFont(UITheme.FONT_SMALL);
        lblRow1.setForeground(UITheme.TEXT_MUTED);

        lblDueDateSummary = new JLabel("• Hạn hoàn trả: " + LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblDueDateSummary.setFont(UITheme.FONT_SMALL);
        lblDueDateSummary.setForeground(UITheme.TEXT_MUTED);

        lblReaderSummary = new JLabel("• Bạn đọc: Chưa xác thực thẻ");
        lblReaderSummary.setFont(UITheme.FONT_SMALL);
        lblReaderSummary.setForeground(UITheme.TEXT_MUTED);

        summaryInfoBox.add(lblRow1);
        summaryInfoBox.add(lblDueDateSummary);
        summaryInfoBox.add(lblReaderSummary);
        card.add(summaryInfoBox);

        card.add(Box.createVerticalGlue());

        JPanel summaryBox = new JPanel();
        summaryBox.setOpaque(false);
        summaryBox.setLayout(new BoxLayout(summaryBox, BoxLayout.Y_AXIS));
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));
        summaryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        summaryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel counterRow = new JPanel(new BorderLayout());
        counterRow.setOpaque(false);
        counterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lblCounterTitle = new JLabel("Số lượng sách đã chọn:");
        lblCounterTitle.setFont(UITheme.FONT_SMALL);
        lblCounterTitle.setForeground(UITheme.TEXT_MUTED);

        lblCartCounter = new JLabel("0 / 3 cuốn");
        lblCartCounter.setFont(UITheme.FONT_BOLD);
        lblCartCounter.setForeground(UITheme.PRIMARY);

        counterRow.add(lblCounterTitle, BorderLayout.WEST);
        counterRow.add(lblCartCounter, BorderLayout.EAST);
        summaryBox.add(counterRow);
        summaryBox.add(Box.createVerticalStrut(12));

        JButton btnConfirm = UITheme.createPillButton(" XÁC NHẬN CHO MƯỢN", UITheme.PRIMARY, Color.WHITE);
        btnConfirm.setIcon(createCheckIcon(16, Color.WHITE));
        btnConfirm.setFont(UITheme.FONT_BOLD);
        btnConfirm.setPreferredSize(new Dimension(310, 44));
        btnConfirm.setMaximumSize(new Dimension(310, 44));
        btnConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> handleConfirmLoan());
        summaryBox.add(btnConfirm);
        summaryBox.add(Box.createVerticalStrut(8));

        JButton btnReset = UITheme.createPillButton(" Làm Mới Phiếu", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnReset.setIcon(createRefreshIcon(16, UITheme.TEXT_MAIN));
        btnReset.setFont(UITheme.FONT_SMALL);
        btnReset.setPreferredSize(new Dimension(310, 36));
        btnReset.setMaximumSize(new Dimension(310, 36));
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetForm());
        summaryBox.add(btnReset);

        card.add(summaryBox);

        return card;
    }

    private JPanel createReaderStatusCard() {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = (currentCard != null && currentCard.cardActive() && !currentCard.cardExpired())
                        ? UITheme.SUCCESS_BG : (currentCard != null ? UITheme.DANGER_BG : UITheme.BG_LIGHT);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color border = (currentCard != null && currentCard.cardActive() && !currentCard.cardExpired())
                        ? UITheme.SUCCESS_GREEN : (currentCard != null ? UITheme.DANGER_TEXT : UITheme.BORDER);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel avatarIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());

                if (currentCard != null && currentCard.fullName() != null && !currentCard.fullName().isBlank()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    String initial = String.valueOf(currentCard.fullName().charAt(0)).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                } else {

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawOval(cx - 5, cy - 9, 10, 10);
                    g2.drawArc(cx - 9, cy + 3, 18, 14, 0, 180);
                }
                g2.dispose();
            }
        };
        avatarIcon.setPreferredSize(new Dimension(36, 36));
        avatarIcon.setMaximumSize(new Dimension(36, 36));
        avatarIcon.setOpaque(false);

        JPanel infoText = new JPanel();
        infoText.setOpaque(false);
        infoText.setLayout(new BoxLayout(infoText, BoxLayout.Y_AXIS));

        lblReaderName = new JLabel("Chưa kiểm tra thẻ bạn đọc");
        lblReaderName.setFont(UITheme.FONT_BOLD);
        lblReaderName.setForeground(UITheme.TEXT_MAIN);

        lblReaderSub = new JLabel("Nhập mã thẻ rồi bấm Kiểm tra hoặc nhấn Enter");
        lblReaderSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblReaderSub.setForeground(UITheme.TEXT_MUTED);

        infoText.add(lblReaderName);
        infoText.add(Box.createVerticalStrut(2));
        infoText.add(lblReaderSub);

        lblReaderBadge = new JLabel(" CHỜ ");
        lblReaderBadge.setFont(UITheme.FONT_BADGE);
        lblReaderBadge.setOpaque(true);
        lblReaderBadge.setBackground(UITheme.TINT);
        lblReaderBadge.setForeground(UITheme.ON_TINT);
        lblReaderBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        card.add(avatarIcon, BorderLayout.WEST);
        card.add(infoText, BorderLayout.CENTER);
        card.add(lblReaderBadge, BorderLayout.EAST);

        return card;
    }

    private void updateLoanTypeToggleUI() {
        if (btnMangVe != null && btnTaiCho != null) {
            if (btnMangVe.isSelected()) {
                btnMangVe.setBackground(UITheme.PRIMARY);
                btnMangVe.setForeground(Color.WHITE);
                btnTaiCho.setBackground(UITheme.CARD_BG);
                btnTaiCho.setForeground(UITheme.TEXT_MAIN);
            } else {
                btnTaiCho.setBackground(UITheme.PRIMARY);
                btnTaiCho.setForeground(Color.WHITE);
                btnMangVe.setBackground(UITheme.CARD_BG);
                btnMangVe.setForeground(UITheme.TEXT_MAIN);
            }
        }
        updateLoanHint();
    }

    private void updateLoanHint() {
        boolean isMangVe = (btnMangVe == null || btnMangVe.isSelected());
        if (isMangVe) {
            String due = LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            lblLoanHint.setIcon(createCalendarIcon(16, UITheme.PRIMARY));
            lblLoanHint.setText(UiText.HINT_LOAN_HOME_PREFIX + due + UiText.HINT_LOAN_HOME_SUFFIX);
            if (lblDueDateSummary != null) lblDueDateSummary.setText(UiText.SUMMARY_DUE_PREFIX + due);
        } else {
            lblLoanHint.setIcon(createClockIcon(16, UITheme.PRIMARY));
            lblLoanHint.setText(UiText.HINT_LOAN_ONSITE);
            if (lblDueDateSummary != null) lblDueDateSummary.setText(UiText.SUMMARY_DUE_ONSITE);
        }
    }

    private void triggerReaderSuggest(JPopupMenu popup) {
        if (readerSuggestTimer != null && readerSuggestTimer.isRunning()) {
            readerSuggestTimer.stop();
        }
        readerSuggestTimer = new Timer(200, ev -> executeReaderSuggest(popup));
        readerSuggestTimer.setRepeats(false);
        readerSuggestTimer.start();
    }

    private void executeReaderSuggest(JPopupMenu popup) {
        String query = txtReaderCard.getText().trim();
        if (query.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        SwingWorker<List<PatronDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PatronDto> doInBackground() {
                return AppContainer.getInstance().getAuthenticationService().searchPatrons(query);
            }
            @Override
            protected void done() {
                try {
                    List<PatronDto> patrons = get();
                    popup.removeAll();
                    if (patrons != null && !patrons.isEmpty() && txtReaderCard.isFocusOwner()) {
                        int limit = Math.min(patrons.size(), 10);
                        for (int i = 0; i < limit; i++) {
                            PatronDto p = patrons.get(i);
                            JMenuItem item = new JMenuItem(p.getFullName() + " (" + p.getCardNumber() + ")");
                            item.setFont(UITheme.FONT_REGULAR);
                            item.addActionListener(e -> {
                                txtReaderCard.setText(p.getCardNumber());
                                checkReaderCard();
                                popup.setVisible(false);
                            });
                            popup.add(item);
                        }
                        popup.show(txtReaderCard, 0, txtReaderCard.getHeight());
                    } else {
                        popup.setVisible(false);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private JPanel createRightTableCard() {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel topHeader = new JPanel(new BorderLayout(8, 0));
        topHeader.setOpaque(false);

        JPanel titleWithBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleWithBadge.setOpaque(false);

        JLabel lblCartTitle = new JLabel("Sách Chuẩn Bị Mượn");
        lblCartTitle.setFont(UITheme.FONT_TITLE);
        lblCartTitle.setForeground(UITheme.TEXT_MAIN);

        lblCartCountBadge = new JLabel(" 0 cuốn ");
        lblCartCountBadge.setFont(UITheme.FONT_BADGE);
        lblCartCountBadge.setOpaque(true);
        lblCartCountBadge.setBackground(UITheme.TINT);
        lblCartCountBadge.setForeground(UITheme.ON_TINT);
        lblCartCountBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titleWithBadge.add(lblCartTitle);
        titleWithBadge.add(lblCartCountBadge);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        btnRemoveSelected = UITheme.createPillButton("Xóa cuốn đã chọn", UITheme.DANGER_BG, UITheme.DANGER_TEXT);
        btnRemoveSelected.setIcon(createTrashIcon(14, UITheme.DANGER_TEXT));
        btnRemoveSelected.setFont(UITheme.FONT_SMALL);
        btnRemoveSelected.setVisible(false);
        btnRemoveSelected.setEnabled(false);
        btnRemoveSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemoveSelected.addActionListener(e -> removeSelectedBook());

        btnClearAll = UITheme.createPillButton("Xóa tất cả", UITheme.TINT_LIGHT, UITheme.TEXT_MAIN);
        btnClearAll.setIcon(createRefreshIcon(14, UITheme.TEXT_MAIN));
        btnClearAll.setFont(UITheme.FONT_SMALL);
        btnClearAll.setVisible(false);
        btnClearAll.setEnabled(false);
        btnClearAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearAll.addActionListener(e -> clearCart());

        actions.add(btnRemoveSelected);
        actions.add(btnClearAll);

        topHeader.add(titleWithBadge, BorderLayout.WEST);
        topHeader.add(actions, BorderLayout.EAST);
        card.add(topHeader, BorderLayout.NORTH);

        rightCardLayout = new CardLayout();
        rightCenterPanel = new JPanel(rightCardLayout);
        rightCenterPanel.setOpaque(false);

        JPanel emptyPanel = createEmptyStatePanel();

        String[] cols = {"STT", "Mã vạch cuốn", "Tên đầu sách", "Vị trí kệ sách", "Tình trạng"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(40);
        cartTable.setShowHorizontalLines(true);
        cartTable.setShowVerticalLines(false);
        cartTable.setGridColor(UITheme.BORDER);
        cartTable.setFont(UITheme.FONT_REGULAR);
        cartTable.setSelectionBackground(UITheme.TINT);
        cartTable.setSelectionForeground(UITheme.TEXT_MAIN);
        cartTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        cartTable.getTableHeader().setBackground(UITheme.PRIMARY_TINT);
        cartTable.getTableHeader().setForeground(UITheme.ON_TINT);
        cartTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(60);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(150);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        cartTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(UITheme.FONT_SMALL);
                lbl.setForeground(UITheme.SUCCESS_TEXT);
                lbl.setText("● Khả dụng");
                return lbl;
            }
        });

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = cartTable.getSelectedRow() >= 0;
            btnRemoveSelected.setEnabled(hasSelection);
        });

        cartTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedBook();
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(cartTable);
        scrollTable.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UITheme.BORDER));
        scrollTable.getViewport().setBackground(Color.WHITE);

        rightCenterPanel.add(emptyPanel, "EMPTY");
        rightCenterPanel.add(scrollTable, "TABLE");
        rightCardLayout.show(rightCenterPanel, "EMPTY");

        card.add(rightCenterPanel, BorderLayout.CENTER);

        JPanel bottomFooter = new JPanel(new BorderLayout());
        bottomFooter.setOpaque(false);
        bottomFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(8, 2, 0, 2)
        ));

        JLabel lblRule = new JLabel("Lưu ý: Độc giả mượn về nhà tối đa 3 cuốn sách và không có khoản phạt chưa hoàn tất.");
        lblRule.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRule.setForeground(UITheme.TEXT_MUTED);
        bottomFooter.add(lblRule, BorderLayout.WEST);

        card.add(bottomFooter, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JPanel iconVector = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TINT);
                g2.fillOval(getWidth() / 2 - 28, getHeight() / 2 - 28, 56, 56);

                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.drawLine(cx, cy - 8, cx, cy + 10);

                java.awt.geom.Path2D leftPage = new java.awt.geom.Path2D.Double();
                leftPage.moveTo(cx, cy - 8);
                leftPage.quadTo(cx - 8, cy - 12, cx - 18, cy - 9);
                leftPage.lineTo(cx - 18, cy + 9);
                leftPage.quadTo(cx - 8, cy + 6, cx, cy + 10);
                g2.draw(leftPage);

                java.awt.geom.Path2D rightPage = new java.awt.geom.Path2D.Double();
                rightPage.moveTo(cx, cy - 8);
                rightPage.quadTo(cx + 8, cy - 12, cx + 18, cy - 9);
                rightPage.lineTo(cx + 18, cy + 9);
                rightPage.quadTo(cx + 8, cy + 6, cx, cy + 10);
                g2.draw(rightPage);

                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx + 4, cy - 3, cx + 14, cy - 4);
                g2.drawLine(cx + 4, cy + 1, cx + 14, cy);
                g2.drawLine(cx + 4, cy + 5, cx + 12, cy + 4);

                g2.drawLine(cx - 14, cy - 4, cx - 4, cy - 3);
                g2.drawLine(cx - 14, cy, cx - 4, cy + 1);
                g2.drawLine(cx - 12, cy + 4, cx - 4, cy + 5);

                g2.dispose();
            }
        };
        iconVector.setPreferredSize(new Dimension(64, 64));
        iconVector.setMaximumSize(new Dimension(64, 64));
        iconVector.setOpaque(false);
        iconVector.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("Chưa có cuốn sách nào trong danh sách mượn");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptySub = new JLabel("Quét mã vạch hoặc nhập mã ở Bước 3 bên trái rồi nhấn Enter để thêm sách vào phiếu");
        lblEmptySub.setFont(UITheme.FONT_SMALL);
        lblEmptySub.setForeground(UITheme.TEXT_MUTED);
        lblEmptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(iconVector);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lblEmptyTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblEmptySub);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createBaseCard() {
        JPanel card = new JPanel() {
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
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        return card;
    }

    private JPanel createStepHeader(int step, String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel badge = new JLabel(String.valueOf(step), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setPreferredSize(new Dimension(18, 18));
        badge.setMinimumSize(new Dimension(18, 18));
        badge.setMaximumSize(new Dimension(18, 18));

        JLabel text = new JLabel(title);
        text.setFont(new Font("Segoe UI", Font.BOLD, 11));
        text.setForeground(UITheme.PRIMARY);

        p.add(badge);
        p.add(text);
        return p;
    }

    private Icon createCalendarIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, y + 2, size - 3, size - 4, 3, 3);
                g2.drawLine(x + 1, y + 6, x + size - 2, y + 6);
                g2.drawLine(x + 4, y, x + 4, y + 3);
                g2.drawLine(x + size - 5, y, x + size - 5, y + 3);
                g2.fillRect(x + 4, y + 8, 2, 2);
                g2.fillRect(x + 8, y + 8, 2, 2);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createClockIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                int cx = x + size / 2;
                int cy = y + size / 2;
                g2.drawLine(cx, cy, cx, cy - 4);
                g2.drawLine(cx, cy, cx + 4, cy);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createInfoIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                g2.fillOval(x + size / 2 - 1, y + 3, 2, 2);
                g2.drawLine(x + size / 2, y + 6, x + size / 2, y + size - 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createSearchIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 1, y + 1, size - 6, size - 6);
                g2.drawLine(x + size - 5, y + size - 5, x + size - 1, y + size - 1);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createPlusIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int midX = x + size / 2;
                int midY = y + size / 2;
                g2.drawLine(midX, y + 3, midX, y + size - 3);
                g2.drawLine(x + 3, midY, x + size - 3, midY);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createCheckIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 2, y + size / 2, x + size / 2 - 1, y + size - 4);
                g2.drawLine(x + size / 2 - 1, y + size - 4, x + size - 2, y + 3);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createRefreshIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                g2.drawArc(x + 3, y + 3, 10, 10, 40, 290);

                java.awt.geom.Path2D arrow = new java.awt.geom.Path2D.Double();
                arrow.moveTo(x + 13, y + 2);
                arrow.lineTo(x + 13, y + 5);
                arrow.lineTo(x + 10, y + 5);
                g2.draw(arrow);

                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private Icon createTrashIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 2, y + 4, x + size - 2, y + 4);
                g2.drawLine(x + size / 2 - 2, y + 2, x + size / 2 + 2, y + 2);
                g2.drawRoundRect(x + 3, y + 4, size - 6, size - 6, 2, 2);
                g2.drawLine(x + size / 2 - 2, y + 6, x + size / 2 - 2, y + size - 4);
                g2.drawLine(x + size / 2 + 2, y + 6, x + size / 2 + 2, y + size - 4);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private void checkReaderCard() {
        String idOrCard = txtReaderCard.getText().trim();
        if (idOrCard.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng nhập mã thẻ hoặc số CCCD của bạn đọc!");
            return;
        }

        UserCardInfo info = circulationService.getCardInfo(idOrCard);
        if (info == null) {
            currentCard = null;
            lblReaderName.setText("Không tìm thấy bạn đọc này!");
            lblReaderName.setForeground(UITheme.DANGER_TEXT);
            lblReaderSub.setText("Kiểm tra lại mã thẻ '" + idOrCard + "' trong hệ thống.");
            lblReaderBadge.setIcon(null);
            lblReaderBadge.setText(" KHÔNG TÌM THẤY ");
            lblReaderBadge.setBackground(UITheme.DANGER_BG);
            lblReaderBadge.setForeground(UITheme.DANGER_TEXT);
            lblReaderSummary.setText("• Bạn đọc: Không tìm thấy");
            pnlReaderCard.repaint();
            UiModalHelper.showToastError(this, "Không tìm thấy bạn đọc với mã: " + idOrCard);
            return;
        }

        currentCard = info;
        lblReaderName.setText(info.fullName());
        lblReaderName.setForeground(UITheme.TEXT_MAIN);
        lblReaderSub.setText("Số thẻ: " + info.cardNumber());
        lblReaderSummary.setText("• Bạn đọc: " + info.fullName() + " (" + info.cardNumber() + ")");

        if (!info.cardActive()) {
            lblReaderBadge.setIcon(null);
            lblReaderBadge.setText(" ĐANG KHÓA ");
            lblReaderBadge.setBackground(UITheme.DANGER_BG);
            lblReaderBadge.setForeground(UITheme.DANGER_TEXT);
            UiModalHelper.showToastWarning(this, "Thẻ của bạn đọc " + info.fullName() + " đang bị khóa!");
        } else if (info.cardExpired()) {
            lblReaderBadge.setIcon(null);
            lblReaderBadge.setText(" HẾT HẠN ");
            lblReaderBadge.setBackground(UITheme.WARNING_BG);
            lblReaderBadge.setForeground(UITheme.WARNING_TEXT);
            UiModalHelper.showToastWarning(this, "Thẻ thư viện của bạn đọc đã hết hạn sử dụng!");
        } else {
            lblReaderBadge.setIcon(createCheckIcon(12, UITheme.SUCCESS_TEXT));
            lblReaderBadge.setIconTextGap(4);
            lblReaderBadge.setText("HỢP LỆ ");
            lblReaderBadge.setBackground(UITheme.SUCCESS_BG);
            lblReaderBadge.setForeground(UITheme.SUCCESS_TEXT);
            UiModalHelper.showToastSuccess(this, "Xác nhận thẻ bạn đọc: " + info.fullName());
            txtBarcode.requestFocusInWindow();
        }
        pnlReaderCard.repaint();
    }

    private void addBarcodeToCart() {
        String input = txtBarcode.getText().trim();
        if (input.isEmpty()) return;

        boolean isMangVe = (btnMangVe == null || btnMangVe.isSelected());
        if (isMangVe && cartBarcodes.size() >= 3) {
            UiModalHelper.showToastWarning(this, UiText.MSG_MAX_3_HOME_BOOKS);
            txtBarcode.setText("");
            return;
        }

        BookCopyInfo copy = circulationService.getCopyInfo(input);
        if (copy == null && bookCatalogService != null) {
            PagedResult<BookSummaryDto> result = bookCatalogService.searchBooks(
                new BookSearchCriteria(input, null, BookSearchCriteria.AvailabilityFilter.AVAILABLE_ONLY),
                1,
                5
            );
            if (result != null && result.getItems() != null) {
                for (BookSummaryDto b : result.getItems()) {
                    if (b.getAvailableCopies() > 0) {
                        List<BookCopyInfo> copies = circulationService.getAvailableCopies(b.getBookId());
                        if (copies != null) {
                            for (BookCopyInfo cp : copies) {
                                if (!cartBarcodes.contains(cp.copyId()) && "KHA_DUNG".equalsIgnoreCase(cp.status())) {
                                    copy = cp;
                                    break;
                                }
                            }
                        }
                    }
                    if (copy != null) break;
                }
            }
        }

        if (copy == null) {
            UiModalHelper.showToastError(this, UiText.ERR_COPY_OR_TITLE_NOT_FOUND + input);
            return;
        }

        if (cartBarcodes.contains(copy.copyId())) {
            UiModalHelper.showToastWarning(this, "Cuốn sách '" + copy.copyId() + "' (" + copy.bookTitle() + ") đã có trong danh sách mượn!");
            txtBarcode.setText("");
            return;
        }

        if (!"KHA_DUNG".equalsIgnoreCase(copy.status())) {
            UiModalHelper.showToastWarning(this, "Cuốn sách đang ở trạng thái: " + copy.status() + ", không thể cho mượn!");
            return;
        }

        cartBarcodes.add(copy.copyId());
        int stt = cartBarcodes.size();
        cartTableModel.addRow(new Object[]{
            stt,
            copy.copyId(),
            copy.bookTitle(),
            (copy.shelfLocation() != null ? copy.shelfLocation() : "Chưa xếp kệ"),
            copy.status()
        });
        txtBarcode.setText("");

        updateCartViewState();
        UiModalHelper.showToastSuccess(this, "Đã thêm: " + copy.bookTitle());
        txtBarcode.requestFocusInWindow();
    }

    private void removeSelectedBook() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartBarcodes.size()) return;

        cartBarcodes.remove(row);
        cartTableModel.removeRow(row);

        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            cartTableModel.setValueAt(i + 1, i, 0);
        }

        updateCartViewState();
    }

    private void clearCart() {
        if (cartBarcodes.isEmpty()) return;
        cartBarcodes.clear();
        cartTableModel.setRowCount(0);
        updateCartViewState();
    }

    private void updateCartViewState() {
        int count = cartBarcodes.size();
        lblCartCountBadge.setText(" " + count + " cuốn ");
        lblCartCounter.setText(count + " / 3 cuốn");

        boolean hasItems = count > 0;
        btnRemoveSelected.setVisible(hasItems);
        btnClearAll.setVisible(hasItems);
        btnRemoveSelected.setEnabled(hasItems && cartTable.getSelectedRow() >= 0);
        btnClearAll.setEnabled(hasItems);

        if (hasItems) {
            rightCardLayout.show(rightCenterPanel, "TABLE");
        } else {
            rightCardLayout.show(rightCenterPanel, "EMPTY");
        }
    }

    private void resetForm() {
        currentCard = null;
        txtReaderCard.setText("");
        txtBarcode.setText("");
        lblReaderName.setText("Chưa kiểm tra thẻ bạn đọc");
        lblReaderName.setForeground(UITheme.TEXT_MAIN);
        lblReaderSub.setText("Nhập mã thẻ rồi bấm Kiểm tra hoặc nhấn Enter");
        lblReaderBadge.setIcon(null);
        lblReaderBadge.setText(" CHỜ ");
        lblReaderBadge.setBackground(UITheme.TINT);
        lblReaderBadge.setForeground(UITheme.ON_TINT);
        lblReaderSummary.setText("• Bạn đọc: Chưa xác thực thẻ");
        pnlReaderCard.repaint();

        clearCart();
    }

    private void handleConfirmLoan() {
        if (currentCard == null) {
            UiModalHelper.showToastWarning(this, "Vui lòng kiểm tra thẻ bạn đọc trước khi cho mượn!");
            txtReaderCard.requestFocusInWindow();
            return;
        }

        if (!currentCard.cardActive()) {
            UiModalHelper.showToastError(this, "Thẻ bạn đọc đang bị khóa! Không thể lập phiếu mượn.");
            return;
        }

        if (currentCard.cardExpired()) {
            UiModalHelper.showToastError(this, "Thẻ bạn đọc đã hết hạn! Vui lòng gia hạn thẻ trước khi mượn sách.");
            return;
        }

        if (cartBarcodes.isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng quét ít nhất 1 cuốn sách vào danh sách mượn!");
            txtBarcode.requestFocusInWindow();
            return;
        }

        LoanType lt = (btnMangVe != null && btnMangVe.isSelected()) ? LoanType.VE_NHA : LoanType.TAI_CHO;

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
                        UiModalHelper.showToastSuccess(CirculationDeskView.this, "Lập phiếu mượn thành công! Mã phiếu: " + ticket.getTicketId());
                        resetForm();
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
