package com.ximilib.presentation.component;

import com.ximilib.gui.util.UITheme;
import com.ximilib.presentation.util.UiModalHelper;
import raven.modal.component.SimpleModalBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * HelpGuideModalPanel: Cẩm nang Hướng dẫn Độc giả & Quy định Thư viện (SRS).
 * Hiển thị chính xác các quy chế thư viện BR-01..BR-08.
 * Tuân thủ R-09, R-11.
 */
public class HelpGuideModalPanel extends JPanel {

    public HelpGuideModalPanel() {
        setPreferredSize(new Dimension(560, 480));
        setBackground(UITheme.CARD_BG);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(16, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText("""
            <html>
            <body style='font-family:Segoe UI, sans-serif; font-size:12px; color:#1F2937;'>
                <h3 style='color:#0F766E; margin-top:0;'>📖 CẨM NANG BẠN ĐỌC — THƯ VIỆN XIMILIB (ĐHBK ĐÀ NẴNG)</h3>
                <p>Chào mừng bạn đến với Thư viện XimiLib. Vui lòng tuân thủ các quy định mượn trả sau:</p>

                <h4 style='color:#0F766E; margin-bottom:4px;'>1. Giờ Mở Cửa & Phục Vụ</h4>
                <ul>
                    <li>Thứ 2 đến Thứ 6: <b>07:30 - 21:00</b></li>
                    <li>Thứ 7 & Chủ Nhật: <b>08:00 - 17:00</b></li>
                </ul>

                <h4 style='color:#0F766E; margin-bottom:4px;'>2. Hạn Mức Mượn Sách (BR-02, BR-04)</h4>
                <ul>
                    <li><b>Mượn về nhà:</b> Tối đa <b>3 cuốn sách</b> trong thời hạn <b>14 ngày</b>.</li>
                    <li><b>Đọc tại chỗ:</b> Không giới hạn số lượng nhưng phải hoàn trả trước <b>17:00</b> cùng ngày.</li>
                </ul>

                <h4 style='color:#0F766E; margin-bottom:4px;'>3. Gia Hạn Trực Tuyến (BR-03)</h4>
                <ul>
                    <li>Mỗi phiếu mượn được gia hạn trực tuyến <b>1 lần (+7 ngày)</b> trên ứng dụng.</li>
                    <li>Sách đã quá hạn hoặc bị độc giả khác đặt trước sẽ không được gia hạn.</li>
                </ul>

                <h4 style='color:#0F766E; margin-bottom:4px;'>4. Phạt Trễ Hạn & Bồi Thường (BR-06, BR-08)</h4>
                <ul>
                    <li><b>Phạt quá hạn:</b> <b>2.000 VNĐ / ngày / cuốn</b>. Độc giả còn nợ phạt sẽ bị tạm khóa quyền mượn sách.</li>
                    <li><b>Làm mất / hỏng sách:</b> Bồi thường <b>200% giá bìa</b> + <b>20.000 VNĐ</b> phí xử lý kỹ thuật + tiền phạt trễ (nếu có).</li>
                </ul>

                <h4 style='color:#0F766E; margin-bottom:4px;'>5. Hỗ Trợ & Liên Hệ</h4>
                <p>📍 Địa chỉ: 54 Nguyễn Lương Bằng, Hòa Khánh Bắc, Liên Chiểu, Đà Nẵng.<br>
                ☎️ Hotline hỗ trợ: <b>0236 3842 323</b> | 📧 Email: <b>thuvien@dut.udn.vn</b></p>
            </body>
            </html>
        """);
        editorPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(editorPane);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.CARD_BORDER, 1));
        add(scroll, BorderLayout.CENTER);
    }

    public static void show(Component parent) {
        HelpGuideModalPanel guidePanel = new HelpGuideModalPanel();
        UiModalHelper.showCustomModal(
            parent,
            "HELP_GUIDE",
            "Hướng Dẫn Quy Định Thư Viện",
            guidePanel,
            SimpleModalBorder.DEFAULT_OPTION,
            null
        );
    }
}
