package com.ximilib.presentation.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.*;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.Toast;
import raven.modal.component.SimpleModalBorder;
import raven.modal.listener.ModalCallback;
import raven.modal.option.Option;
import raven.popup.DefaultOption;
import raven.popup.GlassPanePopup;
import raven.popup.component.GlassPaneChild;
import raven.popup.component.SimplePopupBorder;

/**
 * UiModalHelper: Bo cong cu Helper/Factory chuan hoa toan bo giao dien
 * tren nen FlatLaf ket hop 3 thu vien DJ-Raven:
 * 1. raven.modal (ModalDialog, SimpleModalBorder, Toast)
 * 2. raven.popup (GlassPanePopup)
 * 3. raven.datetime (DatePicker, TimePicker)
 *
 * Tuyet doi thay the 100% JOptionPane va JDialog truyen thong bang In-App Modal Overlay.
 */
public class UiModalHelper {

    /**
     * Khoi tao GlassPanePopup va cau hinh mac dinh cho ModalDialog tren JFrame chinh.
     * Goi 1 lan duy nhat khi khoi dong cua so (MainDashboard / LoginForm).
     */
    public static void initMainFrame(JFrame frame) {
        if (frame != null && (!GlassPanePopup.isInit() || GlassPanePopup.getMainFrame() != frame)) {
            GlassPanePopup.install(frame);
            // Chan snapshot animation tranh loi NullPointerException: VolatileImage.createGraphics()
            GlassPanePopup.setDefaultOption(new DefaultOption() {
                @Override
                public boolean useSnapshot() {
                    return false;
                }
            });
        }
        // Cau hinh mac dinh ModalDialog: Chon BLOCK background click de tranh bi dong popup lap tuc
        Option defaultModalOption = ModalDialog.createOption();
        defaultModalOption.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
        ModalDialog.setDefaultOption(defaultModalOption);
    }

    // =========================================================================
    // 1. TOAST NOTIFICATIONS (Thay the JOptionPane.showMessageDialog)
    // =========================================================================

    public static void showToastSuccess(Component parent, String message) {
        runOnEdt(() -> Toast.show(parent, Toast.Type.SUCCESS, message));
    }

    public static void showToastError(Component parent, String message) {
        runOnEdt(() -> Toast.show(parent, Toast.Type.ERROR, message));
    }

    public static void showToastWarning(Component parent, String message) {
        runOnEdt(() -> Toast.show(parent, Toast.Type.WARNING, message));
    }

    public static void showToastInfo(Component parent, String message) {
        runOnEdt(() -> Toast.show(parent, Toast.Type.INFO, message));
    }

    // =========================================================================
    // 2. MODAL DIALOGS (Thay the JOptionPane.showConfirmDialog & Custom Dialog)
    // =========================================================================

    /**
     * Hien thi hop thoai xac nhan (YES / NO) bang SimpleModalBorder.
     */
    public static void showConfirm(Component parent, String title, String message, Runnable onConfirm) {
        runOnEdt(() -> {
            JLabel lblMessage = new JLabel("<html><body style='width: 280px; padding: 4px;'>" + message + "</body></html>");
            lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            SimpleModalBorder border = new SimpleModalBorder(
                lblMessage,
                title,
                SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {
                        controller.close();
                        if (onConfirm != null) {
                            onConfirm.run();
                        }
                    } else if (action == SimpleModalBorder.NO_OPTION || action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                        controller.close();
                    }
                }
            );

            Option option = ModalDialog.createOption();
            option.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
            ModalDialog.showModal(parent, border, option);
        });
    }

    /**
     * Hien thi Custom Form Modal (Them/Sua sach, Lap phieu muon, Bao mat...)
     */
    public static void showCustomModal(Component parent, String id, String title, Component content, int optionType, ModalCallback callback) {
        runOnEdt(() -> {
            SimpleModalBorder border = new SimpleModalBorder(content, title, optionType, callback);
            Option option = ModalDialog.createOption();
            option.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
            if (content.getPreferredSize() != null && content.getPreferredSize().width > 0) {
                option.getLayoutOption().setSize(content.getPreferredSize().width + 48, content.getPreferredSize().height + 80);
            }
            if (id != null && !id.isBlank()) {
                ModalDialog.showModal(parent, border, option, id);
            } else {
                ModalDialog.showModal(parent, border, option);
            }
        });
    }

    /**
     * Hien thi Modal voi nut dong co ban
     */
    public static void showModal(Component parent, String title, Component content) {
        runOnEdt(() -> {
            SimpleModalBorder border = new SimpleModalBorder(content, title, SimpleModalBorder.CLOSE_OPTION, (controller, action) -> controller.close());
            Option option = ModalDialog.createOption();
            option.setBackgroundClickType(Option.BackgroundClickType.BLOCK);
            ModalDialog.showModal(parent, border, option);
        });
    }

    public static void closeModal(String id) {
        runOnEdt(() -> {
            if (id != null && ModalDialog.isIdExist(id)) {
                ModalDialog.closeModal(id);
            }
        });
    }

    // =========================================================================
    // 3. GLASSPANE POPUP (Quick Lightweight Overlays: Digital Card, Chuong Thong Bao)
    // =========================================================================

    public static void showQuickPopup(Component content) {
        runOnEdt(() -> {
            com.ximilib.presentation.view.MainDashboardFrame mainFrame = com.ximilib.presentation.view.MainDashboardFrame.getInstance();
            if (mainFrame != null && (!GlassPanePopup.isInit() || GlassPanePopup.getMainFrame() != mainFrame)) {
                initMainFrame(mainFrame);
            }
            DefaultOption safeOption = new DefaultOption() {
                @Override
                public boolean useSnapshot() {
                    return false;
                }
            };
            if (content instanceof GlassPaneChild) {
                GlassPanePopup.showPopup((GlassPaneChild) content, safeOption);
            } else {
                GlassPaneChild wrapper = new GlassPaneChild() {
                    {
                        setLayout(new java.awt.BorderLayout());
                        setOpaque(false);
                        add(content);
                    }
                };
                GlassPanePopup.showPopup(wrapper, safeOption);
            }
        });
    }

    public static void showQuickPopup(Component content, String title) {
        runOnEdt(() -> {
            com.ximilib.presentation.view.MainDashboardFrame mainFrame = com.ximilib.presentation.view.MainDashboardFrame.getInstance();
            if (mainFrame != null && (!GlassPanePopup.isInit() || GlassPanePopup.getMainFrame() != mainFrame)) {
                initMainFrame(mainFrame);
            }
            SimplePopupBorder border = new SimplePopupBorder(content, title);
            DefaultOption safeOption = new DefaultOption() {
                @Override
                public boolean useSnapshot() {
                    return false;
                }
            };
            GlassPanePopup.showPopup(border, safeOption);
        });
    }

    public static void closePopup() {
        runOnEdt(GlassPanePopup::closePopupLast);
    }

    // =========================================================================
    // 4. DATE & TIME PICKER (Chuẩn hóa LocalDate, LocalTime)
    // =========================================================================

    /**
     * Tao DatePicker gan vao JFormattedTextField voi format dd/MM/yyyy.
     * @param editor Text field hien thi va nhan nhap ngay
     * @param isRange true neu chon khoang ngay (Between), false neu chon 1 ngay
     */
    public static DatePicker createDatePicker(JFormattedTextField editor, boolean isRange) {
        DatePicker datePicker = new DatePicker();
        datePicker.setDateFormat("dd/MM/yyyy");
        datePicker.setDateSelectionMode(isRange
            ? DatePicker.DateSelectionMode.BETWEEN_DATE_SELECTED
            : DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setCloseAfterSelected(true);
        datePicker.setEditor(editor);
        return datePicker;
    }

    /**
     * Tao TimePicker gan vao JFormattedTextField (mac dinh 24h: HH:mm).
     */
    public static TimePicker createTimePicker(JFormattedTextField editor, boolean is24Hour) {
        TimePicker timePicker = new TimePicker();
        timePicker.set24HourView(is24Hour);
        timePicker.setEditor(editor);
        return timePicker;
    }

    /**
     * Helper dam bao thuc thi tren Swing EDT
     */
    public static void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}
