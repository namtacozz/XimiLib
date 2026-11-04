package com.ximilib.application.port;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ReportExporter {

    void exportToExcel(OutputStream outputStream, String sheetTitle, String[] headers, List<Object[]> rows) throws IOException;
}
