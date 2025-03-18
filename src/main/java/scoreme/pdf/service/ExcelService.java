package scoreme.pdf.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import scoreme.pdf.entity.TableData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ExcelService {

    public String exportTablesToSingleExcel(List<TableData> tables, String outputDir, String fileName) throws IOException {
        String outputPath = Paths.get(outputDir, fileName).toString();

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (TableData table : tables) {
                String sheetName = sanitizeSheetName(table.getTableName());
                Sheet sheet = workbook.createSheet(sheetName);

                // Create header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < table.getHeaders().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(table.getHeaders().get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                List<List<String>> rows = table.getRows();
                for (int i = 0; i < rows.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    List<String> rowData = rows.get(i);

                    for (int j = 0; j < rowData.size(); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(rowData.get(j));
                    }
                }

                // Auto-size columns
                for (int i = 0; i < table.getHeaders().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            // Save the workbook
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }

        return outputPath;
    }

    public List<String> exportTablesToMultipleExcel(List<TableData> tables, String outputDir, String baseFileName) throws IOException {
        List<String> outputFiles = new ArrayList<>();

        for (TableData table : tables) {
            String fileName = "table_" + table.getPageNumber() + "_" + table.getTableNumber() + "_" + UUID.randomUUID() + ".xlsx";
            String outputPath = Paths.get(outputDir, fileName).toString();

            try (Workbook workbook = new XSSFWorkbook()) {
                CellStyle headerStyle = createHeaderStyle(workbook);
                Sheet sheet = workbook.createSheet("Table Data");

                // Create header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < table.getHeaders().size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(table.getHeaders().get(i));
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                List<List<String>> rows = table.getRows();
                for (int i = 0; i < rows.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    List<String> rowData = rows.get(i);

                    for (int j = 0; j < rowData.size(); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(rowData.get(j));
                    }
                }

                // Auto-size columns
                for (int i = 0; i < table.getHeaders().size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Save the workbook
                try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                    workbook.write(fileOut);
                }

                outputFiles.add(outputPath);
            }
        }

        return outputFiles;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        return headerStyle;
    }

    private String sanitizeSheetName(String name) {
        // Excel sheet names must be 31 characters or less and cannot contain
        // these characters: : \ / ? * [ ]
        String sanitized = name.replaceAll("[:\\\\/?*\\[\\]]", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
    }
}