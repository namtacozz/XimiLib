package com.ximilib.presentation.component;

import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.presentation.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

/**
 * BookFormModalPanel: Form Thêm/Sửa thông tin sách
 * Tích hợp lưu vào CSDL và DatePicker chuẩn dd/MM/yyyy.
 */
public class BookFormModalPanel extends JPanel {

    private final BookSummaryDto existingBook;
    private JTextField txtTitle;
    private JTextField txtIsbn;
    private JTextField txtAuthor;
    private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbPublisher;
    private JComboBox<String> cmbShelf;
    private JTextField txtPrice;
    private JSpinner spnCopies;
    private JFormattedTextField txtImportDate;
    private DatePicker importDatePicker;

    public BookFormModalPanel(BookSummaryDto book) {
        this.existingBook = book;
        setLayout(new BorderLayout());
        setOpaque(false);
        initUI();
    }

    public static void show(Component parent, BookSummaryDto book, Runnable onSaveSuccess) {
        BookFormModalPanel panel = new BookFormModalPanel(book);
        String title = (book == null) ? "Thêm Sách Mới" : "Chỉnh Sửa Thông Tin Sách";

        SimpleModalBorder border = new SimpleModalBorder(
            panel,
            title,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    if (panel.validateForm()) {
                        try {
                            String t = panel.txtTitle.getText().trim();
                            String a = panel.txtAuthor.getText().trim();
                            String cat = (String) panel.cmbCategory.getSelectedItem();
                            String pub = (String) panel.cmbPublisher.getSelectedItem();
                            String shelf = (String) panel.cmbShelf.getSelectedItem();
                            String isbn = panel.txtIsbn.getText().trim();
                            int copies = (Integer) panel.spnCopies.getValue();
                            double price = 150000;
                            try {
                                String cleanPrice = panel.txtPrice.getText().trim().replace(".", "").replace(",", "");
                                price = Double.parseDouble(cleanPrice);
                            } catch (Exception ignored) {}

                            AppContainer.getInstance().getBookCatalogService().saveNewBook(t, a, cat, pub, shelf, isbn, price, copies);
                            controller.close();
                            UiModalHelper.showToastSuccess(parent, "Thêm sách thành công!");
                            if (onSaveSuccess != null) onSaveSuccess.run();
                        } catch (Exception ex) {
                            UiModalHelper.showToastError(parent, "Lỗi lưu sách: " + ex.getMessage());
                        }
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
        Option option = ModalDialog.createOption();
        option.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
        option.getLayoutOption().setSize(520, 520);
        ModalDialog.showModal(parent, border, option, "BOOK_FORM_MODAL");
    }

    private void initUI() {
        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        form.setPreferredSize(new Dimension(460, 360));

        txtTitle = new JTextField(existingBook != null ? existingBook.getTitle() : "");
        txtIsbn = new JTextField(existingBook != null ? ("ISBN-" + existingBook.getBookId()) : "978-604-");
        txtAuthor = new JTextField(existingBook != null ? existingBook.getAuthor() : "");
        
        String[] categories = {"Công nghệ thông tin", "Khoa học tự nhiên", "Kinh tế - Quản trị", "Văn học & Nghệ thuật", "Ngoại ngữ", "Kỹ năng sống", "Tâm lý học"};
        cmbCategory = new JComboBox<>(categories);
        if (existingBook != null && existingBook.getCategoryName() != null) {
            cmbCategory.setSelectedItem(existingBook.getCategoryName());
        }

        String[] publishers = {"NXB Bách Khoa", "NXB Trẻ", "NXB Giáo Dục", "NXB Kim Đồng", "NXB Tri Thức", "NXB Khoa Học & Kỹ Thuật", "O'Reilly Media", "Addison-Wesley"};
        cmbPublisher = new JComboBox<>(publishers);
        cmbPublisher.setEditable(true);
        if (existingBook != null && existingBook.getPublisherName() != null) {
            cmbPublisher.setSelectedItem(existingBook.getPublisherName());
        }

        String[] shelves = {"Kệ IT-01 (Khu A)", "Kệ A1 - Toán & Lý luận (Khu A)", "Kệ A2 - Cơ khí & Kỹ thuật (Khu A)", "Kệ B1 - Văn học kinh điển (Khu B)", "Kệ B2 - Kỹ năng sống & Tâm lý (Khu B)", "Kệ C1 - Lập trình & Phần mềm (Khu C)", "Kệ C2 - Ngoại ngữ & Từ điển (Khu C)"};
        cmbShelf = new JComboBox<>(shelves);
        cmbShelf.setEditable(true);

        txtPrice = new JTextField(existingBook != null ? "150.000" : "150.000");

        spnCopies = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));

        // Ngày nhập kho với DatePicker (dd/MM/yyyy)
        txtImportDate = new JFormattedTextField();
        importDatePicker = UiModalHelper.createDatePicker(txtImportDate, false);
        importDatePicker.setSelectedDate(LocalDate.now());

        form.add(new JLabel("Tên đầu sách (*):"));
        form.add(txtTitle);

        form.add(new JLabel("Thể loại:"));
        form.add(cmbCategory);

        form.add(new JLabel("Tác giả (*):"));
        form.add(txtAuthor);

        form.add(new JLabel("Nhà xuất bản:"));
        form.add(cmbPublisher);

        form.add(new JLabel("Vị trí kệ:"));
        form.add(cmbShelf);

        form.add(new JLabel("Giá bìa (VNĐ):"));
        form.add(txtPrice);

        form.add(new JLabel("Số lượng nhập kho:"));
        form.add(spnCopies);

        form.add(new JLabel("Mã ISBN:"));
        form.add(txtIsbn);

        form.add(new JLabel("Ngày nhập kho:"));
        form.add(txtImportDate);

        add(form, BorderLayout.CENTER);
    }

    public boolean validateForm() {
        if (txtTitle.getText().trim().isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng nhập tên đầu sách!");
            txtTitle.requestFocus();
            return false;
        }
        if (txtAuthor.getText().trim().isEmpty()) {
            UiModalHelper.showToastWarning(this, "Vui lòng nhập tên tác giả!");
            txtAuthor.requestFocus();
            return false;
        }
        return true;
    }
}
