package com.ximilib.presentation.view;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.dto.CompensationResultDto;
import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.dto.ReturnResultDto;
import com.ximilib.application.service.AuthenticationService;
import com.ximilib.application.service.CirculationService;
import com.ximilib.domain.repository.CirculationRepository.BorrowDetailInfo;
import com.ximilib.presentation.component.VietQrModalPanel;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiText;
import com.ximilib.presentation.util.IconFactory;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UiModalHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReturnAndCompensationView extends JPanel {

    private final CirculationService circulationService;
    private final AuthenticationService authService;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField txtBorrower;
    private JLabel lblSelectedBorrower;
    private PatronDto selectedPatron;

    private JComboBox<String> cmbBorrowedBooks;
    private final List<BorrowTicketDto> currentActiveLoans = new ArrayList<>();
    private JTextField txtBarcode;

    private JRadioButton radNormal;
    private JRadioButton radDamaged;
    private JRadioButton radLost;

    private JLabel lblBookTitle;
    private JLabel lblCopyBarcode;
    private JLabel lblTicketId;
    private JLabel lblDueDate;
    private JLabel lblOverdueStatus;

    private JPanel financeCard;
    private JLabel lblFineDetail;
    private JLabel lblTotalAmount;
    private JLabel lblCleanReturnNotice;
    private JButton btnExecute;

    private BorrowDetailInfo currentDetail;
    private BigDecimal currentTotalFee = BigDecimal.ZERO;
    private Timer borrowerSuggestTimer;

    public ReturnAndCompensationView() {
        this(AppContainer.getInstance().getCirculationService(), AppContainer.getInstance().getAuthenticationService());
    }

    public ReturnAndCompensationView(CirculationService service) {
        this(service, AppContainer.getInstance().getAuthenticationService());
    }

    public ReturnAndCompensationView(CirculationService service, AuthenticationService authService) {
        this.circulationService = service != null ? service : AppContainer.getInstance().getCirculationService();
        this.authService = authService != null ? authService : AppContainer.getInstance().getAuthenticationService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));

        initUI();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(UiText.TITLE_RETURN_VIEW);
        lblTitle.setFont(UITheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        JLabel lblSub = new JLabel(UiText.SUB_RETURN_VIEW);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(lblSub);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new GridLayout(1, 2, 18, 0));
        mainSplit.setOpaque(false);

        mainSplit.add(createLeftWorkflowCard());
        mainSplit.add(createRightDetailsCard());

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createLeftWorkflowCard() {
        JPanel card = createBaseCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblCardTitle = new JLabel(UiText.TITLE_RECEIVE_RETURN);
        lblCardTitle.setFont(UITheme.FONT_TITLE);
        lblCardTitle.setForeground(UITheme.TEXT_MAIN);
        lblCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCardTitle);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(1, UiText.STEP_SELECT_BORROWER));
        card.add(Box.createVerticalStrut(6));

        JPanel borrowerInputBox = new JPanel(new BorderLayout(8, 0));
        borrowerInputBox.setOpaque(false);
        borrowerInputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        borrowerInputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtBorrower = new JTextField();
        txtBorrower.setFont(UITheme.FONT_REGULAR);
        txtBorrower.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtBorrower.putClientProperty("JTextField.placeholderText", UiText.PH_SEARCH_BORROWER);

        JPopupMenu suggestPopup = new JPopupMenu();
        suggestPopup.setFocusable(false);

        txtBorrower.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { triggerBorrowerSuggest(suggestPopup); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { triggerBorrowerSuggest(suggestPopup); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { triggerBorrowerSuggest(suggestPopup); }
        });
        txtBorrower.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                Timer t = new Timer(200, ev -> suggestPopup.setVisible(false));
                t.setRepeats(false);
                t.start();
            }
        });

        JButton btnSearchBorrower = UITheme.createPillButton(UiText.BTN_SEARCH_SHORT, UITheme.PRIMARY, Color.WHITE);
        btnSearchBorrower.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearchBorrower.setPreferredSize(new Dimension(80, 36));
        btnSearchBorrower.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearchBorrower.addActionListener(e -> searchBorrowerDirect());

        borrowerInputBox.add(txtBorrower, BorderLayout.CENTER);
        borrowerInputBox.add(btnSearchBorrower, BorderLayout.EAST);
        card.add(borrowerInputBox);
        card.add(Box.createVerticalStrut(6));

        lblSelectedBorrower = new JLabel(UiText.LBL_PATRON_UNSELECTED);
        lblSelectedBorrower.setFont(UITheme.FONT_SMALL);
        lblSelectedBorrower.setForeground(UITheme.TEXT_MUTED);
        lblSelectedBorrower.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSelectedBorrower);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(2, UiText.STEP_SELECT_RETURN_BOOK));
        card.add(Box.createVerticalStrut(6));

        cmbBorrowedBooks = new JComboBox<>(new String[]{UiText.PH_SELECT_BOOK_COMBO});
        cmbBorrowedBooks.setFont(UITheme.FONT_REGULAR);
        cmbBorrowedBooks.setBackground(Color.WHITE);
        cmbBorrowedBooks.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cmbBorrowedBooks.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbBorrowedBooks.addActionListener(e -> onSelectBookFromCombo());
        card.add(cmbBorrowedBooks);
        card.add(Box.createVerticalStrut(8));

        JPanel scanRow = new JPanel(new BorderLayout(8, 0));
        scanRow.setOpaque(false);
        scanRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        scanRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtBarcode = new JTextField();
        txtBarcode.setFont(UITheme.FONT_REGULAR);
        txtBarcode.setBorder(new UITheme.RoundedBorder(UITheme.BORDER, 8));
        txtBarcode.putClientProperty("JTextField.placeholderText", UiText.PH_SCAN_BARCODE_DIRECT);
        txtBarcode.addActionListener(e -> checkDirectBarcode());

        JButton btnCheckBarcode = UITheme.createPillButton(UiText.BTN_CHECK, UITheme.TINT, UITheme.ON_TINT);
        btnCheckBarcode.setFont(UITheme.FONT_SMALL);
        btnCheckBarcode.setPreferredSize(new Dimension(86, 36));
        btnCheckBarcode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckBarcode.addActionListener(e -> checkDirectBarcode());

        scanRow.add(txtBarcode, BorderLayout.CENTER);
        scanRow.add(btnCheckBarcode, BorderLayout.EAST);
        card.add(scanRow);
        card.add(Box.createVerticalStrut(14));

        card.add(createStepHeader(3, UiText.STEP_BOOK_CONDITION));
        card.add(Box.createVerticalStrut(6));

        radNormal = new JRadioButton(UiText.COND_NORMAL, true);
        radDamaged = new JRadioButton(UiText.COND_DAMAGED);
        radLost = new JRadioButton(UiText.COND_LOST);

        radNormal.setFont(UITheme.FONT_REGULAR);
        radDamaged.setFont(UITheme.FONT_REGULAR);
        radLost.setFont(UITheme.FONT_REGULAR);
        radNormal.setOpaque(false);
        radDamaged.setOpaque(false);
        radLost.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radNormal);
        bg.add(radDamaged);
        bg.add(radLost);

        radNormal.addActionListener(e -> recalculateFine());
        radDamaged.addActionListener(e -> recalculateFine());
        radLost.addActionListener(e -> recalculateFine());

        card.add(radNormal);
        card.add(Box.createVerticalStrut(4));
        card.add(radDamaged);
        card.add(Box.createVerticalStrut(4));
        card.add(radLost);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createRightDetailsCard() {
        JPanel card = createBaseCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));

        JLabel lblCardTitle = new JLabel(UiText.TITLE_LOAN_INFO);
        lblCardTitle.setFont(UITheme.FONT_TITLE);
        lblCardTitle.setForeground(UITheme.TEXT_MAIN);
        lblCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBox.add(lblCardTitle);
        centerBox.add(Box.createVerticalStrut(12));

        JPanel infoBox = new JPanel(new GridLayout(5, 1, 0, 4));
        infoBox.setBackground(UITheme.BG_LIGHT);
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        lblBookTitle = new JLabel(UiText.LBL_BOOK_UNSELECTED);
        lblCopyBarcode = new JLabel(UiText.LBL_COPY_EMPTY);
        lblTicketId = new JLabel(UiText.LBL_TICKET_EMPTY);
        lblDueDate = new JLabel(UiText.LBL_DUE_EMPTY);
        lblOverdueStatus = new JLabel(UiText.LBL_STATUS_EMPTY);

        infoBox.add(lblBookTitle);
        infoBox.add(lblCopyBarcode);
        infoBox.add(lblTicketId);
        infoBox.add(lblDueDate);
        infoBox.add(lblOverdueStatus);
        centerBox.add(infoBox);
        centerBox.add(Box.createVerticalStrut(16));

        JLabel lblFinanceTitle = new JLabel(UiText.TITLE_FINANCE_DUTY);
        lblFinanceTitle.setFont(UITheme.FONT_TITLE);
        lblFinanceTitle.setForeground(UITheme.TEXT_MAIN);
        lblFinanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBox.add(lblFinanceTitle);
        centerBox.add(Box.createVerticalStrut(10));

        lblCleanReturnNotice = new JLabel(UiText.NOTICE_CLEAN_RETURN);
        lblCleanReturnNotice.setIcon(IconFactory.createCheckIcon(UITheme.SUCCESS_GREEN, 16));
        lblCleanReturnNotice.setIconTextGap(8);
        lblCleanReturnNotice.setFont(UITheme.FONT_REGULAR);
        lblCleanReturnNotice.setForeground(UITheme.SUCCESS_TEXT);
        lblCleanReturnNotice.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCleanReturnNotice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.SUCCESS_GREEN, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        lblCleanReturnNotice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        centerBox.add(lblCleanReturnNotice);

        financeCard = new JPanel(new GridLayout(2, 1, 0, 4));
        financeCard.setBackground(UITheme.BG_LIGHT);
        financeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        financeCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        financeCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.DANGER_TEXT, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        lblFineDetail = new JLabel(UiText.LBL_FINE_DETAIL_INIT);
        lblFineDetail.setFont(UITheme.FONT_SMALL);
        lblFineDetail.setForeground(UITheme.TEXT_MUTED);

        lblTotalAmount = new JLabel(UiText.LBL_TOTAL_AMOUNT_INIT);
        lblTotalAmount.setFont(UITheme.FONT_TITLE);
        lblTotalAmount.setForeground(UITheme.DANGER_TEXT);

        financeCard.add(lblFineDetail);
        financeCard.add(lblTotalAmount);
        financeCard.setVisible(false);
        centerBox.add(financeCard);
        centerBox.add(Box.createVerticalGlue());

        card.add(centerBox, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        btnExecute = UITheme.createPillButton(UiText.BTN_CONFIRM_UPPER, UITheme.PRIMARY, Color.WHITE);
        btnExecute.setFont(UITheme.FONT_BOLD);
        btnExecute.setPreferredSize(new Dimension(180, 42));
        btnExecute.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExecute.addActionListener(e -> executeReturnOrLoss());

        bottomBar.add(btnExecute);
        card.add(bottomBar, BorderLayout.SOUTH);

        return card;
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

    private void triggerBorrowerSuggest(JPopupMenu popup) {
        if (borrowerSuggestTimer != null && borrowerSuggestTimer.isRunning()) {
            borrowerSuggestTimer.stop();
        }
        borrowerSuggestTimer = new Timer(200, ev -> executeBorrowerSuggest(popup));
        borrowerSuggestTimer.setRepeats(false);
        borrowerSuggestTimer.start();
    }

    private void executeBorrowerSuggest(JPopupMenu popup) {
        String query = txtBorrower.getText().trim();
        if (query.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        SwingWorker<List<PatronDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PatronDto> doInBackground() {
                return authService.searchPatrons(query);
            }
            @Override
            protected void done() {
                try {
                    List<PatronDto> patrons = get();
                    popup.removeAll();
                    if (patrons != null && !patrons.isEmpty() && txtBorrower.isFocusOwner()) {
                        int limit = Math.min(patrons.size(), 10);
                        for (int i = 0; i < limit; i++) {
                            PatronDto p = patrons.get(i);
                            JMenuItem item = new JMenuItem(p.getFullName() + " (" + p.getCardNumber() + ")");
                            item.setFont(UITheme.FONT_REGULAR);
                            item.addActionListener(e -> {
                                txtBorrower.setText(p.getFullName());
                                popup.setVisible(false);
                                loadPatronLoans(p);
                            });
                            popup.add(item);
                        }
                        popup.show(txtBorrower, 0, txtBorrower.getHeight());
                    } else {
                        popup.setVisible(false);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void searchBorrowerDirect() {
        String query = txtBorrower.getText().trim();
        if (query.isEmpty()) return;
        List<PatronDto> patrons = authService.searchPatrons(query);
        if (patrons != null && !patrons.isEmpty()) {
            loadPatronLoans(patrons.get(0));
        } else {
            UiModalHelper.showToastWarning(this, UiText.ERR_BORROWER_NOT_FOUND + query);
        }
    }

    private void loadPatronLoans(PatronDto patron) {
        selectedPatron = patron;
        lblSelectedBorrower.setText("Bạn đọc: " + patron.getFullName() + " (" + patron.getCardNumber() + ")");
        lblSelectedBorrower.setForeground(UITheme.PRIMARY);

        SwingWorker<List<BorrowTicketDto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BorrowTicketDto> doInBackground() {
                return circulationService.getAllLoans(patron.getCardNumber(), 1);
            }
            @Override
            protected void done() {
                try {
                    List<BorrowTicketDto> loans = get();
                    currentActiveLoans.clear();
                    cmbBorrowedBooks.removeAllItems();
                    if (loans != null && !loans.isEmpty()) {
                        for (BorrowTicketDto ticket : loans) {
                            if (ticket.getStatus() == 1) {
                                currentActiveLoans.add(ticket);
                                String itemText = (ticket.getCopyBarcode() != null ? ticket.getCopyBarcode() : ticket.getTicketId())
                                    + " - " + ticket.getBookTitle();
                                cmbBorrowedBooks.addItem(itemText);
                            }
                        }
                    }
                    if (currentActiveLoans.isEmpty()) {
                        cmbBorrowedBooks.addItem(UiText.ERR_NO_ACTIVE_LOANS);
                        clearDetail();
                    } else {
                        onSelectBookFromCombo();
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void onSelectBookFromCombo() {
        int idx = cmbBorrowedBooks.getSelectedIndex();
        if (idx < 0 || idx >= currentActiveLoans.size()) return;
        BorrowTicketDto ticket = currentActiveLoans.get(idx);
        if (ticket.getCopyBarcode() != null) {
            loadCopyDetail(ticket.getCopyBarcode());
        }
    }

    private void checkDirectBarcode() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) return;
        loadCopyDetail(barcode);
    }

    private void loadCopyDetail(String barcode) {
        BorrowDetailInfo detail = circulationService.findActiveLoanByCopy(barcode);
        if (detail == null) {
            clearDetail();
            UiModalHelper.showToastWarning(this, UiText.ERR_LOAN_NOT_FOUND);
            return;
        }

        currentDetail = detail;
        lblBookTitle.setText("Đầu sách: " + detail.bookTitle());
        lblCopyBarcode.setText("Mã cuốn: " + detail.copyId());
        lblTicketId.setText("Mã phiếu: " + detail.ticketId() + " (Mã ĐG: " + detail.userId() + ")");
        lblDueDate.setText("Hạn hoàn trả: " + (detail.dueDate() != null ? detail.dueDate().format(dateFormatter) : "-"));

        recalculateFine();
    }

    private void recalculateFine() {
        if (currentDetail == null) {
            financeCard.setVisible(false);
            lblCleanReturnNotice.setVisible(true);
            lblCleanReturnNotice.setText(UiText.NOTICE_SCAN_PROMPT);
            currentTotalFee = BigDecimal.ZERO;
            return;
        }

        boolean isLost = radLost.isSelected();
        boolean isDamaged = radDamaged.isSelected();

        CompensationResultDto calc = circulationService.calculateCompensation(currentDetail.copyId(), isLost, isDamaged);
        currentTotalFee = calc.getTotalAmount() != null ? calc.getTotalAmount() : BigDecimal.ZERO;

        if (calc.getOverdueDays() > 0) {
            lblOverdueStatus.setText("<html><font color='#dc2626'><b>" + UiText.OVERDUE_PREFIX + calc.getOverdueDays() + UiText.OVERDUE_SUFFIX + "</b></font></html>");
        } else {
            lblOverdueStatus.setText("<html><font color='#16a34a'>" + UiText.STATUS_IN_LOAN_TERM + "</font></html>");
        }

        if (currentTotalFee.compareTo(BigDecimal.ZERO) > 0) {
            financeCard.setVisible(true);
            lblCleanReturnNotice.setVisible(false);
            lblFineDetail.setText(UiText.FINE_OVERDUE_PREFIX + currencyFormat.format(calc.getOverdueFine())
                + UiText.FINE_FEE_PREFIX + currencyFormat.format(calc.getTechFee())
                + (calc.getBookLossAmount().compareTo(BigDecimal.ZERO) > 0 ? UiText.FINE_LOSS_PREFIX + currencyFormat.format(calc.getBookLossAmount()) : ""));
            lblTotalAmount.setText(UiText.TOTAL_PAY_PREFIX + currencyFormat.format(currentTotalFee));
        } else {
            financeCard.setVisible(false);
            lblCleanReturnNotice.setVisible(true);
            lblCleanReturnNotice.setText(UiText.NOTICE_CLEAN_OK);
        }
        financeCard.revalidate();
        financeCard.repaint();
    }

    private void executeReturnOrLoss() {
        if (currentDetail == null) {
            UiModalHelper.showToastWarning(this, UiText.ERR_SELECT_BOOK_PROMPT);
            return;
        }

        String barcode = currentDetail.copyId();

        if (radLost.isSelected()) {
            CompensationResultDto loss = circulationService.reportLoss(barcode);
            if (loss.getTotalAmount() != null && loss.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                VietQrModalPanel.show(this, loss.getFineTicketId(), loss.getTotalAmount(), () -> {
                    circulationService.payFine(loss.getFineTicketId(), UiText.METHOD_VIETQR);
                    UiModalHelper.showToastSuccess(this, UiText.MSG_COMPENSATION_PAID + currencyFormat.format(loss.getTotalAmount()));
                    resetForm();
                });
            } else {
                UiModalHelper.showToastSuccess(this, UiText.MSG_LOSS_RECORDED);
                resetForm();
            }
        } else {
            String cond = radDamaged.isSelected() ? UiText.COND_LABEL_DAMAGED : UiText.COND_LABEL_NORMAL;
            ReturnResultDto ret = circulationService.returnBook(barcode, cond);
            if (ret.getTotalFine() != null && ret.getTotalFine().compareTo(BigDecimal.ZERO) > 0) {
                VietQrModalPanel.show(this, "P-" + barcode, ret.getTotalFine(), () -> {
                    UiModalHelper.showToastSuccess(this, UiText.MSG_RETURN_FINE_PAID + currencyFormat.format(ret.getTotalFine()));
                    resetForm();
                });
            } else {
                UiModalHelper.showToastSuccess(this, UiText.MSG_RETURN_NO_FINE);
                resetForm();
            }
        }
    }

    private void clearDetail() {
        currentDetail = null;
        lblBookTitle.setText(UiText.LBL_BOOK_UNSELECTED);
        lblCopyBarcode.setText(UiText.LBL_COPY_EMPTY);
        lblTicketId.setText(UiText.LBL_TICKET_EMPTY);
        lblDueDate.setText(UiText.LBL_DUE_EMPTY);
        lblOverdueStatus.setText(UiText.LBL_STATUS_EMPTY);
        financeCard.setVisible(false);
        lblCleanReturnNotice.setVisible(true);
        lblCleanReturnNotice.setText(UiText.NOTICE_SCAN_PROMPT);
        currentTotalFee = BigDecimal.ZERO;
    }

    private void resetForm() {
        clearDetail();
        txtBorrower.setText("");
        txtBarcode.setText("");
        lblSelectedBorrower.setText(UiText.LBL_PATRON_UNSELECTED);
        lblSelectedBorrower.setForeground(UITheme.TEXT_MUTED);
        selectedPatron = null;
        currentActiveLoans.clear();
        cmbBorrowedBooks.removeAllItems();
        cmbBorrowedBooks.addItem(UiText.PH_SELECT_BOOK_COMBO);
        radNormal.setSelected(true);
    }
}
