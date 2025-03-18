package scoreme.pdf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import scoreme.pdf.DTO.PdfUploadResponse;
import scoreme.pdf.DTO.TableExtractionResult;
import scoreme.pdf.entity.PdfFileInfo;
import scoreme.pdf.entity.TableData;
import scoreme.pdf.repository.PdfRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final PdfRepository pdfRepository;
    private final PdfTableDetectorService tableDetectorService;
    private final ExcelService excelService;

    @Value("${pdf.upload.dir}")
    private String uploadDir;

    @Value("${pdf.output.dir}")
    private String outputDir;

    public PdfUploadResponse processPdfFile(MultipartFile file, boolean consolidateToOneFile) throws IOException {
        // Create directories if they don't exist
        createDirectories();

        // Save the file
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // Process the PDF file
        PdfFileInfo fileInfo = new PdfFileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(filePath.toString());

        // Detect and extract tables
        TableExtractionResult result = tableDetectorService.detectAndExtractTables(filePath.toString());

        if (!result.isSuccess()) {
            return new PdfUploadResponse("Failed to extract tables: " + result.getMessage(),
                    fileName, 0, new ArrayList<>());
        }

        fileInfo.setTables(result.getTables());
        fileInfo.setPageCount(tableDetectorService.getPageCount(filePath.toString()));

        // Save PDF info to repository
        pdfRepository.savePdfInfo(fileInfo);

        // Export tables to Excel
        List<String> outputFiles;
        if (consolidateToOneFile) {
            String outputFileName = "output_" + UUID.randomUUID() + ".xlsx";
            outputFiles = List.of(excelService.exportTablesToSingleExcel(result.getTables(), outputDir, outputFileName));
        } else {
            outputFiles = excelService.exportTablesToMultipleExcel(result.getTables(), outputDir, fileName);
        }

        return new PdfUploadResponse("Tables extracted successfully",
                fileName, result.getTables().size(), outputFiles);
    }

    private void createDirectories() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path outputPath = Paths.get(outputDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
    }
}