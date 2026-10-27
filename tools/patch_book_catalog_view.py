import os
import re

file_path = r'F:\AppMaking\XimiLib\sources\src\main\java\com\ximilib\gui\views\BookCatalogView.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update imports
content = content.replace(
    'import com.ximilib.application.service.impl.MockBookCatalogServiceImpl;',
    'import com.ximilib.infrastructure.config.AppContainer;\nimport java.util.concurrent.ConcurrentHashMap;'
)

# 2. Update default constructor
content = content.replace(
    'this(MockBookCatalogServiceImpl.getInstance(), UserSession.getCurrentUser());',
    'this(AppContainer.getInstance().getBookCatalogService(), UserSession.getCurrentUser());'
)

# 3. Update loadDataAsync exception handling
old_done = """            @Override
            protected void done() {
                try {
                    PagedResult<BookSummaryDto> result = get();
                    SwingUtilities.invokeLater(() -> updateUIWithResult(result));
                } catch (Exception e) {
                    showCardView("EMPTY");
                }
            }"""

new_done = """            @Override
            protected void done() {
                try {
                    PagedResult<BookSummaryDto> result = get();
                    SwingUtilities.invokeLater(() -> updateUIWithResult(result));
                } catch (Exception e) {
                    Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                    SwingUtilities.invokeLater(() -> {
                        showCardView("EMPTY");
                        lblTotalBooksCount.setText("(Lỗi kết nối CSDL)");
                        UiModalHelper.showToastError(BookCatalogView.this, "Lỗi kết nối MySQL: " + cause.getMessage());
                    });
                }
            }"""

if old_done in content:
    content = content.replace(old_done, new_done)
else:
    # Try with CRLF
    content = content.replace(old_done.replace('\n', '\r\n'), new_done.replace('\n', '\r\n'))

# 4. Update BookTitleCellRenderer to render cover image
old_renderer_field = """    private class BookTitleCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblTitle = new JLabel();
        private final JLabel lblSub = new JLabel();
        private final JPanel coverPanel;
        private Color currentCoverColor = UITheme.PRIMARY;
        private String bookInitials = "BK";"""

new_renderer_field = """    private class BookTitleCellRenderer extends JPanel implements TableCellRenderer {
        private static final Map<String, Image> COVER_CACHE = new ConcurrentHashMap<>();
        private final JLabel lblTitle = new JLabel();
        private final JLabel lblSub = new JLabel();
        private final JPanel coverPanel;
        private Color currentCoverColor = UITheme.PRIMARY;
        private String bookInitials = "BK";
        private Image currentCoverImg = null;"""

if old_renderer_field in content:
    content = content.replace(old_renderer_field, new_renderer_field)
else:
    content = content.replace(old_renderer_field.replace('\n', '\r\n'), new_renderer_field.replace('\n', '\r\n'))

# 5. Update paintComponent in BookTitleCellRenderer
old_paint = """                    // Bìa sách bo góc
                    g2.setColor(currentCoverColor);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                    // Gáy sách
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRect(0, 0, 4, getHeight());

                    // Chữ cái đầu sách
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(bookInitials)) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(bookInitials, tx, ty);"""

new_paint = """                    if (currentCoverImg != null) {
                        Shape oldClip = g2.getClip();
                        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                        g2.drawImage(currentCoverImg, 0, 0, getWidth(), getHeight(), null);
                        g2.setClip(oldClip);
                        g2.setColor(new Color(0, 0, 0, 25));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    } else {
                        // Bìa sách bo góc
                        g2.setColor(currentCoverColor);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                        // Gáy sách
                        g2.setColor(new Color(0, 0, 0, 30));
                        g2.fillRect(0, 0, 4, getHeight());

                        // Chữ cái đầu sách
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();
                        int tx = (getWidth() - fm.stringWidth(bookInitials)) / 2;
                        int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(bookInitials, tx, ty);
                    }"""

if old_paint in content:
    content = content.replace(old_paint, new_paint)
else:
    content = content.replace(old_paint.replace('\n', '\r\n'), new_paint.replace('\n', '\r\n'))

# 6. Update getTableCellRendererComponent in BookTitleCellRenderer
old_get_comp = """            if (value instanceof BookSummaryDto b) {
                lblTitle.setText(b.getTitle());
                lblSub.setText("Mã: " + b.getBookId() + " • " + b.getPublisherName());
                this.currentCoverColor = b.getCoverColor() != null ? b.getCoverColor() : UITheme.PRIMARY;
                this.bookInitials = UITheme.getAvatarInitial(b.getTitle());
            }"""

new_get_comp = """            if (value instanceof BookSummaryDto b) {
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
            }"""

if old_get_comp in content:
    content = content.replace(old_get_comp, new_get_comp)
else:
    content = content.replace(old_get_comp.replace('\n', '\r\n'), new_get_comp.replace('\n', '\r\n'))

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated BookCatalogView.java successfully.")
