package com.ximilib.presentation.component;

import com.ximilib.application.dto.BookSummaryDto;
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
 * BookFormModalPanel: Form Them/Sua dau sach & cuon sach vat ly (UC-3)
 * tich hop DatePicker cho truong ngay nhap/ngay xuat ban.
 */
public class BookFormModalPanel extends JPanel {

    private final BookSummaryDto existingBook;
    private JTextField txtTitle;
    private JTextField txtIsbn;
    private JTextField txtAuthor;
    private JComboBox<String> cmbCategory;
    private JTextField txtPrice;
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
        String title = (book == null) ? "Thêm Đầu Sách Mới (UC-3)" : "Chỉnh Sửa Thông Tin Sách";

        SimpleModalBorder border = new SimpleModalBorder(
            panel,
            title,
            SimpleModalBorder.OK_CANCEL_OPTION,
            (controller, action) -> {
                if (action == SimpleModalBorder.OK_OPTION) {
                    if (panel.validateForm()) {
                        controller.close();
                        UiModalHelper.showToastSuccess(parent, "Đã lưu thông tin đầu sách thành công!");
                        if (onSaveSuccess != null) onSaveSuccess.run();
                    }
                } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
        Option option = ModalDialog.createOption();
        option.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
        ModalDialog.showModal(parent, border, option, "BOOK_FORM_MODAL");
    }

    private void initUI() {
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        form.setPreferredSize(new Dimension(420, 240));

        txtTitle = new JTextField(existingBook != null ? existingBook.getTitle() : "");
        txtIsbn = new JTextField(existingBook != null ? ("ISBN-" + existingBook.getBookId()) : "978-604-");
        txtAuthor = new JTextField(existingBook != null ? existingBook.getAuthor() : "");
        
        String[] categories = {"Công nghệ thông tin", "Khoa học tự nhiên", "Kinh tế - Quản trị", "Văn học & Nghệ thuật", "Ngoại ngữ"};
        cmbCategory = new JComboBox<>(categories);
        if (existingBook != null && existingBook.getCategoryName() != null) {
            cmbCategory.setSelectedItem(existingBook.getCategoryName());
        }

        txtPrice = new JTextField(existingBook != null ? "85.000" : "50.000");

        // Ngày nhập kho với DatePicker (dd/MM/yyyy)
        txtImportDate = new JFormattedTextField();
        importDatePicker = UiModalHelper.createDatePicker(txtImportDate, false);
        importDatePicker.setSelectedDate(LocalDate.now());

        form.add(new JLabel("Tên đầu sách (*):"));
        form.add(txtTitle);

        form.add(new JLabel("Mã ISBN:"));
        form.add(txtIsbn);

        form.add(new JLabel("Tác giả (*):"));
        form.add(txtAuthor);

        form.add(new JLabel("Thể loại:"));
        form.add(cmbCategory);

        form.add(new JLabel("Giá bìa (VNĐ):"));
        form.add(txtPrice);

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
