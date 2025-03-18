package scoreme.pdf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import scoreme.pdf.DTO.PdfUploadRequest;
import scoreme.pdf.DTO.PdfUploadResponse;
import scoreme.pdf.service.PdfService;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {



    private final PdfService pdfService;

    @PostMapping("/upload")
    public ResponseEntity<PdfUploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "consolidate", defaultValue = "false") boolean consolidate) {

        try {
            PdfUploadResponse response = pdfService.processPdfFile(file, consolidate);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    new PdfUploadResponse("Failed to process PDF: " + e.getMessage(), null, 0, null));
        }
    }
}