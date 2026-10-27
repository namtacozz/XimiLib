package com.ximilib.gui.components;

import com.ximilib.presentation.component.DigitalCardPopupPanel;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.Component;

/**
 * DigitalCardDialog: Adapter hien thi The Thu Vien Dien Tu bang GlassPanePopup (In-App Overlay)
 * mo phong theo widget smartphone cua LibRack.
 * Triet tieu hoan toan OS JDialog truyen thong theo dung quy chuan XimiLib.
 * Tac gia: Phan Vu Xuan Kien
 */
public class DigitalCardDialog {

    private final Component parent;
    private final String cardId;
    private final String memberName;

    public DigitalCardDialog(Component parent, String cardId, String memberName) {
        this.parent = parent;
        this.cardId = cardId;
        this.memberName = memberName;
    }

    public static void show(Component parent, String cardId, String memberName) {
        UiModalHelper.showQuickPopup(new DigitalCardPopupPanel(cardId, memberName));
    }

    public void setVisible(boolean visible) {
        if (visible) {
            show(parent, cardId, memberName);
        }
    }
}
