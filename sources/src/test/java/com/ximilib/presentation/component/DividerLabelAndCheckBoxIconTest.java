package com.ximilib.presentation.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JCheckBox;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Thành phần Giao diện PR 1: DividerLabel & VectorCheckBoxIcon")
class DividerLabelAndCheckBoxIconTest {

    @Test
    @DisplayName("TC-UI01: DividerLabel khởi tạo và vẽ không lỗi")
    void testDividerLabelRendering() {
        DividerLabel label = new DividerLabel("hoặc");
        label.setSize(360, 24);

        BufferedImage img = new BufferedImage(360, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        assertDoesNotThrow(() -> label.paintComponent(g2));
        g2.dispose();
    }

    @Test
    @DisplayName("TC-UI02: VectorCheckBoxIcon vẽ chuẩn ở cả hai trạng thái chọn và bỏ chọn")
    void testVectorCheckBoxIconRendering() {
        VectorCheckBoxIcon icon = new VectorCheckBoxIcon(16);
        assertEquals(20, icon.getIconWidth());
        assertEquals(20, icon.getIconHeight());

        JCheckBox chk = new JCheckBox("Test", false);
        chk.setSize(100, 30);
        BufferedImage img = new BufferedImage(100, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        assertDoesNotThrow(() -> icon.paintIcon(chk, g2, 0, 0));

        chk.setSelected(true);
        assertDoesNotThrow(() -> icon.paintIcon(chk, g2, 0, 0));

        g2.dispose();
    }
}
