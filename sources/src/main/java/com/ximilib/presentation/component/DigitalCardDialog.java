package com.ximilib.presentation.component;

import com.ximilib.presentation.component.DigitalCardPopupPanel;
import com.ximilib.presentation.util.UiModalHelper;
import java.awt.Component;

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
