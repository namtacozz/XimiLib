package com.ximilib.application.port;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * ReportExporter: Cổng xuất dữ liệu báo cáo thống kê ra các định dạng tệp tin ngoại vi (như .xlsx).
 * Tuân thủ R-01 (không phụ thuộc framework UI hay Apache POI cụ thể ở interface).
 */
public interface ReportExporter {

    /**
     * Xuất dữ liệu bảng báo cáo ra file Excel .xlsx.
     *
     * @param outputStream Luồng xuất file
     * @param sheetTitle   Tên sheet trong Excel
     * @param headers      Mảng tiêu đề các cột
     * @param rows         Danh sách các dòng dữ liệu
     * @throws IOException khi gặp lỗi ghi file
     */
    void exportToExcel(OutputStream outputStream, String sheetTitle, String[] headers, List<Object[]> rows) throws IOException;
}
