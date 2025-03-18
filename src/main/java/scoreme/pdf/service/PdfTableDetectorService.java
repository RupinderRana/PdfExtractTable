package scoreme.pdf.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;
import scoreme.pdf.DTO.TableExtractionResult;
import scoreme.pdf.entity.TableData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PdfTableDetectorService {

    public int getPageCount(String pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            return document.getNumberOfPages();
        }
    }

    public TableExtractionResult detectAndExtractTables(String pdfPath) {
        List<TableData> tables = new ArrayList<>();

        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                List<TableData> pageTables = detectTablesInPage(document, i);
                tables.addAll(pageTables);
            }

            return new TableExtractionResult(true, "Tables extracted successfully", tables);
        } catch (IOException e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            return new TableExtractionResult(false, "Error processing PDF: " + e.getMessage(), new ArrayList<>());
        }
    }

    private List<TableData> detectTablesInPage(PDDocument document, int pageNumber) throws IOException {
        List<TableData> tables = new ArrayList<>();

        // Create a custom text stripper to extract text with position information
        CustomTextStripper stripper = new CustomTextStripper();
        stripper.setStartPage(pageNumber + 1);
        stripper.setEndPage(pageNumber + 1);
        stripper.getText(document);

        // Get the lines extracted by our custom stripper
        List<List<TextPosition>> lines = stripper.getLines();

        // Table detection algorithm
        int tableCount = 0;

        // This is a simplified approach. You would need to implement
        // more sophisticated logic for real-world PDFs
        for (int i = 0; i < lines.size(); i++) {
            // Check if this line could be a table header
            if (isLikelyTableHeader(lines, i)) {
                // Extract table data
                TableData table = extractTable(lines, i, pageNumber, tableCount);
                if (table != null && !table.getRows().isEmpty()) {
                    tables.add(table);
                    tableCount++;

                    // Skip the lines we've already processed as part of this table
                    i += table.getRows().size();
                }
            }
        }

        return tables;
    }

    private static class CustomTextStripper extends PDFTextStripper {
        private final List<List<TextPosition>> lines = new ArrayList<>();

        public CustomTextStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (!textPositions.isEmpty()) {
                lines.add(new ArrayList<>(textPositions));
            }
            super.writeString(text, textPositions);
        }

        public List<List<TextPosition>> getLines() {
            return lines;
        }
    }

    private boolean isLikelyTableHeader(List<List<TextPosition>> lines, int lineIndex) {
        if (lineIndex >= lines.size()) {
            return false;
        }

        List<TextPosition> line = lines.get(lineIndex);

        // Check if line has multiple text segments with similar Y positions
        if (line.size() > 3) {
            return true;
        }

        // Check if line contains common table header indicators
        String lineText = getTextFromPositions(line);
        String[] headerIndicators = {"id", "name", "date", "amount", "description", "total"};
        for (String indicator : headerIndicators) {
            if (lineText.toLowerCase().contains(indicator)) {
                return true;
            }
        }

        return false;
    }

    private TableData extractTable(List<List<TextPosition>> lines, int startLine, int pageNumber, int tableNumber) {
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        // Extract headers
        if (startLine < lines.size()) {
            List<TextPosition> headerLine = lines.get(startLine);
            List<TextPosition> sortedHeaderPositions = sortTextPositionsByX(headerLine);

            for (TextPosition position : sortedHeaderPositions) {
                headers.add(position.getUnicode().trim());
            }
        }

        // Extract rows with flexible column detection
        int currentLine = startLine + 1;
        while (currentLine < lines.size()) {
            List<TextPosition> rowLine = lines.get(currentLine);
            List<String> rowData = extractRowData(rowLine, headers.size());

            if (!rowData.isEmpty()) {
                rows.add(rowData);
            }

            currentLine++;
        }

        if (headers.isEmpty() || rows.isEmpty()) {
            return null;
        }

        return new TableData("Table_" + (pageNumber + 1) + "_" + (tableNumber + 1),
                headers, rows, pageNumber + 1, tableNumber + 1);
    }

    private List<String> extractRowData(List<TextPosition> rowLine, int expectedColumns) {
        List<String> rowData = new ArrayList<>();

        // Group text positions by their X position to identify columns
        List<TextPosition> sortedPositions = sortTextPositionsByX(rowLine);

        for (TextPosition position : sortedPositions) {
            rowData.add(position.getUnicode().trim());
        }

        // Ensure we have the expected number of columns
        while (rowData.size() < expectedColumns) {
            rowData.add("");
        }

        return rowData;
    }

    private List<TextPosition> sortTextPositionsByX(List<TextPosition> positions) {
        List<TextPosition> sorted = new ArrayList<>(positions);
        sorted.sort((p1, p2) -> Float.compare(p1.getX(), p2.getX()));
        return sorted;
    }

    private String getTextFromPositions(List<TextPosition> positions) {
        StringBuilder sb = new StringBuilder();
        for (TextPosition position : positions) {
            sb.append(position.getUnicode());
        }
        return sb.toString();
    }
}