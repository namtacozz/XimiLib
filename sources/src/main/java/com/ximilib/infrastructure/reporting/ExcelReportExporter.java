package com.ximilib.infrastructure.reporting;

import com.ximilib.application.port.ReportExporter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportExporter implements ReportExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void exportToExcel(OutputStream outputStream, String sheetTitle, String[] headers, List<Object[]> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String sanitized = (sheetTitle != null && !sheetTitle.isBlank()) ? sheetTitle.replace('/', '-') : "BaoCao";
            String safeTitle = WorkbookUtil.createSafeSheetName(sanitized);
            Sheet sheet = workbook.createSheet(safeTitle);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createBorderedStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (rows != null) {
                for (Object[] rowData : rows) {
                    Row dataRow = sheet.createRow(rowIdx++);
                    dataRow.setHeightInPoints(22);

                    for (int col = 0; col < headers.length; col++) {
                        Cell cell = dataRow.createCell(col);
                        Object val = (col < rowData.length) ? rowData[col] : null;

                        if (val == null) {
                            cell.setCellValue("");
                            cell.setCellStyle(textStyle);
                        } else if (val instanceof BigDecimal bd) {

                            cell.setCellValue(bd.doubleValue());
                            cell.setCellStyle(currencyStyle);
                        } else if (val instanceof Number num) {
                            cell.setCellValue(num.doubleValue());
                            cell.setCellStyle(numberStyle);
                        } else if (val instanceof LocalDate ld) {
                            cell.setCellValue(ld.format(DATE_FMT));
                            cell.setCellStyle(dateStyle);
                        } else if (val instanceof LocalDateTime ldt) {
                            cell.setCellValue(ldt.format(DATETIME_FMT));
                            cell.setCellStyle(dateStyle);
                        } else if (val instanceof Boolean b) {
                            cell.setCellValue(b ? "Có" : "Không");
                            cell.setCellStyle(textStyle);
                        } else {
                            cell.setCellValue(val.toString());
                            cell.setCellStyle(textStyle);
                        }
                    }
                }
            }

            for (int col = 0; col < headers.length; col++) {
                sheet.autoSizeColumn(col);
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(Math.max(currentWidth + 1200, 3500), 18000));
            }

            workbook.write(outputStream);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createBorderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("#,##0\" VNĐ\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
