package com.ximilib.infrastructure.reporting;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Xuất Báo Cáo Excel bằng Apache POI (ExcelReportExporter)")
class ExcelReportExporterTest {

    private final ExcelReportExporter exporter = new ExcelReportExporter();

    @Test
    @DisplayName("TC-XLSX-01: Xuất file .xlsx ra file tạm và đọc lại kiểm tra tính toàn vẹn dữ liệu")
    void testExportToExcelAndReadBack() throws IOException {
        String sheetTitle = "Thống Kê Sách";
        String[] headers = new String[]{"Mã Đầu Sách", "Tên Sách", "Số Cuốn", "Giá Bìa", "Ngày Nhập"};

        LocalDate today = LocalDate.of(2026, 11, 1);
        BigDecimal price = new BigDecimal("125000.00");

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"MS001", "Clean Code Chuyên Sâu", 5, price, today});
        rows.add(new Object[]{"MS002", "Kiến Trúc Java Nâng Cao", 12, new BigDecimal("89000.50"), today.minusDays(3)});

        Path tempFile = Files.createTempFile("ximilib_test_report_", ".xlsx");
        try {

            try (OutputStream fos = Files.newOutputStream(tempFile)) {
                exporter.exportToExcel(fos, sheetTitle, headers, rows);
            }

            assertTrue(Files.exists(tempFile));
            assertTrue(Files.size(tempFile) > 0, "File .xlsx tạo ra không được rỗng");

            try (InputStream fis = Files.newInputStream(tempFile);
                 Workbook wb = new XSSFWorkbook(fis)) {

                assertEquals(1, wb.getNumberOfSheets());
                Sheet sheet = wb.getSheetAt(0);
                assertEquals(sheetTitle, sheet.getSheetName());

                Row headerRow = sheet.getRow(0);
                assertNotNull(headerRow);
                assertEquals(5, headerRow.getPhysicalNumberOfCells());
                for (int i = 0; i < headers.length; i++) {
                    assertEquals(headers[i], headerRow.getCell(i).getStringCellValue());
                }

                Row dataRow1 = sheet.getRow(1);
                assertNotNull(dataRow1);
                assertEquals("MS001", dataRow1.getCell(0).getStringCellValue());
                assertEquals("Clean Code Chuyên Sâu", dataRow1.getCell(1).getStringCellValue());
                assertEquals(5.0, dataRow1.getCell(2).getNumericCellValue(), 0.001);
                assertEquals(125000.0, dataRow1.getCell(3).getNumericCellValue(), 0.001);

                String dateStr = dataRow1.getCell(4).getStringCellValue();
                assertEquals("01/11/2026", dateStr);

                Row dataRow2 = sheet.getRow(2);
                assertNotNull(dataRow2);
                assertEquals("MS002", dataRow2.getCell(0).getStringCellValue());
                assertEquals(12.0, dataRow2.getCell(2).getNumericCellValue(), 0.001);
                assertEquals(89000.50, dataRow2.getCell(3).getNumericCellValue(), 0.001);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("TC-XLSX-02: Xuất bảng rỗng không có dòng dữ liệu vẫn tạo đúng tiêu đề cột")
    void testExportEmptyRows() throws IOException {
        String[] headers = new String[]{"Cột 1", "Cột 2"};
        Path tempFile = Files.createTempFile("ximilib_test_empty_", ".xlsx");
        try {
            try (OutputStream fos = Files.newOutputStream(tempFile)) {
                exporter.exportToExcel(fos, "EmptySheet", headers, List.of());
            }

            try (InputStream fis = Files.newInputStream(tempFile);
                 Workbook wb = new XSSFWorkbook(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                assertEquals(1, sheet.getPhysicalNumberOfRows(), "Chỉ có đúng 1 dòng tiêu đề cột");
                assertEquals("Cột 1", sheet.getRow(0).getCell(0).getStringCellValue());
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
