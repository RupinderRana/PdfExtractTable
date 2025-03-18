package scoreme.pdf.repository;

import org.springframework.stereotype.Repository;
import scoreme.pdf.entity.PdfFileInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PdfRepository {

    private final Map<String, PdfFileInfo> pdfStorage = new HashMap<>();

    public void savePdfInfo(PdfFileInfo fileInfo) {
        pdfStorage.put(fileInfo.getFileName(), fileInfo);
    }

    public Optional<PdfFileInfo> findByFileName(String fileName) {
        return Optional.ofNullable(pdfStorage.get(fileName));
    }

    public void deleteByFileName(String fileName) {
        pdfStorage.remove(fileName);
    }
}