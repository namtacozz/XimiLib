package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.gui.util.UITheme;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer cột "Tên sách": Ảnh bìa nhỏ bo góc (34x40) + Tên sách đậm + Phụ chú.
 */
public class BookTitleCellRenderer extends JPanel implements TableCellRenderer {

    private static final Map<String, Image> COVER_CACHE = new ConcurrentHashMap<>();
    private final JLabel lblTitle = new JLabel();
    private final JLabel lblSub = new JLabel();
    private final BookTableModel tableModel;
    private final Set<String> selectedBookIds;

    private Color currentCoverColor = UITheme.PRIMARY;
    private String bookInitials = "BK";
    private Image currentCoverImg = null;

    public BookTitleCellRenderer(BookTableModel tableModel, Set<String> selectedBookIds) {
        this.tableModel = tableModel;
        this.selectedBookIds = selectedBookIds;

        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setOpaque(true);

        JPanel coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (currentCoverImg != null) {
                    Shape oldClip = g2.getClip();
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                    g2.drawImage(currentCoverImg, 0, 0, getWidth(), getHeight(), null);
                    g2.setClip(oldClip);
                    g2.setColor(new Color(0, 0, 0, 25));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                } else {
                    g2.setColor(currentCoverColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRect(0, 0, 4, getHeight());

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(bookInitials)) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(bookInitials, tx, ty);
                }
                g2.dispose();
            }
        };
        coverPanel.setPreferredSize(new Dimension(34, 40));
        coverPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.TEXT_MAIN);

        lblSub.setFont(UITheme.FONT_BADGE);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblSub);

        add(coverPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof BookSummaryDto b) {
            lblTitle.setText(b.getTitle());
            lblSub.setText("Mã: " + b.getBookId() + " • " + b.getPublisherName());
            this.currentCoverColor = b.getCoverColor() != null ? b.getCoverColor() : UITheme.PRIMARY;
            this.bookInitials = UITheme.getAvatarInitial(b.getTitle());
            String imgPath = b.getCoverImage();
            if (imgPath != null && !imgPath.trim().isEmpty()) {
                this.currentCoverImg = COVER_CACHE.computeIfAbsent(imgPath, p -> {
                    try {
                        java.net.URL u = getClass().getResource(p);
                        if (u != null) {
                            ImageIcon ico = new ImageIcon(u);
                            return ico.getImage().getScaledInstance(34, 40, Image.SCALE_SMOOTH);
                        }
                    } catch (Exception ignored) {}
                    return null;
                });
            } else {
                this.currentCoverImg = null;
            }
        }

        BookSummaryDto b = tableModel.getBookAt(row);
        boolean isChecked = (b != null && selectedBookIds.contains(b.getBookId()));
        if (isChecked || isSelected) {
            setBackground(UITheme.ROW_SELECTED);
        } else {
            setBackground(UITheme.CARD_BG);
        }
        return this;
    }
}
